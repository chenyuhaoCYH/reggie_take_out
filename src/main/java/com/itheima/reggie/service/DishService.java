package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据
    public void saveWithFlavor(DishDto dishDto);

    public Page<DishDto> selectList(int page, int pageSize, String name);

    public DishDto getById(Long id);

    void updateWithFlavor(DishDto dishDto);

    public void deleteWithFlavor(List<Long> ids);
}
