package com.xazktx.flowable.mapper.flowable;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActDeModelMapper {

    String selectJosnByModelKey(String Key);

}
