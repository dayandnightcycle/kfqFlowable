package com.xazktx.flowable.mapper.flowable;

import com.xazktx.flowable.model.WFM_ACTINST;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WFM_ACTINSTMapper {

    int insertWFM_ACTINST(WFM_ACTINST wfm_actinst);

    int countWFM_ACTINST(String SLBH);

    int updateWFM_ACTINST(WFM_ACTINST wfm_actinst);

    int deleteWFM_ACTINST(String SLBH);

    String selectYWMCByWFM_ACTINST(String SLBH);

}
