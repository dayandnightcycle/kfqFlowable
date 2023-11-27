package com.xazktx.flowable.mapper.kfqsxk;

import com.xazktx.flowable.model.DJ_SJD;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DJ_SJDMapper {

    DJ_SJD selectDJ_SJDBySLBH(String SLBH);

    String djdl(String SLBH);
    String lcmc(String SLBH);

    String djdlzzd(String SLBH);

    Integer djdldy(String SLBH);

}
