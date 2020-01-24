package xin.codedream.email.job;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xin.codedream.email.config.Config;
import xin.codedream.email.service.SendMegService;

import javax.mail.MessagingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public SendEmailJob(SendMegService sendMegService, TemplateEngine templateEngine, Config config) {
        this.sendMegService = sendMegService;
        this.templateEngine = templateEngine;
        this.config = config;
    }

    @Scheduled(cron = "#{config.cron}")
    public void check() throws IOException, MessagingException, ScriptException {
        log.info("开始检查最新动态");
        final Document document = Jsoup.connect(config.getUrl())
                .timeout(30000)
                .header("cache-control", "no-cache")
                .get();
        Elements mapBoxElement = document.getElementsByClass("mapBox___qoGhu");
        Element mapTopElement = mapBoxElement.first();
        final Element timeElement = mapTopElement.getElementsByClass("mapTitle___2QtRg").first();
        String time = timeElement.text();
        Element confirmedNumberElement = mapTopElement.getElementsByClass("confirmedNumber___3WrF5").first();
        String confirmedNumber = confirmedNumberElement.text();
        Elements mapDescListElement = mapTopElement.getElementsByClass("descList___3iOuI");
        List<String> mapDescList = mapDescListElement.stream().map(Element::text).collect(Collectors.toList());
        Element timelineService = document.getElementById("getTimelineService");
        String timelineJs = "function timeline() {var window = {getTimelineService:0}; " + timelineService.html() + "  return window.getTimelineService;} timeline()";
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        ScriptObjectMirror eval = (ScriptObjectMirror) engine.eval(timelineJs);
        ScriptObjectMirror first = (ScriptObjectMirror) eval.get("0");
        String title = first.get("title").toString();
        String content = first.get("summary").toString();
        String origin = first.get("infoSource").toString();

        if (exists(title)) {
            log.info("检查完毕，暂无新消息");
            return;
        }
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("time", time);
        context.setVariable("confirmedNumber", confirmedNumber);
        context.setVariable("mapTopDescList", mapDescList);
        context.setVariable("content", content);
        context.setVariable("origin", origin);
        String msg = templateEngine.process("msg", context);
        sendMegService.sendMsg(config.getTo(), config.getFrom(), title, msg);
        log.info("检查完毕");
    }

    private List<String> getDescList(Elements descBoxElements) {
        List<String> descs = new ArrayList<>();
        Elements elements = descBoxElements.next();
        for (Element element : elements) {
            descs.add(element.text());
        }
        return descs;
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
