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
    @Autowired
    private BookClient bookClient;
    //
    // You never write `new BookClient()`. There is no class to instantiate -
    // Feign built one for you at startup.

    /**
     * POST /api/transactions - record a borrow or a return.
     */
    @PostMapping("/transactions")
    public ResponseEntity<TransactionDTO> record(@RequestBody TransactionDTO dto) {
        BookDTO bookdto = null;
        try{
            bookdto = bookClient.getBook(dto.getBookId());
        } catch (Exception e) {
            LOGGER.error("Book does not exist: {}", dto.getBookId());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }  
        Transaction transaction = transactionMapper.toEntity(dto);
        transaction.setTransactionDate(LocalDate.now());
        Transaction saved = transactionRepository.save(transaction);

        TransactionDTO result = transactionMapper.toDto(saved);
        result.setBookTitle(bookdto.getTitle());
        result.setServedBy(bookdto.getServedBy());
        return new ResponseEntity<>(result, HttpStatus.CREATED);
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

            BookDTO book = bookClient.getBook(transaction.getBookId());
            dto.setBookTitle(book.getTitle());
            dto.setServedBy(book.getServedBy());

            result.add(dto);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
