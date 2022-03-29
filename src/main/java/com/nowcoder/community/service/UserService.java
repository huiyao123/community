package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    //注入邮件客户端
    private MailClient mailClient;

    @Autowired
    //注入模板引擎
    private TemplateEngine templateEngine;

    //注入域名
    @Value("${community.path.domain}")
    private String domain;

    //注入项目名
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null) {
            //非法参数异常
            throw new IllegalArgumentException("参数不能为空");
        }
        //判断账号是否为空
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        //判断密码是否为空
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        //判断邮箱是否为空
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }

        //注册用户
        //生成随机字符串,substring截取5位
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        //对密码进行加密,对设置的密码覆盖
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        //注册的用户默认都是普通用户,0
        user.setType(0);
        //设置状态为0，表示没有激活
        user.setStatus(0);
        //发送随机激活码
        user.setActivationCode(CommunityUtil.generateUUID());

        //设置随机头像,String的format方法用于创建格式化的字符串以及连接多个字符串对象
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        //将user添加到库中
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //  http://localhost:8080/community/activation/101/code
        //           域名+         项目名 + 功能访问名+ 用户名+激活码
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        //生成邮件内容,process方法用于传入路径和数据到浏览器(详见MailTests)。
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    //传入userId和激活码,以便于判断是否符合,从而激活账号
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
            //激活码相同
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,long expairedSeconds) {
        Map<String,Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)) {
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        //验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg","该账号未激活");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password+user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg","密码不正确");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        //设置凭证为随机生成的字符串(调用CommunityUtil中的方法)
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        //设置过期时间为当前时间后推移多少毫秒(Mapper中的过期时间)
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expairedSeconds*1000));
        //传入参数
        loginTicketMapper.insertloginticket(loginTicket);

        //将凭证传入
        map.put("ticket",loginTicket.getTicket());
        return map;

    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket,1);
    }

    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }
}