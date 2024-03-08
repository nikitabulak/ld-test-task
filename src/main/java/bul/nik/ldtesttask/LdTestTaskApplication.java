package bul.nik.ldtesttask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LdTestTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(LdTestTaskApplication.class, args);
    }

}
