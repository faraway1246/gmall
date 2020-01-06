package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCidOrTypePage(Integer type, Long cid, QueryCondition queryCondition) {

        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id",cid);
        if (type != null) {
            wrapper.eq("attr_type", type);
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(queryCondition), // 构建分页条件
                wrapper
        );
        return new PageVo(page);
    }

    @Override
    public void saveAttrVO(AttrVo attrVo) {
        // 新增规格参数
        this.attrDao.insert(attrVo);

        // 新增中间表
        AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
        relation.setAttrId(attrVo.getAttrId());
        relation.setAttrGroupId(attrVo.getAttrGroupId());
        this.relationDao.insert(relation);
    }

}