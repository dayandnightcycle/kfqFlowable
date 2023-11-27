package com.xazktx.flowable.service;

import com.xazktx.flowable.mapper.kfqsxk.DJ_SJDMapper;
import com.xazktx.flowable.model.DJ_SJD;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DJ_SJDService {

    @Resource
    private DJ_SJDMapper dj_sjdMapper;

    public DJ_SJD selectDJ_SJDBySLBH(String SLBH){
        return dj_sjdMapper.selectDJ_SJDBySLBH(SLBH);
    }

    public String selectDjdl(String SLBH){
        return dj_sjdMapper.djdl(SLBH);
    }    public String selectlcmc(String SLBH){
        return dj_sjdMapper.lcmc(SLBH);
    }

    public String selectDjdlzzd(String SLBH){
        return dj_sjdMapper.djdlzzd(SLBH);
    }

    public Integer djdldy(String SLBH){
        return dj_sjdMapper.djdldy(SLBH);
    }

}
