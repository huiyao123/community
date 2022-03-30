package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptchaProducer() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        //验证码的随机字符的范围
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        //生存随机字符的个数
        properties.setProperty("kaptcha.textproducer.char.length","4");
        //设置验证码的干扰
        properties.setProperty("kaptcha.textproducer.noise.impl","com.kaptcha.impl.NoNoise");

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        //bean被自动装配到容器中，通过容器可以得到properites实例，通过实例可以创建验证码
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
