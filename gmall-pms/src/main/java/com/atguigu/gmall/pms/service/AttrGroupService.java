package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.AttrGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性分组
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2019-12-31 19:06:41
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryGroupsByCidPage(Long catId, QueryCondition queryCondition);

    AttrGroupVo queryById(Long gid);

    List<AttrGroupVo> queryGroupVOsByCid(Long catId);
}

