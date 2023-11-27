package com.xazktx.flowable.service;

import com.xazktx.flowable.mapper.flowable.ActFoFormResourceMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ActFoFormResourceService {

    @Resource
    private ActFoFormResourceMapper actFoFormResourceMapper;

    public String getFormKeyByName(String name) throws Exception {
        return actFoFormResourceMapper.selectJosnByName(name)
                .getRESOURCE_BYTES_()
                .split("^\\{\"key\":\"|\",\"name\":\"")[1];
    }
}
