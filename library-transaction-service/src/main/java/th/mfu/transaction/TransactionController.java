package th.mfu.transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Records a borrow or a return.
 * <p>
 * Compare this with TransactionController in the 3-tier sample. There, checking
 * the book was one line:
 *
 * <pre>
 *   Optional&lt;Book&gt; book = bookRepository.findById(dto.getBookId());
 * </pre>
 *
 * There is no bookRepository here. The book is in another service, so the same
 * check becomes a call over the network - and a network can be slow, or absent.
 */
@RestController
@RequestMapping("/api")
public class TransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * The Assembler, exactly as in the 3-tier sample. It fills in everything this
     * service already knows. Already done for you - the interesting field is the
     * one it cannot fill, and that is what step 4 is about.
     */
    @Autowired
    private TransactionMapper transactionMapper;

    // TODO: (step 4) Let Spring give you the Feign client you annotated in step 3:
    //
    //   @Autowired
    //   private BookClient bookClient;
    //
    // You never write `new BookClient()`. There is no class to instantiate -
    // Feign built one for you at startup.

    /**
     * POST /api/transactions - record a borrow or a return.
     */
    @PostMapping("/transactions")
    public ResponseEntity<TransactionDTO> record(@RequestBody TransactionDTO dto) {

        // TODO: (step 4) Ask library-book-service whether this book exists, and
        //       what it is called:
        //
        //         BookDTO book = bookClient.getBook(dto.getBookId());
        //
        //       One line, and it is an HTTP request to another program. Watch the
        //       console of book-service when you run it: you will see the call
        //       arrive there.
        //
        // TODO: (step 4) Save the transaction. The mapper turns the DTO into a
        //       row, and the date is yours to set - not the caller's, or a client
        //       could backdate a borrow:
        //
        //         Transaction transaction = transactionMapper.toEntity(dto);
        //         transaction.setTransactionDate(LocalDate.now());
        //         Transaction saved = transactionRepository.save(transaction);
        //
        // TODO: (step 4) Answer 201 with the saved transaction PLUS the two
        //       fields that came from the other service:
        //
        //         TransactionDTO result = transactionMapper.toDto(saved);
        //         result.setBookTitle(book.getTitle());
        //         result.setServedBy(book.getServedBy());
        //
        // TODO: (step 5) The book might not exist. Feign does not return an empty
        //       Optional - it THROWS. Wrap the call:
        //
        //         catch (FeignException.NotFound e) -> answer 400
        //
        //       (import feign.FeignException)
        //
        // TODO: (step 5) book-service might be down altogether. That is a
        //       different failure and deserves a different answer:
        //
        //         catch (Exception e) -> answer 503 SERVICE_UNAVAILABLE
        //
        //       Try it: stop book-service and post a transaction. Without this
        //       catch the client gets a 500 and a stack trace. With it, the
        //       client is told something true - "the part I need is not
        //       available right now".

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * GET /api/transactions - list what we have recorded.
     * <p>
     * Our database holds a bookId and nothing else about the book. Run this now,
     * before you change anything: bookTitle comes back null on every row.
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> list() {
        List<TransactionDTO> result = new ArrayList<>();
        for (Transaction transaction : transactionRepository.findAll()) {

            TransactionDTO dto = transactionMapper.toDto(transaction);

            // TODO: (step 4) Fill in the title for this row, the same way the
            //       POST above does it:
            //
            //         BookDTO book = bookClient.getBook(transaction.getBookId());
            //         dto.setBookTitle(book.getTitle());
            //         dto.setServedBy(book.getServedBy());
            //
            // TODO: (step 5) Wrap that in a try/catch. If book-service does not
            //       answer, log it and leave bookTitle null - do NOT return 503
            //       here.
            //
            //       Think about why this differs from the POST: a borrow that was
            //       never checked is worthless, so 503 is right there. A list is
            //       still true without the titles, so throwing it away would be
            //       worse than returning it incomplete.
            //
            //       Then count the requests. Ten transactions, ten HTTP calls.
            //       The 3-tier version did this with one join. That is what the
            //       split costs, and the next lesson is about paying less.

            result.add(dto);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
