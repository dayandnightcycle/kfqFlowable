package com.xazktx.flowable.mapper.kfqggk;

import com.xazktx.flowable.model.HomePage;
import com.xazktx.flowable.model.SEC_LOG;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SEC_LOGMapper {

    int insertSEC_LOG(SEC_LOG sec_log);

    List<HomePage> ywmc(@Param("list") List<String> list);

}
