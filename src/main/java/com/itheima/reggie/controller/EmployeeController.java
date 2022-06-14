package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param employee
     * @param request
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request){
        String password = employee.getPassword();
        //进行hd5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //查询数据库
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strings.isNotEmpty(employee.getUsername()),Employee::getUsername,employee.getUsername());

        Employee emp = employeeService.getOne(wrapper);

        //判断
        if (emp==null){
            return R.error("用户名不存在!");
        }else if (!emp.getPassword().equals(password)){
            return R.error("用户名或者密码错误!");
        }else if (emp.getStatus()!=1){
            return R.error("该账号已被锁定");
        }else {
            //登陆成功 存入session域中
            HttpSession session = request.getSession();
            session.setAttribute("employee",emp.getId());
            return R.success(emp);
        }
    }

    /**
     * 退出请求
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> addEmployee(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工:{}",employee.toString());
        String password = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(password);

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        Long empId = (Long)request.getSession().getAttribute("employee");

//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<Page<Employee>> page(int page,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //条件
        LambdaQueryWrapper<Employee> wrapper=new LambdaQueryWrapper<>();
        wrapper.like(Strings.isNotEmpty(name),Employee::getName,name);
        //添加排序条件(根据更新时间降序)
        wrapper.orderByDesc(Employee::getUpdateTime);

        //分页构造器
        Page<Employee> employeePage=new Page<>(page,pageSize);

        employeeService.page(employeePage, wrapper);

        return R.success(employeePage);
    }

    /**
     * 修改员工信息
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request){
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        log.info(employee.toString());
        log.info("线程id为:"+Thread.currentThread().getId());
        employeeService.updateById(employee);

        return R.success("修改成功!");
    }

    /**
     * 根据id查询
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable("id") Long id){
        Employee byId = employeeService.getById(id);
        if (byId==null)
            return R.error("出现未知错误，请重试!");
        return R.success(byId);
    }
}
