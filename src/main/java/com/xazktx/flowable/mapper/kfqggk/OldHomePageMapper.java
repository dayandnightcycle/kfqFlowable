package com.xazktx.flowable.mapper.kfqggk;

import com.xazktx.flowable.model.HomePage;
import com.xazktx.flowable.model.Lcmc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OldHomePageMapper {

    List<HomePage> oldhomepagepro(@Param("SLBH") String SLBH, @Param("YWMC") String YWMC, @Param("YWZL") String YWZL, @Param("pageNumber") Integer pageNumber, @Param("pageSize") Integer pageSize);

    HomePage oldhomepageact(@Param("SLBH") String SLBH);

    Long oldcount(@Param("SLBH") String SLBH, @Param("YWMC") String YWMC, @Param("YWZL") String YWZL);

    List<Lcmc> lcmc(@Param("SLBH") String SLBH);

    String lcmc1(@Param("SLBH") String SLBH);

}
