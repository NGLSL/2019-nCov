package xin.codedream.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xin.codedream.email.config.Config;

@SpringBootApplication
@Slf4j
public class EmailApplication {
    private static Config config;

    public EmailApplication(Config config) {
        EmailApplication.config = config;
    }

    public static void main(String[] args) {
        SpringApplication.run(EmailApplication.class, args);
        log.info("初始化邮箱：{}", config.getFrom());
    }
}
