package xin.codedream.ncov.job;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xin.codedream.ncov.config.Config;
import xin.codedream.ncov.model.AreaStat;
import xin.codedream.ncov.model.Statistics;
import xin.codedream.ncov.model.TimeLine;
import xin.codedream.ncov.service.PushMessageService;

import javax.mail.MessagingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author LeiXinXin
 * @date 2020/1/22
 */
@Component
@EnableScheduling
@Slf4j
public class SendEmailJob {
    private final PushMessageService pushMessageService;
    private final TemplateEngine templateEngine;
    private final Config config;
    private ScriptEngine engine;
    private String areaStatTemplate;
    private String statisticsTemplate;
    private String timeLineTemplate;
    private ObjectMapper objectMapper;

    public SendEmailJob(PushMessageService pushMessageService, TemplateEngine templateEngine, Config config) throws IOException {
        this.pushMessageService = pushMessageService;
        this.templateEngine = templateEngine;
        this.config = config;
        init();
    }

    private void init() throws IOException {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // JavaScript Engine
        engine = new ScriptEngineManager().getEngineByName("nashorn");

        // JavaScript Templates
        areaStatTemplate = readFileContent("templates/js/AreaStat.js");
        statisticsTemplate = readFileContent("templates/js/Statistics.js");
        timeLineTemplate = readFileContent("templates/js/TimeLine.js");
    }

    private String readFileContent(String path) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(path);
        try (InputStream inputStream = classPathResource.getInputStream()) {
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            return new String(bytes);
        }
    }

    @Scheduled(cron = "#{config.cron}")
    public void check() throws IOException, ScriptException, MessagingException {
        log.info("开始检查最新动态");
        final Document document = Jsoup.connect(config.getUrl())
                .timeout(30000)
                .header("cache-control", "no-cache")
                .header("user-agent", config.getUserAgent())
                .get();
        Element timelineService = document.getElementById("getTimelineService");
        Element areaStatService = document.getElementById("getAreaStat");
        Element statisticsService = document.getElementById("getStatisticsService");
        String newTimeLineTemplate = timeLineTemplate.replace("{js}", timelineService.html());
        String newAreaStatTemplate = areaStatTemplate.replace("{js}", areaStatService.html());
        String newStatisticsTemplate = statisticsTemplate.replace("{js}", statisticsService.html());
        TimeLine timeLine = objectMapper.readValue(engine.eval(newTimeLineTemplate).toString(), TimeLine.class);
        List<Object> objectList = objectMapper.readValue(engine.eval(newAreaStatTemplate).toString(), List.class);
        List<AreaStat> areaStats = objectList.stream().collect(ArrayList::new, (arrayList, object) -> {
            Map<String, Object> map = (Map<String, Object>) object;
            AreaStat areaStat = objectMapper.convertValue(map, AreaStat.class);
            arrayList.add(areaStat);
        }, List::addAll);
        Statistics statistics = objectMapper.readValue(engine.eval(newStatisticsTemplate).toString(), Statistics.class);
        statistics.setTime(new Date(statistics.getModifyTime()));
        if (exists(timeLine.getTitle())) {
            log.info("检查完毕，暂无新消息");
            return;
        }
        Context context = new Context();
        context.setVariable("statistics", statistics);
        context.setVariable("areaStats", areaStats);
        context.setVariable("timeLine", timeLine);
        String msg = templateEngine.process("msg", context);
        pushMessageService.push(timeLine.getTitle(), msg);
        log.info("检查完毕");
    }

    private boolean exists(String title) throws IOException {
        final Path path = Paths.get(config.getPath(), config.getFileName());
        final boolean exists = Files.exists(path);
        if (exists) {
            final byte[] bytes = Files.readAllBytes(path);
            final String fileContent = new String(bytes);
            if (title.equals(fileContent)) {
                return true;
            }
        }
        Files.write(path, title.getBytes());
        return false;
    }


}
