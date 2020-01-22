package xin.codedream.email.job;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    public void check() throws IOException, MessagingException {
        log.info("开始检查最新动态");
        final Document document = Jsoup.connect(config.getUrl())
                .timeout(30000)
                .header("cache-control", "no-cache")
                .get();
        final Elements tabRight = document.getElementsByClass("tabRight___3Z0eJ");
        final Element topicTitle = tabRight.get(0);
        final Node node = topicTitle.childNode(0);
        final Node childNode = node.childNode(1);
        final String title = childNode.outerHtml();

        if (exists(title)) {
            log.info("检查完毕，暂无新消息");
            return;
        }

        final Elements mapBox = document.getElementsByClass("mapTitle___2QtRg");
        final Elements confirmedNumber = document.getElementsByClass("confirmedNumber___3WrF5");
        final Elements descBoxElements = document.getElementsByClass("descBox___3dfIo").get(0).getElementsByClass("descList___3iOuI");
        Elements mapTopElements = document.getElementsByClass("mapTop___2VZCl").get(0).getElementsByClass("descList___3iOuI");

        final Elements mapImg = document.getElementsByClass("mapImg___3LuBG");
        List<String> descList = getDescList(descBoxElements);
        List<String> mapTopDescList = getDescList(mapTopElements);
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("time", mapBox.get(0).text());
        context.setVariable("confirmedNumber", confirmedNumber.get(0).text());
        context.setVariable("mapTopDescList", mapTopDescList);
        context.setVariable("descList", descList);
        context.setVariable("img", mapImg.get(0).attr("src"));
        context.setVariable("content", tabRight.get(1).getElementsByClass("topicContent___1KVfy").get(0).text());
        context.setVariable("origin", tabRight.get(1).getElementsByClass("topicFrom___3xlna").get(0).text());
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
