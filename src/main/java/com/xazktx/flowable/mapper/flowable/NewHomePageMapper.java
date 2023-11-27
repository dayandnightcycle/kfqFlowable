package com.xazktx.flowable.mapper.flowable;

import com.xazktx.flowable.model.HomePage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NewHomePageMapper {

    List<HomePage> newhomepage(@Param("list") List<String> list, @Param("YWMC") String YWMC, @Param("YWZL") String YWZL, @Param("pageNumber") Integer pageNumber, @Param("pageSize") Integer pageSize);

    Long newcount(@Param("list") List<String> list, @Param("YWMC") String YWMC, @Param("YWZL") String YWZL);

}
