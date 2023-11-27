package com.xazktx.flowable.mapper.kfqggk;

import com.xazktx.flowable.model.HomePage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomePageMapper {

    List<HomePage> homepage(@Param("list") List<String> list, @Param("YWMC") String YWMC, @Param("YWZL") String YWZL, @Param("pageNumber") Integer pageNumber, @Param("pageSize") Integer pageSize);

    Long count(@Param("list") List<String> list, @Param("YWMC") String YWMC, @Param("YWZL") String YWZL);

}
