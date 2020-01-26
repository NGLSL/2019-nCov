package xin.codedream.ncov.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "custom")
@Configuration
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Config {
    private String url;
    private String from;
    private String[] to;
    private String cron;
    private String path;
    private String fileName;
    private String userAgent;
}
