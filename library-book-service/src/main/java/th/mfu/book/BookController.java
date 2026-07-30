package th.mfu.book;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The REST service for books. Nothing new here - this is the same kind of
 * controller you wrote in the 3-tier sample.
 * <p>
 * In this project it plays a new role: it is the service that ANOTHER service
 * calls.
 */
@RestController
@RequestMapping("/api")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    /**
     * Tells us the port this copy of the service is running on.
     */
    @Autowired
    private Environment environment;

    /**
     * The Assembler, exactly as in the 3-tier sample. Nobody writes the class:
     * MapStruct generated it during the build and componentModel = "spring" made
     * it a bean, so it can be injected like any other. Already done for you.
     */
    @Autowired
    private BookMapper bookMapper;

    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>> listBooks() {
        List<BookDTO> books = new ArrayList<>();
        for (Book book : bookRepository.findAll()) {
            books.add(bookMapper.toDto(book, thisPort()));
        }
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<BookDTO> getBook(@PathVariable Long id) {
        Optional<Book> book = bookRepository.findById(id);
        if (!book.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(bookMapper.toDto(book.get(), thisPort()), HttpStatus.OK);
    }

    /**
     * Which port THIS copy of the service is running on. The mapper puts whatever
     * this returns into servedBy - so right now every answer says 0.
     */
    private int thisPort() {

        // TODO: (step 6) Return the real port instead of 0:
        //
        //   return Integer.parseInt(environment.getProperty("server.port"));
        //
        // Then start book-service a second time on another port. Every call
        // through transaction-service will show one port or the other, and that
        // is the load balancer choosing for you.
        //
        // The load balancer works without this - but you cannot SEE it working,
        // and a lesson you cannot see is a lesson nobody believes.

        return 0;
    }
}
