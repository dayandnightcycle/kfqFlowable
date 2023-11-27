package com.xazktx.flowable.mapper.kfqggk;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FormMapper {

    String getFormJsonById(String ID);

    String getFormJsonByName(String name);

    String getFormJsonIDByPath(String filepath);

}
