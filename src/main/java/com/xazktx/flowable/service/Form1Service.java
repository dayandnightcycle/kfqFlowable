package com.xazktx.flowable.service;

import com.xazktx.flowable.mapper.kfqggk.FormMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class Form1Service {

    @Resource
    private FormMapper formMapper;

    public String getFormJsonById(String ID) {
        String formJsonById = formMapper.getFormJsonById(ID);
        return formJsonById;
    }

    public String getFormJsonByName(String name) {
        String formJsonByName = formMapper.getFormJsonByName(name);
        return formJsonByName;
    }

    public String getFormJsonIDByPath(String filepath) {
        String formJsonID = formMapper.getFormJsonIDByPath(filepath);
        return formJsonID;
    }
}
