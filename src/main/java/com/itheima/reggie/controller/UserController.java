package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();

        if (Strings.isNotEmpty(phone)){
            //生成四位验证码
            String  code = ValidateCodeUtils.generateValidateCode(4).toString();
            code="1234";
            log.info(code);
            //发送短信验证码
//            SMSUtils.sendMessage("瑞吉外卖","",phone,code);
//            session.setAttribute(phone,code);
            //将生成验证码缓存到redis中并设置有效期5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.success("发送成功");
        }
        return R.error("发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String,String> map,HttpSession session){
        String phone = map.get("phone");

        String cur_code = map.get("code");
        cur_code="1234";
//        String code = (String) session.getAttribute();
        String code = redisTemplate.opsForValue().get(phone);
        log.info(code);

        if (code!=null&&code.equals(cur_code)){
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,map.get("phone"));

            User user = userService.getOne(queryWrapper);
            if (user==null){
                user=new User();
                user.setPhone(map.get("phone"));
                user.setStatus(1);

                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            //登陆成功，删除验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("登陆失败");
    }

}
