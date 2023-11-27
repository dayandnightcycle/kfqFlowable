package com.xazktx.flowable.mapper.flowable;

import com.xazktx.flowable.model.ActFoFormResource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ActFoFormResourceMapper {

    ActFoFormResource selectJosnByName(String name);

    List<ActFoFormResource> selectJosnName();

    List<ActFoFormResource> selectJosn();

    int deleteFormByName(String formName);
}
