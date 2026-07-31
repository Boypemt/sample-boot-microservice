package th.mfu.transaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * How this service asks library-book-service for a book.
 * <p>
 * You will not write any code in this file. That is not a mistake - read the
 * TODO.
 */
// TODO: (step 3) Turn this interface into a Feign client:
//
//         @FeignClient(name = "library-book-service")
//         (import org.springframework.cloud.openfeign.FeignClient)
//
//       Feign reads the annotations below and writes the HTTP call for you: it
//       builds the URL, sends the GET, and turns the JSON answer into a BookDTO.
//       There is no code to write, and that is the point of the exercise.
//
//       "library-book-service" is a NAME, not an address. It is the
//       spring.application.name of the other service. The naming server turns it
//       into a real address when the call happens.
//
//       Never put a URL here. A URL is one machine and one port. A name can be
//       two copies on two ports, which is step 6.
@FeignClient(name = "library-book-service")
public interface BookClient {

    @GetMapping("/api/books/{id}")
    BookDTO getBook(@PathVariable("id") Long id);
}
