package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Override
    @Transactional  //加入事务控制
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息
        this.save(dishDto);
        // 获取菜品id
        Long dishDtoId = dishDto.getId();
        //保存菜品口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDtoId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 查询列表，并将菜品名称也加入进去
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    @Transactional
    public Page<DishDto> selectList(int page, int pageSize, String name) {
        LambdaQueryWrapper<Dish> wrapper=new LambdaQueryWrapper<>();
        wrapper.like(Strings.isNotEmpty(name),Dish::getName,name);
        wrapper.orderByDesc(Dish::getUpdateTime);

        Page<Dish> dishPage=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage =new Page<>(page,pageSize);

        this.page(dishPage,wrapper);

        //对象拷贝
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");
        List<Dish> records = dishPage.getRecords();
        List<DishDto> dishDtoList = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoList);

        return dishDtoPage;
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    @Transactional
    public DishDto getById(Long id) {
        DishDto dishDto=new DishDto();
        LambdaQueryWrapper<Dish> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getId,id);

        List<Dish> dishList =  this.list(wrapper);
        Dish dish= dishList.get(0);

        BeanUtils.copyProperties(dish,dishDto);

        LambdaQueryWrapper<DishFlavor> wrapper1=new LambdaQueryWrapper<>();
        wrapper1.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> list1 = dishFlavorService.list(wrapper1);

        dishDto.setFlavors(list1);

        return dishDto;
    }

    /**
     * 更新菜品信息同时更新口味信息
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品表
        this.updateById(dishDto);

        //更新口味表
        //先删除再插入
        LambdaQueryWrapper<DishFlavor> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(wrapper);

        //插入
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public void deleteWithFlavor(List<Long> ids) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,ids).eq(Dish::getStatus,1);

        int count = this.count(dishLambdaQueryWrapper);

        if (count>0){
            throw new CustomException("正在售卖的商品不可删除！");
        }

        this.removeByIds(ids);

        LambdaQueryWrapper<DishFlavor> dishDtoLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishDtoLambdaQueryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(dishDtoLambdaQueryWrapper);
    }
}
