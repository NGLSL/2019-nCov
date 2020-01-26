package xin.codedream.ncov.service.impl;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import xin.codedream.ncov.config.Config;
import xin.codedream.ncov.service.PushMessageService;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author LeiXinXin
 * @date 2020/1/22
 */
@Service
public class PushMessageServiceImpl implements PushMessageService {
    private final JavaMailSender javaMailSender;
    private final Config config;

    public PushMessageServiceImpl(JavaMailSender javaMailSender, Config config) {
        this.javaMailSender = javaMailSender;
        this.config = config;
    }

    @Override
    public void push(String title, String msg) throws MessagingException {
        final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(config.getFrom());
        helper.setTo(config.getTo());
        helper.setSubject(title);
        helper.setText(msg, true);
        javaMailSender.send(mimeMessage);
    }
}
