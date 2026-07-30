package th.mfu.book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * book-service. Port 8090.
 */
// TODO: (step 1) Right now this is an ordinary Spring Boot app. It answers on
//       port 8090 and nobody else knows it exists.
//
//       Start the naming server, then open http://localhost:8761/ and look at
//       the list of registered services. It is empty.
//
//       Add one annotation:
//
//         @EnableDiscoveryClient
//         (import org.springframework.cloud.client.discovery.EnableDiscoveryClient)
//
//       ...and the three eureka lines in application.properties. Restart, wait
//       about 30 seconds, and reload the dashboard. The service is on the list.
//
//       That is the whole idea of a naming server: a service says "I am here and
//       my name is X", so that nobody has to write down its address.
@SpringBootApplication
public class BookServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(BookServiceApp.class, args);
    }
}
