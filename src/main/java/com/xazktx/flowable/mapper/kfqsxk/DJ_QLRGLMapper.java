package com.xazktx.flowable.mapper.kfqsxk;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DJ_QLRGLMapper {

    List<String> selectDJ_QLRGLBySLBH(String SLBH);

}
