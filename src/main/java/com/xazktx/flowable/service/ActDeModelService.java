package com.xazktx.flowable.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xazktx.flowable.mapper.flowable.ActDeModelMapper;
import com.xazktx.flowable.util.XmlUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ActDeModelService {

    @Resource
    private ActDeModelMapper actDeModelMapper;

    public byte[] getXml(String key) throws JsonProcessingException {
        String json = actDeModelMapper.selectJosnByModelKey(key);
        return json != null ? XmlUtils.jsonToXml(json) : null;
    }
}
