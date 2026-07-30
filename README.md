# sample-boot-microservice

Your **first microservice**. Starting point for the microservice session of
**Backend Programming** (MFU).

This is the library application again — but this time the books and the borrowing
records live in **two separate programs** that talk to each other over HTTP.

> **Start here → [TODO.md](TODO.md)** — six small steps, in order.

Continues from
[`sample-boot-3tier`](https://github.com/cnacha-mfu/sample-boot-3tier) and leads
into the `lab-web-microservice` lab.

**Stuck?** The finished version is in
[`sample-boot-microservice-solution`](https://github.com/cnacha-mfu/sample-boot-microservice-solution).
Every step in [TODO.md](TODO.md) links to the exact file that answers it.

---

## What changed since the 3-tier sample

In `sample-boot-3tier` you had **one** program with three tiers inside it. One
database, and `TransactionController` could look a book up like this:

```java
Optional<Book> book = bookRepository.findById(id);      // 3-tier
```

Now there are **two** programs, each with **its own database**. There is no
`bookRepository` in the transaction service any more, so the same check becomes:

```java
BookDTO book = bookClient.getBook(id);                  // microservice
```

One line for one line. But the second one is a call over the network, and a
network can be slow, or down, or answer with an error. **Most of what this
session teaches follows from that one difference.**

## The three programs

| Module | What it owns | Port |
| --- | --- | --- |
| `naming-server` | nothing — it keeps the list of running services | 8761 |
| `library-book-service` | books, and the book database | 8090 |
| `library-transaction-service` | borrow/return records, and their database | 8100 |

```
                         naming-server :8761
                       (who is running, and where)
                          ^                  ^
                register  |                  |  register + ask
                          |                  |
   library-book-service :8090  <---HTTP---  library-transaction-service :8100
```

`library-transaction-service` does **not** depend on `library-book-service` in
its `pom.xml`. Open the file and check. It cannot read the book table even if it
wanted to — it can only ask.

## Before you start

You need JDK 11+ and Maven. **No database to install**: each service uses its own
in-memory H2, so there is nothing to set up and nothing to clean up.

```bash
mvn install -DskipTests        # from THIS folder, always
```

Then start the three programs, **in this order**, each in its own terminal:

```bash
mvn -pl naming-server                spring-boot:run    # 1st - 8761
mvn -pl library-book-service         spring-boot:run    # 2nd - 8090
mvn -pl library-transaction-service  spring-boot:run    # 3rd - 8100
```

VS Code users: the four run configurations are already in `.vscode/launch.json`.

Then open the naming server: <http://localhost:8761/>

> The order matters. A service that starts before the naming server complains in
> its log until it can reach it. That is normal, and worth seeing once.

## Trying it by hand

Import `postman/library-microservice.postman_collection.json` into Postman. The
requests are in the same order as the steps, and each one says which step makes it
work. Or use curl:

```bash
curl http://localhost:8090/api/books/10002

curl -X POST -H "Content-Type: application/json" \
     -d '{"type":"BORROW","bookId":10002,"borrowerName":"Alice Johnson"}' \
     http://localhost:8100/api/transactions
```

## Checking your work

```bash
mvn test
```

15 tests. They fail at the start; each step turns more of them green. Do not edit
the tests - make them pass.

Maven stops at the first module that fails, so early on you only see
book-service's failures. To see all of them at once:

```bash
mvn test -fae
```

You can also test one service at a time:

```bash
mvn test -pl library-book-service
mvn test -pl library-transaction-service
```

---

## One thing worth arguing about

`BookDTO` exists **twice**: once in `library-book-service`, once in
`library-transaction-service`. Two copies of the same class looks like a mistake
you would be told off for.

It is deliberate. The two services are separate programs that agree on a *shape*,
not on a *class*. Put that class in a shared jar and the two services must be
rebuilt and redeployed together every time it changes - which is the thing you
split them up to avoid.

There is no free answer here. Duplication costs you something too. It is one of
the real trade-offs of this style, and it is better to see it than to be told
about it.
