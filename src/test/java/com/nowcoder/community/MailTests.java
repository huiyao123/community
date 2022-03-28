package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    //thymeleaf模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail() {
        mailClient.sendMail("1522379817@qq.com","TEST","Welcome");
    }

    @Test
    public void testHtmlMail() {
        //利用Context(thymeleaf)对象构造参数传递给模板引擎
        Context context = new Context();
        //将需要传递的参数传给对象
        context.setVariable("username","sunday");

        //调用模板,引擎生成动态网页
        //传入模板文件路径和数据
        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);

        mailClient.sendMail("1522379817@qq.com","HTML",content);

    }
}


