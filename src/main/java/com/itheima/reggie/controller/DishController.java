package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    /**
     * 菜品管理 添加菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        redisTemplate.delete("dish_"+dishDto.getCategoryId()+"_1");
        return R.success("新增菜品成功");
    }

    /**
     * 分页查询菜品
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page,int pageSize,String name){
        Page<DishDto> dishDtoPage=dishService.selectList(page,pageSize,name);
        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> selectById(@PathVariable("id") Long id){
        log.info(String.valueOf(id));
        DishDto dishDto = dishService.getById(id);
        return R.success(dishDto);
    }

    /**
     * 菜品管理 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        redisTemplate.delete("dish_"+dishDto.getCategoryId()+"_1");

        return R.success("新增菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//
//        LambdaQueryWrapper<Dish> wrapper=new LambdaQueryWrapper<>();
//        wrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        wrapper.eq(Dish::getStatus,1);
//        wrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> dishList = dishService.list(wrapper);
//
//        return R.success(dishList);
//    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList =null;
        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();  //构造key
        //首先查询redis
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList!=null)
            return R.success(dishDtoList);
        //查询不到查询数据库
        LambdaQueryWrapper<Dish> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        wrapper.eq(Dish::getStatus,1);
        wrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishList = dishService.list(wrapper);

        dishDtoList=dishList.stream().map((item)->{
            DishDto dishDto=new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper=new LambdaQueryWrapper<>();
            flavorLambdaQueryWrapper.eq(DishFlavor::getDishId,item.getId());

            List<DishFlavor> list = dishFlavorService.list(flavorLambdaQueryWrapper);
            dishDto.setFlavors(list);
            return dishDto;
        }).collect(Collectors.toList());

        //然后在存入redis中
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }

    /**
     * 批量修改菜品状态
     * @param statue
     * @param ids
     * @return
     */
    @PostMapping("/status/{value}")
    public R<String> pause(@PathVariable("value") int statue,@RequestParam List<Long> ids){
        log.info(""+statue);
        log.info(""+ids);

        Dish dish=new Dish();
        dish.setStatus(statue);
        LambdaQueryWrapper<Dish> wrapper=new LambdaQueryWrapper<>();
        wrapper.in(Dish::getId,ids);

        dishService.update(dish,wrapper);
        return R.success("修改成功");
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        dishService.deleteWithFlavor(ids);
        return R.success("删除成功！");
    }

}
