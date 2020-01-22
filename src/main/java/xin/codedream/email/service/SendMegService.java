package xin.codedream.email.service;

import javax.mail.MessagingException;

/**
 * @author LeiXinXin
 * @date 2020/1/22
 */
public interface SendMegService {
    void sendMsg(String[] to, String from, String title, String msg) throws MessagingException;
}
