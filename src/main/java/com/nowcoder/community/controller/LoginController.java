package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController  implements CommunityConstant {

    @Autowired
    private UserService userService;

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

}
