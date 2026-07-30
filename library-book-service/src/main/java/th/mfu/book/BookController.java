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

    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>> listBooks() {
        List<BookDTO> books = new ArrayList<>();
        for (Book book : bookRepository.findAll()) {
            books.add(toDto(book));
        }
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<BookDTO> getBook(@PathVariable Long id) {
        Optional<Book> book = bookRepository.findById(id);
        if (!book.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(toDto(book.get()), HttpStatus.OK);
    }

    /**
     * Entity in, DTO out. Six lines, written by hand - you already know how to
     * let MapStruct do this, and today is not about that.
     */
    private BookDTO toDto(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setYear(book.getYear());

        // TODO: (step 6) Put the port of THIS copy of the service into the answer:
        //
        //   dto.setServedBy(Integer.parseInt(environment.getProperty("server.port")));
        //
        // Then start book-service a second time on another port. Every call
        // through transaction-service will show one port or the other, and that
        // is the load balancer choosing for you.

        return dto;
    }
}
