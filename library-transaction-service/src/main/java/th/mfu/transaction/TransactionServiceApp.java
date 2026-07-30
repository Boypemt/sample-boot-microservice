package th.mfu.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * transaction-service. Port 8100.
 */
// TODO: (step 2) Join the naming server, exactly as you did for book-service:
//
//         @EnableDiscoveryClient
//
//       plus the three eureka lines in application.properties. Nothing new here
//       - do it from memory if you can. Two services on the dashboard now.
//
// TODO: (step 3) This service also has to CALL another one, so it needs one more
//       annotation:
//
//         @EnableFeignClients
//         (import org.springframework.cloud.openfeign.EnableFeignClients)
//
//       This is what makes Spring go and look for interfaces marked
//       @FeignClient and build them. Without it, BookClient stays an interface
//       that nobody implements, and the app fails to start with:
//
//         No qualifying bean of type 'th.mfu.transaction.BookClient'
@SpringBootApplication
public class TransactionServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApp.class, args);
    }
}
