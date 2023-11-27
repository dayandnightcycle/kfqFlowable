package com.xazktx.flowable.mapper.flowable;

import com.xazktx.flowable.model.ActIdUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ActIdUserMapper {

    ActIdUser selectByPrimaryKey(String id);

    ActIdUser selectByName(String name);

    List<ActIdUser> findAll();

    List<String> gszyUsers();

    List<String> xxzxUsers();

}
