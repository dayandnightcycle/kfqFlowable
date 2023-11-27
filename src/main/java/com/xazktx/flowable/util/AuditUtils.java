package com.xazktx.flowable.util;

import com.xazktx.flowable.service.ActFoFormResourceService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AuditUtils {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ActFoFormResourceService actFoFormResourceService;

    private static AuditUtils auditUtils;

    @PostConstruct
    public void init() {
        auditUtils = this;
    }

    public static Boolean audit(String variate, String taskId, String taskName) throws Exception {
        Map<String, Object> map = new HashMap<>();
        boolean b = false;

        if (variate.equals("yes")) {
            b = true;
        }

        map.put(taskName, b);
        auditUtils.taskService.complete(taskId, map);
        return b;
    }

    public static Boolean audit(String variate, String taskId, String taskName, String users, String name) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String formKey = users.split("users")[0] + "Form";

        boolean b = false;

        if (variate.equals("yes")) {
            b = true;
        }

        map.put(taskName, b);
        map.put(formKey, name);
        auditUtils.taskService.complete(taskId, map);
        return b;
    }

    public static Boolean audit(String variate, String taskId, String taskName,
                                String users, String user, String name) throws Exception {
        String formKey = users.split("users")[0] + "Form";
        Map<String, Object> map = new HashMap<>();
        boolean b = false;

        if (variate.equals("yes")) {
            b = true;
        }

        map.put(taskName, b);
        map.put(users, user);
        map.put(formKey, name);
        auditUtils.taskService
                .complete(taskId, map);

        return b;
    }

    public static Boolean audit(String variate, String taskId, String taskName,
                                String users, String user, String name, String bpmnKey) throws Exception {

        String formKey = users.split("users")[0] + "Form";
        Map<String, Object> map = new HashMap<>();
        boolean b = false;

        if (variate.equals("yes")) {
            b = true;
        }

        map.put(taskName, b);
        map.put(users, user);
        map.put(formKey, name);
        auditUtils.taskService
                .complete(taskId, map);

        String taskId1 = auditUtils.taskService
                .createTaskQuery()
                .orderByTaskCreateTime()
                .desc()
                .processDefinitionKey(bpmnKey)
                .taskCandidateUser(user)
                .list()
                .get(0)
                .getId();

        try {
            auditUtils.taskService.claim(taskId1, user);
        } catch (Exception e) {
            log.error("签收失败", e);
        }
        return b;
    }
}
