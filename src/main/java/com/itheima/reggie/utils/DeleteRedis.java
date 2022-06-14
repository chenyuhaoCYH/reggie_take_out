package com.itheima.reggie.utils;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Aspect
public class DeleteRedis {
    @Autowired
    RedisTemplate<Object,Object> redisTemplate;

    @Before(value = "execution(* com.itheima.reggie.controller.*.update(..))")
    public boolean clear(){
        System.out.println("清理redis成功");
        return true;
    }
}
