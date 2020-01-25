package xin.codedream.email.job;

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
import xin.codedream.email.config.Config;
import xin.codedream.email.model.AreaStat;
import xin.codedream.email.model.Statistics;
import xin.codedream.email.model.TimeLine;
import xin.codedream.email.service.SendMegService;

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
import java.util.function.BiConsumer;

/**
 * @author LeiXinXin
 * @date 2020/1/22
 */
@Component
@EnableScheduling
@Slf4j
public class SendEmailJob {
    private final SendMegService sendMegService;
    private final TemplateEngine templateEngine;
    private final Config config;
    private ScriptEngine engine;
    private String areaStatTemplate;
    private String statisticsTemplate;
    private String timeLineTemplate;
    private ObjectMapper objectMapper;

    public SendEmailJob(SendMegService sendMegService, TemplateEngine templateEngine, Config config) throws IOException {
        this.sendMegService = sendMegService;
        this.templateEngine = templateEngine;
        this.config = config;
        init();
    }

    private void init() throws IOException {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        engine = new ScriptEngineManager().getEngineByName("nashorn");

        areaStatTemplate = readFile("templates/js/AreaStat.js");
        statisticsTemplate = readFile("templates/js/Statistics.js");
        timeLineTemplate = readFile("templates/js/TimeLine.js");
    }

    private String readFile(String path) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(path);
        try (InputStream inputStream = classPathResource.getInputStream()) {
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            return new String(bytes);
        }
    }

    @Scheduled(cron = "#{config.cron}")
    public void check() throws IOException, MessagingException, ScriptException {
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
        @SuppressWarnings("unchecked") List<AreaStat> areaStats = (List<AreaStat>) objectMapper.readValue(engine.eval(newAreaStatTemplate).toString(), List.class)
                .stream()
                .collect(ArrayList::new, (BiConsumer<List<AreaStat>, Map<String, Object>>) (areaStats1, stringObjectMap) -> {
                    AreaStat areaStat = objectMapper.convertValue(stringObjectMap, AreaStat.class);
                    areaStats1.add(areaStat);
                }, (BiConsumer<List<AreaStat>, List<AreaStat>>) List::addAll);
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
        sendMegService.sendMsg(config.getTo(), config.getFrom(), timeLine.getTitle(), msg);
        log.info("检查完毕");
    }

    private boolean exists(String description) throws IOException {
        final Path path = Paths.get(config.getPath(), config.getFileName());
        final boolean exists = Files.exists(path);
        if (exists) {
            final byte[] bytes = Files.readAllBytes(path);
            final String fileContent = new String(bytes);
            if (description.equals(fileContent)) {
                return true;
            }
        }
        Files.write(path, description.getBytes());
        return false;
    }


}
