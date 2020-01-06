package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * spu信息
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2019-12-31 19:06:41
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo querySpuInfoquerySpuInfo(QueryCondition queryCondition, Long catId);

    void bigSave(SpuInfoVO spuInfoVO);
}

