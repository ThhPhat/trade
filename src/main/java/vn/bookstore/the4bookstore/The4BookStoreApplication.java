package vn.bookstore.the4bookstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class The4BookStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(The4BookStoreApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String baseUrl = "http://localhost:" + port + contextPath;

        System.out.println("\n" +
            "========================================================================\n" +
            "  🎉 BOOKSTORE ĐÃ KHỞI CHẠY THÀNH CÔNG!\n\n" +
            "  👉 Trang chủ:       " + baseUrl + "/\n" +
            "  👉 Đăng nhập:       " + baseUrl + "/login\n" +
            "  👉 Trang Admin:     " + baseUrl + "/admin\n" +
            "  👉 Kênh người bán:  " + baseUrl + "/vendor\n" +
            "========================================================================\n"
        );
    }
}

