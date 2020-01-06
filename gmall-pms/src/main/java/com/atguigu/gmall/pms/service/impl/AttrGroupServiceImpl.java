package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.vo.AttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryGroupsByCidPage(Long catId, QueryCondition queryCondition) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(queryCondition),
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id",catId)
        );

        return new PageVo(page);
    }

    @Override
    public AttrGroupVo queryById(Long gid) {
        // 查询分组
        AttrGroupVo attrGroupVo = new AttrGroupVo();
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(gid);
        BeanUtils.copyProperties(attrGroupEntity,attrGroupVo);
        // 查询分组下的关联关系
        List<AttrAttrgroupRelationEntity> relations = relationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));
        // 判断关联关系是否为空，如果为空，直接返回
        if(CollectionUtils.isEmpty(relations)){
            return attrGroupVo;
        }
        attrGroupVo.setRelations(relations);
        // 收集分组下的所有规格id
        List<Long> attrIds = relations.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());
        // 查询分组下的所有规格参数
        List<AttrEntity> attrEntities = this.attrDao.selectBatchIds(attrIds);
        attrGroupVo.setAttrEntities(attrEntities);
        return attrGroupVo;
    }

    @Override
    public List<AttrGroupVo> queryGroupVOsByCid(Long catId) {
        //id查询规格参数组
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId));
        //遍历每个组，查询每个组下的中间关系
        List<AttrGroupVo> attrGroupVos = groupEntities.stream().map(attrGroupEntity -> {
            return this.queryById(attrGroupEntity.getAttrGroupId());
        }).collect(Collectors.toList());
        //查询每个组下的规格参数
        return attrGroupVos;
    }

}