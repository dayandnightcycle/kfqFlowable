package com.xazktx.flowable.service;

import com.xazktx.flowable.mapper.kfqggk.SEC_LOGMapper;
import com.xazktx.flowable.model.HomePage;
import com.xazktx.flowable.model.SEC_LOG;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SEC_LOGService {

    @Resource
    private SEC_LOGMapper sec_logMapper;

    public int insertSEC_LOG(SEC_LOG sec_log) {
        return sec_logMapper.insertSEC_LOG(sec_log);
    }

    public List<HomePage> ywmc(List<String> list) {
        return sec_logMapper.ywmc(list);
    }

}
