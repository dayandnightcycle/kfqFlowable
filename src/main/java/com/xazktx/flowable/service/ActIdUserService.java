package com.xazktx.flowable.service;

import com.xazktx.flowable.base.BizException;
import com.xazktx.flowable.mapper.flowable.ActIdUserMapper;
import com.xazktx.flowable.model.ActIdUser;
import com.xazktx.flowable.util.CodeUtils;
import com.xazktx.flowable.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActIdUserService {

    @Autowired
    private ActIdUserMapper actIdUserMapper;

    public ActIdUser findById(String id) {
        return actIdUserMapper.selectByPrimaryKey(id);
    }

    public ActIdUser findByName(String name) {
        return actIdUserMapper.selectByName(name);
    }

    public String login(String username, String password) throws BizException {
        ActIdUser user = actIdUserMapper.selectByPrimaryKey(username);
        if (user == null) {
            throw new BizException(-3, "用户名或密码错误");
        }

        if (!CodeUtils.bcryptCompare(user.getPwd(), password)) {
            throw new BizException(-1, "用户名或密码错误");
        }

        return TokenUtils.getToken(username);
    }

    public List<String> gszyUsers(){
        return actIdUserMapper.gszyUsers();
    }

    public List<String> xxzxUsers(){
        return actIdUserMapper.xxzxUsers();
    }

}
