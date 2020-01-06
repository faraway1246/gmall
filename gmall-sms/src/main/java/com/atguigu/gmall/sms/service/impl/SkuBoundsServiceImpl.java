package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.vo.SaleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {
    @Autowired
    private SkuFullReductionDao skuFullReductionDao;
    @Autowired
    private SkuLadderDao skuLadderDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public void saveSales(SaleVO saleVO) {
        //3.1 bounds保存
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(saleVO,skuBoundsEntity);
        List<String> works = saleVO.getWork();
        skuBoundsEntity.setWork(new Integer(works.get(0))+new Integer(works.get(1))*2+ new Integer(works.get(2))*4+new Integer(works.get(3))*8);
        this.save(skuBoundsEntity);

        //3.2 满减优惠
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(saleVO,reductionEntity);
        reductionEntity.setAddOther(saleVO.getFullAddOther());
        this.skuFullReductionDao.insert(reductionEntity);

        //3.3打折信息
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(saleVO,ladderEntity);
        ladderEntity.setAddOther(saleVO.getLadderAddOther());
        this.skuLadderDao.insert(ladderEntity);
    }

}