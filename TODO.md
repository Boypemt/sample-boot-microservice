# Live coding plan — your first microservice

The library app, split into two services that talk to each other.

Everything structural is done. What is left are the `// TODO` markers, in the
order we will do them. Search the project for `TODO` to find them all.

---

## Where the answers are

The finished version is in
[`cnacha-mfu/sample-boot-microservice-solution`](https://github.com/cnacha-mfu/sample-boot-microservice-solution).
Every step below links straight to the file that answers it.

---

## Before we start

```bash
mvn install -DskipTests        # from THIS folder
```

Three terminals, in this order:

```bash
mvn -pl naming-server                spring-boot:run    # 8761
mvn -pl library-book-service         spring-boot:run    # 8090
mvn -pl library-transaction-service  spring-boot:run    # 8100
```

Dashboard: <http://localhost:8761/> · Books:
<http://localhost:8090/api/books/10002>

Check your work at any time:

```bash
mvn test
```

Or import `postman/library-microservice.postman_collection.json` into Postman and
work down the list.

---

## What we are building

| Module | Owns | Port |
| --- | --- | --- |
| `naming-server` | the list of running services | 8761 |
| `library-book-service` | books + the book database | 8090 |
| `library-transaction-service` | borrow/return records + their database | 8100 |

The one idea behind all six steps:

> `library-transaction-service` needs a book. It does not have one, it cannot
> reach the book database, and it does not know book-service's address. So it
> asks the naming server who has books, and then asks that service over HTTP.

---

# The six steps

## Step 1 — put book-service on the map (10 min)

**Files:** `library-book-service/.../BookServiceApp.java`,
`library-book-service/src/main/resources/application.properties`

> 💡 **Solution:**
> [`BookServiceApp.java`](https://github.com/cnacha-mfu/sample-boot-microservice-solution/blob/main/library-book-service/src/main/java/th/mfu/book/BookServiceApp.java)
> · [`application.properties`](https://github.com/cnacha-mfu/sample-boot-microservice-solution/blob/main/library-book-service/src/main/resources/application.properties)

Start the naming server and open <http://localhost:8761/>. The list of registered
services is **empty** — book-service is running, and nobody knows.

Add one annotation:

```java
@EnableDiscoveryClient
```

and three lines to `application.properties`:

```properties
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
```

Restart, **wait about 30 seconds**, reload the dashboard.
`LIBRARY-BOOK-SERVICE` is on the list.

**Why bother?** Because `spring.application.name` is now a name anyone can look
up. Nobody has to write down `localhost:8090` — and in step 6 that name will
point at two different ports at once.

✅ `itJoinsTheNamingServer` passes.

---

## Step 2 — the same thing again, on your own (5 min)

**Files:** `library-transaction-service/.../TransactionServiceApp.java`,
`library-transaction-service/src/main/resources/application.properties`

> 💡 **Solution:**
> [`TransactionServiceApp.java`](https://github.com/cnacha-mfu/sample-boot-microservice-solution/blob/main/library-transaction-service/src/main/java/th/mfu/transaction/TransactionServiceApp.java)
> · [`application.properties`](https://github.com/cnacha-mfu/sample-boot-microservice-solution/blob/main/library-transaction-service/src/main/resources/application.properties)

Do step 1 to transaction-service. Same annotation, same three lines. Try it from
memory.

Two services on the dashboard now.

There is nothing new in this step, and that is the point: registering a service is
boilerplate you will write once per service and then forget about.

✅ `itJoinsTheNamingServer` passes for transaction-service too.
(`itHasANameAndAPort` was already green — the name and the port were in
`application.properties` from the start.)

---

## Step 3 — the Feign client (15 min)

**Files:** `library-transaction-service/.../BookClient.java`,
`library-transaction-service/.../TransactionServiceApp.java`

> 💡 **Solution:**
> [`BookClient.java`](https://github.com/cnacha-mfu/sample-boot-microservice-solution/blob/main/library-transaction-service/src/main/java/th/mfu/transaction/BookClient.java)
> · [`TransactionServiceApp.java`](https://github.com/cnacha-mfu/sample-boot-microservice-solution/blob/main/library-transaction-service/src/main/java/th/mfu/transaction/TransactionServiceApp.java)

Open `BookClient.java`. It is an interface with one method and no code, and it is
going to stay that way.

```java
@FeignClient(name = "library-book-service")
public interface BookClient {

    @GetMapping("/api/books/{id}")
    BookDTO getBook(@PathVariable("id") Long id);
}
```

Then add `@EnableFeignClients` to `TransactionServiceApp`.

**Say this out loud:** nobody implements this interface. Feign reads the
annotations at startup and builds the class for you — it makes the URL, sends the
GET, and turns the JSON into a `BookDTO`.

**And this:** `"library-book-service"` is a **name**, not an address. It is the
`spring.application.name` of the other service. The naming server turns it into a
real address at the moment of the call. Write a URL here instead and step 6
becomes impossible.

✅ `feignClientsAreSwitchedOn`, `theBookClientAsksForAServiceByName` pass.

---

## Step 4 — use it (20 min) ⭐

**File:** `library-transaction-service/.../TransactionController.java`

> 💡 **Solution:**
> [`TransactionController.java`](https://github.com/cnacha-mfu/sample-boot-microservice-solution/blob/main/library-transaction-service/src/main/java/th/mfu/transaction/TransactionController.java)

This is the centrepiece. Put the 3-tier version of this method on screen next to
it:

```java
Optional<Book> book = bookRepository.findById(dto.getBookId());   // 3-tier
BookDTO         book = bookClient.getBook(dto.getBookId());       // now
```

Then finish `record(...)`:

1. `@Autowired BookClient bookClient;`
2. ask book-service for the book
3. save the transaction
4. answer **201** with the saved transaction **plus** `bookTitle` and `servedBy`
   from the answer

Send the POST from Postman and look at two things:

```json
{ "id": 1, "type": "BORROW", "bookId": 10002, "borrowerName": "Alice Johnson",
  "bookTitle": "1984", "servedBy": 8090 }
```

- `bookTitle` is **not in our database**. It arrived from another program while
  the request was being handled.
- **The console of book-service** shows the call arriving there. Two programs, one
  request.

✅ `itRecordsABorrowAndAddsWhatTheOtherServiceKnows` passes.

---

## Step 5 — what happens when it goes wrong (20 min) ⭐

**Files:** `library-transaction-service/.../TransactionController.java`,
`library-transaction-service/src/main/resources/application.properties`

> 💡 **Solution:**
> [`TransactionController.java`](https://github.com/cnacha-mfu/sample-boot-microservice-solution/blob/main/library-transaction-service/src/main/java/th/mfu/transaction/TransactionController.java)
> · [`application.properties`](https://github.com/cnacha-mfu/sample-boot-microservice-solution/blob/main/library-transaction-service/src/main/resources/application.properties)

This is the step the 3-tier sample never needed, and it is the real lesson of the
day.

### 5a. The book does not exist

POST a transaction with `"bookId": 999999`. You get a **500** and a stack trace.

In the 3-tier app this was an empty `Optional`. Feign does not do that — a 404
from the other service arrives as a **thrown exception**:

```java
catch (FeignException.NotFound e) {
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
}
```

400, because the caller asked for a book that is not there. Their mistake, and
now we say so.

### 5b. book-service is not running at all

**Stop book-service** and POST the *good* request — the one that worked in step 4.
The same body, and now nothing works.

```java
catch (Exception e) {
    return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
}
```

503 is a different answer from 400 and it means a different thing: *you asked
correctly, I cannot do it right now.*

Also add the two timeouts:

```properties
feign.client.config.default.connectTimeout=2000
feign.client.config.default.readTimeout=2000
```

**The uncomfortable question to end on:** the 3-tier app could not fail this way.
We have made the application less reliable by splitting it up — transaction-service
is now only as available as book-service is. What did we get in exchange?

(Answers worth having: the two can be deployed, scaled and rewritten separately.
Step 6 shows the scaling half.)

✅ `anUnknownBookIs400`, `bookServiceBeingDownIs503` pass.

---

## Step 6 — two copies of book-service (15 min)

**File:** `library-book-service/.../BookController.java`

> 💡 **Solution:**
> [`BookController.java`](https://github.com/cnacha-mfu/sample-boot-microservice-solution/blob/main/library-book-service/src/main/java/th/mfu/book/BookController.java)

First make the service say who it is. In `toDto(...)`:

```java
dto.setServedBy(Integer.parseInt(environment.getProperty("server.port")));
```

Then start a **second** book-service, on another port:

```bash
mvn -pl library-book-service spring-boot:run -Dspring-boot.run.arguments=--server.port=8091
```

Wait for the dashboard to show **2** instances of `LIBRARY-BOOK-SERVICE`, and
then be patient for a little longer.

The dashboard is the naming server's list. transaction-service keeps its *own*
copy of that list and re-reads it every few seconds, so for a moment the server
knows about two copies and the caller still knows about one. If every answer says
the same port, you are almost certainly looking at that gap rather than at a
bug - wait a few tens of seconds and send it again.

Now POST the same borrow again, and again, and again. `servedBy` changes:
`8090`, `8091`, `8090`, …

**Nothing in transaction-service changed.** It still asks for
`"library-book-service"`. The name pointed at one program a minute ago and points
at two now, and the load balancer picks one per call. That is what the name bought
you in step 1.

Try this too: stop the copy on 8090 while the other one runs, and keep posting.
The requests keep working. Compare that with 5b.

✅ `itSaysWhichCopyAnswered` passes — `mvn test` is fully green.

---

## Wrap-up

```bash
mvn test        # 15 tests, all green
```

| Idea | Where it is |
| --- | --- |
| a service owns its own database | two H2 databases, no shared tables |
| service registry | `naming-server`, `@EnableDiscoveryClient` |
| find a service by name | `@FeignClient(name = "library-book-service")` |
| the network can fail | the two `catch` blocks and the timeouts |
| scale one part on its own | two copies of book-service, one name |

What we deliberately did **not** do: an API gateway, a circuit breaker, message
queues, tracing, configuration server. Each one solves a real problem you will
meet after these six steps — not before them.

Then: **`lab-web-microservice`**. Same shape, different domain.

| In the lab | Here |
| --- | --- |
| `lab-microservice-eureka-naming-server` | `naming-server` |
| `lab-microservice-forex-service` | `library-book-service` |
| `lab-microservice-currency-conversion` | `library-transaction-service` |
| `CurrencyExchangeServiceProxy` | `BookClient` |
| `port` in the answer | `servedBy` in the answer |

---

## If something breaks

| Message | Cause |
| --- | --- |
| `Could not find artifact th.mfu:library-microservice:pom` | Ran Maven inside one module. Run `mvn install -DskipTests` from the top folder |
| `No qualifying bean of type 'th.mfu.transaction.BookClient'` | Step 3 — `@EnableFeignClients` or `@FeignClient` missing |
| The dashboard stays empty | Step 1 — the eureka lines, or you did not wait 30 seconds |
| `Load balancer does not contain an instance for the service library-book-service` | book-service is not running, or it never registered. Check the dashboard |
| `Connection refused: localhost:8761` | The naming server is not running. Start it first |
| `Port 8090 is already in use` | A copy is still running from earlier. Note 8080 and 8081 are left free on purpose: that is where yesterday's 3-tier sample runs |
| The POST answers 500 | Step 4 not finished, or step 5's `catch` blocks are missing |
| `servedBy` is always 0 | Step 6 — `setServedBy` not added |
| `servedBy` never changes | Only one copy is running - or you tried too soon. transaction-service re-reads the list every few seconds |
| The transaction list is empty after a restart | Correct. The database is in memory |
