package xin.codedream.ncov;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xin.codedream.ncov.config.Config;

/**
 * Start Application
 *
 * @author LeiXinXin
 * @date 2020/1/22
 */
@SpringBootApplication
@Slf4j
public class Application {
    private static Config config;

    public Application(Config config) {
        Application.config = config;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        log.info("初始化邮箱：{}", config.getFrom());
    }
}
