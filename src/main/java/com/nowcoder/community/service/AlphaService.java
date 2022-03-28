package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
/*
每次调用都会产生一个实例
@Scope("prototype")
 */
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    public AlphaService() {
        System.out.println("实例化AlphaService");
    }

    //注解使方法在构造器之后调用
    @PostConstruct
    public void init() {
        System.out.println("初始化AlphaService");
    }

    //注解使方法在销毁对象之前调用
    @PreDestroy
    public void destory() {
        System.out.println("销毁AlphaService");
    }

    public String find() {
        return  alphaDao.select();
    }
}
