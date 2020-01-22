package xin.codedream.email.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import xin.codedream.email.service.SendMegService;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author LeiXinXin
 * @date 2020/1/22
 */
@Service
public class SendMegServiceImpl implements SendMegService {
    private final JavaMailSender javaMailSender;

    public SendMegServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendMsg(String[] to, String from, String title, String msg) throws MessagingException {
        final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(title);
        helper.setText(msg, true);
        javaMailSender.send(mimeMessage);
    }
}
