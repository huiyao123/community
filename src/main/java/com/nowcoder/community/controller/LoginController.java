package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController  implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    //注册界面
    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    //登录界面
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path="/register",method = RequestMethod.POST)
    public String register(Model model, User user) {
        //将数据传入map中从而便于进行操作
       Map<String,Object> map = userService.register(user);
       if (map == null ||map.isEmpty()) {
           //弹出消息，并设置8秒后的跳转目标
            model.addAttribute("msg","注册成功,我们已经向您的邮箱发送了一封邮件，请尽快激活!");
            model.addAttribute("target","/index");
            return "/site/operate-result";
       }else {
           model.addAttribute("usernameMsg",map.get("usernameMsg"));
           model.addAttribute("passwordMsg",map.get("passwordMsg"));
           model.addAttribute("emailMsg",map.get("emailMsg"));
           return "/site/register";
       }
    }
    //  http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId")int userId, @PathVariable("code") String code) {
        //调用userService中的activation,判断对应的激活码是否正确
        int result = userService.activation(userId,code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg","激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target","/login");

        }else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg","无效的操作，该账号已经激活过了!");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败，您提供的激活码不正确!");
            model.addAttribute("target","/index");

        }
        return "/site/operate-result";
    }

    //传送图片，手动向浏览器发送响应
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image =  kaptchaProducer.createImage(text);

        //将验证码存入sessio
        session.setAttribute("kaptcha",text);

        //将图片输出给浏览器
        //声明返回的是png格式的图片
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            //输出的是图片，格式为png，用os流输出
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
           logger.error("响应验证码失败",e.getMessage());
        }
    }

    @RequestMapping(path="/login",method = RequestMethod.POST)
    /**
     *1返回数据会响应请求(model)
     *2页面传入验证码，需要与生成的验证码比较（生成的验证码在session中）
     * 3将titcket发放给客户端保存,需要cookie保存
     */
    public String login(String username,String password,String code,boolean rememberme,
                        Model model,HttpSession session,HttpServletResponse response) {
        //检查验证码
        String kaptcha = (String) session.getAttribute("kaptcha");
        //判断两种验证码是否为空，以及是否相等（IgnoreCaes方法是忽略大小写）
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg","验证码不正确!");
            return "/site/login";
        }

        //检查账号,密码
        //判断是否选择"记住我"
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = userService.login(username,password,expiredSeconds);
        //判断是否成功
        if (map.containsKey("ticket")) {
            //将得到的ticket对象转换成字符串传给cookie(将cookie发送给客户端保存)
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            //设置cookie有效的路径
            cookie.setPath(contextPath);
            //设置cookie的有效时间
            cookie.setMaxAge(expiredSeconds);
            //在响应时发送给浏览器
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    //利用CookieValue注解得到cookie
    public String logout(@CookieValue("ticket") String ticket) {
        //调用方法设置ticket
        userService.logout(ticket);
        return "redirect:/login";
    }

}
