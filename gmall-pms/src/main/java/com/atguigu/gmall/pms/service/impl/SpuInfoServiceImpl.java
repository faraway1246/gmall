package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.pms.vo.BaseAttrValueVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.gmall.sms.vo.SaleVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao descDao;
    @Autowired
    private ProductAttrValueService attrValueService;
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Autowired
    private SkuImagesDao imagesDao;
    @Autowired
    private SkuSaleAttrValueDao saleAttrValueDao;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private GmallSmsClient smsClient;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuInfoquerySpuInfo(QueryCondition queryCondition, Long catId) {
        // 封装查询条件
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<SpuInfoEntity>();
        // 1.如果分类id不为0，要根据分类id查，否则查全部
        if (catId != 0){
            wrapper.eq("catalog_id", catId);
        }
        // 2.如果用户输入了检索条件，根据检索条件查
        String key = queryCondition.getKey();
        if (StringUtils.isNotBlank(key)){
            wrapper.and(i->i.like("spu_name", key).or().like("id", key));
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(queryCondition),
                wrapper
        );

        return new PageVo(page);
    }

    @Override
    public void bigSave(SpuInfoVO spuInfoVO) {

        //1.保存spu相关信息
        //1.1 spuInfo
        spuInfoVO.setCreateTime(new Date());
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
        this.save(spuInfoVO);
        Long spuId = spuInfoVO.getId();
        //1.2 spuInfoDesc
        List<String> spuImages = spuInfoVO.getSpuImages();
        if(!CollectionUtils.isEmpty(spuImages)){
            SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
            descEntity.setSpuId(spuId);
            descEntity.setDecript(StringUtils.join(spuImages,","));
            descDao.insert(descEntity);
        }
        //1.3 基础属性的相关信息
        List<BaseAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        if(!CollectionUtils.isEmpty(baseAttrs)){
            List<ProductAttrValueEntity> attrValues = baseAttrs.stream().map(baseAttrValueVO -> {
                ProductAttrValueEntity attrValueEntity = new ProductAttrValueEntity();
                BeanUtils.copyProperties(baseAttrValueVO, attrValueEntity);
                attrValueEntity.setSpuId(spuId);
                attrValueEntity.setAttrSort(0);
                attrValueEntity.setQuickShow(0);
                return attrValueEntity;
            }).collect(Collectors.toList());
            attrValueService.saveBatch(attrValues);
        }

        //2.保存sku的相关信息
        List<SkuInfoVO> skus = spuInfoVO.getSkus();
        if(CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(sku ->{
            //2.1 skuInfo
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku,skuInfoEntity);
            // 品牌和分类的id需要从spuInfo中获取
            skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
            // 获取随机的uuid作为sku的编码
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString().substring(0, 10).toUpperCase());
            // 获取图片列表
            List<String> images = sku.getImages();
            // 如果图片列表不为null，则设置默认图片
            if (!CollectionUtils.isEmpty(images)){
                // 设置第一张图片作为默认图片
                skuInfoEntity.setSkuDefaultImg(skuInfoEntity.getSkuDefaultImg()==null ? images.get(0) : skuInfoEntity.getSkuDefaultImg());
            }
            skuInfoEntity.setSpuId(spuId);
            skuInfoDao.insert(skuInfoEntity);

            // 获取skuId
            Long skuId = skuInfoEntity.getSkuId();
            //2.2 skuInfoImages
            if (!CollectionUtils.isEmpty(images)){
                String defaultImage = images.get(0);
                List<SkuImagesEntity> skuImageses = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setDefaultImg(StringUtils.equals(defaultImage, image) ? 1 : 0);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setImgUrl(image);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImageses);
            }
            //2.3 skuSaleAttrValue,即保存sku的规格参数（销售属性）
            List<SkuSaleAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            saleAttrs.forEach(saleAttrValueEntity -> {
                // 设置属性名，需要根据id查询AttrEntity
                saleAttrValueEntity.setAttrName(this.attrDao.selectById(saleAttrValueEntity.getAttrId()).getAttrName());
                saleAttrValueEntity.setAttrSort(0);
                saleAttrValueEntity.setSkuId(skuId);
            });
            this.skuSaleAttrValueService.saveBatch(saleAttrs);

            //3.保存营销的相关信息
            SaleVO saleVO = new SaleVO();
            BeanUtils.copyProperties(sku,saleVO);
            saleVO.setSkuId(skuId);
            this.smsClient.saveSales(saleVO);

        });
    }

}