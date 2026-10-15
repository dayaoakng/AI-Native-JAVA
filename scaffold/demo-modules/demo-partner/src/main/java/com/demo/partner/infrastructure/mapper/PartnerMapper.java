package com.demo.partner.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.partner.domain.entity.PartnerEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 伙伴 MyBatis-Plus Mapper。
 */
@Mapper
public interface PartnerMapper extends BaseMapper<PartnerEntity> {
}
