package xin.codedream.ncov.service;

import javax.mail.MessagingException;

/**
 * @author LeiXinXin
 * @date 2020/1/22
 */
public interface PushMessageService {
    void push(String title, String msg) throws MessagingException;
}
