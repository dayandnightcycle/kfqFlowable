package com.xazktx.flowable.controller;

import cn.hutool.json.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.xazktx.flowable.base.BaseController;
import com.xazktx.flowable.base.BaseSEC_LOGAnnotation;
import com.xazktx.flowable.base.Result;
import com.xazktx.flowable.base.SEC_LOGAnnotation;
import com.xazktx.flowable.mapper.flowable.ActRuTaskMapper;
import com.xazktx.flowable.model.Lcmc;
import com.xazktx.flowable.model.Page;
import com.xazktx.flowable.service.ActDeModelService;
import com.xazktx.flowable.service.ActFoFormResourceService;
import com.xazktx.flowable.service.WFM_ACTINSTService;
import com.xazktx.flowable.util.AuditUtils;
import com.xazktx.flowable.util.ModelToMapUtils;
import com.xazktx.flowable.util.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.idm.api.User;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.hutool.json.JSONUtil.parseObj;
import static com.xazktx.flowable.base.ResMessage.ACTIVATE;
import static com.xazktx.flowable.base.ResMessage.ERROR_PARAM;
import static com.xazktx.flowable.base.ResMessage.ERROR_THIRD;
import static com.xazktx.flowable.base.ResMessage.ERROR_UNAUTH;
import static com.xazktx.flowable.base.ResMessage.FAIL;
import static com.xazktx.flowable.base.ResMessage.SUSPENDED;
import static com.xazktx.flowable.util.ConvertUtils.convert;
import static com.xazktx.flowable.util.ConvertUtils.convert2;
import static com.xazktx.flowable.util.UserUtils.getUserId;

/**
 * 预告土地登记
 * key:YGTDDJ
 * 收件环节----------------(1.安世杰, 2.霍凯星, 3.魏明)
 * 受理环节----------------(1.安世杰, 2.霍凯星, 3.魏明)
 * 收费环节----------------(1.王雅倩, 2.张喆)
 * 初审环节----------------(1.万超, 2.杨帆)
 * 登簿缮证----------------(1.万超, 2.杨帆)
 * 发证环节----------------(1.安世杰)
 * 归档环节----------------(1.苏斐)
 */

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("YGTDDJ")
public class YGTDDJFlowableController extends BaseController {

    private static final String endActivityId = "sid-3ADD072C-ECD3-4B71-B8BB-B7D1D4324D0D";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private ActDeModelService actDeModelService;

    @Autowired
    private ActFoFormResourceService actFoFormResourceService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private ActRuTaskMapper actRuTaskMapper;

    @Autowired
    private WFM_ACTINSTService wfm_actinstService;

    /**
     * byte部署流程
     * @param key 流程Key
     * @return 部署流程
     */
    @GetMapping("qidong2b")
    public Result<String> qiDongb(String key) {
        Deployment deploy;

        try {
            byte[] bytes = actDeModelService.getXml(key);
            String processName = key + ".bpmn20.xml";
            deploy = repositoryService.createDeployment()
                    .addBytes(processName, bytes)
                    .name(key)
                    .deploy();
        } catch (JsonProcessingException e) {
            log.error("部署失败", e);
            return error(ERROR_PARAM);
        }

        log.info("deploy.getId() = {} --- deploy.getName() = {} --- deploy.getParentDeploymentId() = {}",
                deploy.getId(), deploy.getName(), deploy.getParentDeploymentId());
        return success("部署成功");
    }

    /**
     * xml部署流程
     */
    @GetMapping("qidong2")
    public Result<String> qiDong() {
        Deployment deploy;

        try {
            deploy = repositoryService.createDeployment()
                    .addClasspathResource("processes/YGTDDJ.bpmn20.xml")
                    .name("抵押登记1")
                    .deploy();
        } catch (Exception e) {
            log.error("部署失败", e);
            return error(ERROR_PARAM);
        }

        log.info("deploy.getId() = {} --- deploy.getName() = {}", deploy.getId(), deploy.getName());
        return success("部署成功");
    }

    /**
     * 查询流程定义的信息
     * @param deploymentId 部署 ID
     * @return 流程定义信息
     */
    @GetMapping("cx2")
    public Result<JSONArray> cx(String deploymentId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                .singleResult();

        log.info("processDefinition.getDeploymentId() = {}", processDefinition.getDeploymentId());
        JSONArray array = new JSONArray();
        array.add(parseObj(processDefinition, true));
        return success(array);
    }

    /**
     * 删除流程定义
     * @param deploymentId 部署ID
     * @return
     */
    @DeleteMapping(value = "sc2")
    public Result<String> sc(String deploymentId) {
        try {
            repositoryService.deleteDeployment(deploymentId, true);
        } catch (Exception e) {
            log.error("删除失败", e);
            return error(ERROR_PARAM);
        }
        return success("已删除");
    }

    /**
     * 启动流程实例
     * @param key 流程Key
     * @param name 表单名
     * @param user 用户ID
     * @return
     */
    @BaseSEC_LOGAnnotation(value = {
            @SEC_LOGAnnotation(value = "业务创建-受理号:{SLBH}、业务类型:{DJDL}的业务，于{DATE}被创建！创建人:{USERNAME}",
                    remark = "业务监察", opertype = "创建")
    })
    @PostMapping("qd2")
    public Result<JSONArray> qd(String key, String name, String slbh, String user) {

        Map<String, Object> variables = new HashMap<>();

        variables.put("slbh", slbh);
        variables.put("sjForm", name);

        ProcessInstance processInstance;
        try {
                        Authentication.setAuthenticatedUserId(user);
            processInstance = runtimeService.startProcessInstanceByKey(key, variables);
            Authentication.setAuthenticatedUserId(null);
        } catch (Exception e) {
            log.error("启动失败", e);
            return error(ERROR_PARAM);
        }

        log.info("processInstance.getProcessDefinitionId() = {} --- processInstance.getId() = {}",
                processInstance.getProcessDefinitionId(), processInstance.getId());

        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();

        taskService.claim(task.getId(), user);

        JSONArray array = new JSONArray();
        array.add(parseObj(task, true));
        return success(array);
    }

    /**
     * 获取已签收的任务
     * @param userId 用户ID
     * @param processDefinitionKey 流程Key
     * @param pageNumber 页码
     * @param pageSize 每页显示多少条
     * @return 任务列表
     */
    @GetMapping("cscx2")
    public Result<JSONArray> cscx(String userId, String processDefinitionKey,
                                  Integer pageNumber, Integer pageSize) {
        if (userId.equals("undefined")) {
            return error(ERROR_PARAM);
        }

        TaskQuery taskQuery = taskService.createTaskQuery()
                .orderByTaskCreateTime()
                .desc()
                .processDefinitionKey(processDefinitionKey)
                .or()
                .taskCandidateUser(userId)
                .taskAssignee(userId)
                .endOr()
                .active();

        List<Task> list = taskQuery
                .listPage((pageNumber - 1) * pageSize, pageNumber * pageSize);

        long count = taskQuery.count();

        JSONArray array = new JSONArray();
        list.forEach(l -> array.add(parseObj(l, true)));
        Page page = new Page(count);
        array.add(page);
        return success(array);
    }

    /**
     * 根据登录的用户查询对应的所有的任务
     * @param userId 用户ID
     * @param processDefinitionKey 流程Key
     * @param pageNumber 页码
     * @param pageSize 每页显示多少条
     * @return 任务列表
     */
    @GetMapping("cssyrw")
    public Result<JSONArray> cssyrw(String userId, String processDefinitionKey,
                                    Integer pageNumber, Integer pageSize) {
        if (userId.equals("undefined")) {
            return error(ERROR_PARAM);
        }

        TaskQuery taskQuery = taskService.createTaskQuery()
                .orderByTaskCreateTime()
                .desc()
                .processDefinitionKey(processDefinitionKey)
                .or()
                .taskCandidateUser(userId)
                .taskAssignee(userId)
                .endOr()
                .active();

        List<Task> list = taskQuery
                .listPage((pageNumber - 1) * pageSize, pageNumber * pageSize);

        long count = taskQuery.count();

        JSONArray array = new JSONArray();
        list.forEach(l -> array.add(parseObj(l, true)));
        Page page = new Page(count);
        array.add(page);
        return success(array);
    }

    /**
     * 根据登录的用户查询对应的可以签收的任务
     * @param userId 用户ID
     * @param processDefinitionKey 流程Key
     * @param pageNumber 页码
     * @param pageSize 每页显示多少条
     * @return 任务列表
     */
    @GetMapping("cscxs2")
    public Result<JSONArray> cscxs(String userId, String processDefinitionKey,
                                   Integer pageNumber, Integer pageSize) {
        if (userId.equals("undefined")) {
            return error(ERROR_PARAM);
        }

        TaskQuery taskQuery = taskService.createTaskQuery()
                .orderByTaskCreateTime()
                .desc()
                .processDefinitionKey(processDefinitionKey)
                .taskCandidateUser(userId)
                .active();

        List<Task> list = taskQuery
                .listPage((pageNumber - 1) * pageSize, pageNumber * pageSize);

        long count = taskQuery.count();

        List<Map<String, Object>> list1 = new ArrayList<>();
        JSONArray array = new JSONArray();
        list.forEach(l -> {
            Map<String, Object> map = ModelToMapUtils.transBeanToMap(l);
            list1.add(map);
            array.add(parseObj(l, true));
        });
        Map<String, Object> map = new HashMap<>(1, 1f);
        Page page = new Page(count);
        array.add(page);
        map.put("count", count);
        return success(array);
    }

    /**
     * 根据登录的用户查询对应已挂起的任务
     * @param userId 用户ID
     * @param processDefinitionKey 流程Key
     * @param pageNumber 页码
     * @param pageSize 每页显示多少条
     * @return 任务列表
     */
    @GetMapping("cxgqrw")
    public Result<JSONArray> cxgqrw(String userId, String processDefinitionKey,
                                    Integer pageNumber, Integer pageSize) {
        if (userId.equals("undefined")) {
            return error(ERROR_PARAM);
        }

        TaskQuery taskQuery = taskService.createTaskQuery()
                .orderByTaskCreateTime()
                .desc()
                .processDefinitionKey(processDefinitionKey)
                .taskAssignee(userId)
                .suspended();

        List<Task> list = taskQuery
                .listPage((pageNumber - 1) * pageSize, pageNumber * pageSize);

        long count = taskQuery.count();

        JSONArray array = new JSONArray();
        list.forEach(l -> array.add(parseObj(l, true)));
        Page page = new Page(count);
        array.add(page);
        return success(array);
    }

    /**
     * 签收任务
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return
     */
    @PostMapping("qsrw")
    public Result<String> qsrw(String taskId, String userId, HttpServletRequest request) {
        String userId1 = null;
        try {
            userId1 = getUserId(request);
        } catch (Exception e) {
            log.error("Token错误", e);
        }

        try {
            taskService.claim(taskId, userId1);

            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            wfm_actinstService.setWFM_ACTINST(task, userId1, "待完成");

            return success("已签收");
        } catch (Exception e) {
            log.error("签收失败", e);
        }
        return error(ERROR_PARAM);
    }

    /**
     * 退回任务
     * @param taskId 任务ID
     * @return
     */
    @GetMapping("thrw")
    public Result<String> thrw(String taskId, HttpServletRequest request) {
        String userId1 = null;
        try {
            userId1 = getUserId(request);
        } catch (Exception e) {
            log.error("Token错误", e);
        }

        try {
            taskService.unclaim(taskId);

            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            wfm_actinstService.setWFM_ACTINST(task, userId1, "待签收");

            return success("已退回");
        } catch (Exception e) {
            log.error("退回失败", e);
        }
        return error(ERROR_PARAM);
    }

    /**
     * 获取可转办用户
     * @param taskId 任务ID
     * @return
     */
    @GetMapping("zbwrUsers")
    public Result<JSONArray> zbrwUsers(String taskId) {
        JSONArray array = new JSONArray();
        List<User> userList = new ArrayList<>();

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        String processDefinitionId = task.getProcessDefinitionId();
        String processInstanceId = task.getProcessInstanceId();

        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processDefinitionId(processDefinitionId)
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceEndTime()
                .desc()
                .list();

        for (HistoricActivityInstance historicActivityInstance : list) {
            String activityName = historicActivityInstance.getActivityName();
            if (activityName != null && !activityName.equals("通过") && !activityName.equals("开始") &&
                    !activityName.equals("结束") && !activityName.equals("拒绝")) {
                String convert = convert(activityName);

                List<String> strings = new ArrayList<>();
                strings.add(convert);

                Map<String, Object> variables = taskService.getVariables(taskId, strings);
                for (String userId : variables.get(convert).toString().split(",")) {
                    User user = identityService.createUserQuery()
                            .userId(userId)
                            .singleResult();
                    userList.add(user);
                }
            }
            break;
        }
        userList.forEach(l -> array.add(parseObj(l, true)));
        return success(array);
    }

    /**
     * 转办任务
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return
     */
    @GetMapping("zbrw")
    public Result<String> zbrw(String taskId, String userId) {
        try {
            taskService.unclaim(taskId);

            taskService.claim(taskId, userId);

            return success("已转办");
        } catch (FlowableException e) {
            log.error("无法领取暂停的任务", e);
            return error(ERROR_UNAUTH);
        } catch (Exception e) {
            log.error("转办失败", e);
        }
        return error(ERROR_PARAM);
    }

    /**
     * 获取已完成流程以及对应的处理人
     * @param taskId 任务ID
     * @return
     */
    @GetMapping("lcmc")
    public Result<JSONArray> getlcmc(String taskId, HttpServletRequest request) {
        JSONArray array = new JSONArray();
        List<Lcmc> lcmc = new ArrayList<>();
        String processInstanceId;
        User user;

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        try {

            processInstanceId = task.getProcessInstanceId();

            for (HistoricTaskInstance historicTaskInstance : historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .orderByHistoricTaskInstanceEndTime()
                    .asc()
                    .list()) {
                try {
                    user = identityService.createUserQuery()
                            .userId(historicTaskInstance.getAssignee())
                            .singleResult();
                } catch (Exception e) {
                    String token = request.getHeader("Authorization");
                    if (token == null || !token.startsWith("Bearer ")) {
                        return error(ERROR_PARAM);
                    }
                    String userId;
                    token = token.substring(7);
                    try {
                        userId = TokenUtils.getUserId(token);
                    } catch (Exception exception) {
                        log.error("Token 错误", exception);
                        return error(ERROR_THIRD);
                    }
                    user = identityService.createUserQuery()
                            .userId(userId)
                            .singleResult();
                }
                String firstName = user.getFirstName() == null ? "" : user.getFirstName();
                String lastName = user.getLastName() == null ? "" : user.getLastName();

                lcmc.add(new Lcmc(historicTaskInstance.getName(), lastName + firstName));
            }
        } catch (Exception e) {
            for (HistoricTaskInstance historicTaskInstance : historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(historyService.createHistoricTaskInstanceQuery()
                            .taskId(taskId)
                            .singleResult()
                            .getProcessInstanceId())
                    .finished()
                    .orderByHistoricTaskInstanceEndTime()
                    .asc()
                    .list()) {
                user = identityService.createUserQuery()
                        .userId(historicTaskInstance.getAssignee())
                        .singleResult();
                String firstName = user.getFirstName() == null ? "" : user.getFirstName();
                String lastName = user.getLastName() == null ? "" : user.getLastName();

                lcmc.add(new Lcmc(historicTaskInstance.getName(), lastName + firstName));
            }
        }

        List<Lcmc> newList = lcmc.stream()
                .distinct()
                .collect(Collectors.toList());

        newList.forEach(l -> array.add(parseObj(l, true, true)));
        return success(array);
    }

    /**
     * 审核任务
     * @param variate yes ---> 通过, 其它驳回
     * @param taskId  任务ID
     * @param taskName :
     * <li>收件      ---> sj</li>
     * <li>受理      ---> sl</li>
     * <li>收费      ---> sf</li>
     * <li>初审      ---> cs</li>
     * <li>登簿缮证   ---> dbsz</li>
     * <li>发证      ---> fz</li>
     * <li>归档      ---> gd</li>
     * @param users 下一步的候选用户组名
     * <li>收件      ---> sjusers</li>
     * <li>受理      ---> slusers</li>
     * <li>收费      ---> sfusers</li>
     * <li>初审      ---> csusers</li>
     * <li>登簿缮证   ---> dbszusers</li>
     * <li>发证      ---> fzusers</li>
     * <li>归档      ---> gdusers</li>
     * @param user 当前登录用户
     * @param name 表单名
     * @return
     */
    @BaseSEC_LOGAnnotation(value = {
            @SEC_LOGAnnotation(value = "业务结束-受理号:{SLBH}、业务名称:{YWMC}的业务，已于{DATE}正常结束！结束执行人:{USERNAME}",
                    opertype = "结束", remark = "业务监察", mdlname = "{WrkID}")
    })
    @GetMapping("audit")
    public Result<JSONArray> audit(String variate, String taskId, String taskName,
                                   String users, String user, String name, HttpServletRequest request) {

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        String userId1;
        try {
            userId1 = getUserId(request);
            user = getUserId(request);
        } catch (Exception e) {
            log.error("Token错误", e);
            return null;
        }
        try {
            taskService.claim(taskId, userId1);
        } catch (Exception e) {
        }

        Boolean audit;

        if (!StringUtils.hasText(name)) {

            try {
                audit = AuditUtils.audit(variate, taskId, taskName);
            } catch (FlowableObjectNotFoundException e) {
                log.error("任务ID错误", e);
                return error(ERROR_PARAM);
            } catch (FlowableException e) {
                log.error("无法完成暂停的任务", e);
                return error(ERROR_UNAUTH);
            } catch (Exception e) {
                log.error("其他错误", e);
                return error(ERROR_PARAM);
            }

        } else {

            try {
                audit = AuditUtils.audit(variate, taskId, taskName, users, name);
            } catch (FlowableObjectNotFoundException e) {
                log.error("任务ID错误", e);
                return error(ERROR_PARAM);
            } catch (FlowableException e) {
                log.error("无法完成暂停的任务", e);
                return error(ERROR_PARAM);
            } catch (Exception e) {
                log.error("其他错误", e);
                return error(ERROR_PARAM);
            }

        }
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        Task task1 = null;
        JSONArray array = new JSONArray();
        if (audit) {
            if (user != null) {
                task1 = taskService.createTaskQuery()
                        .processInstanceId(task.getProcessInstanceId())
                        .taskCandidateUser(userId1)
                        .singleResult();
            }

            if (task1 == null) {
                array.add(parseObj(task, true));
                objectObjectHashMap.put("nextTaskName", task.getName());
                array.add(objectObjectHashMap);
                try {
                    runtimeService.createProcessInstanceQuery()
                            .processInstanceId(task.getProcessInstanceId())
                            .singleResult()
                            .getId();
                } catch (Exception e) {
                    wfm_actinstService.setWFM_ACTINST(task, userId1, "已完成");
                }
                return success(ERROR_UNAUTH, array);
            }

            array.add(parseObj(task1, true));
            log.info("通过{}", task.getId());
            wfm_actinstService.setWFM_ACTINST(task1, userId1, "待接收");
        } else {
            for (HistoricTaskInstance historicTaskInstance : historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .orderByHistoricTaskInstanceEndTime()
                    .asc()
                    .list()) {
                if (convert2(taskName).equals(historicTaskInstance.getName())) {
                    historyService.deleteHistoricTaskInstance(historicTaskInstance.getId());
                }
            }
            historyService.deleteHistoricTaskInstance(taskId);
            task1 = taskService.createTaskQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            task.setCategory("退回");
            historyService.deleteHistoricTaskInstance(task1.getId());
            try {
                taskService.saveTask(task);
            } catch (Exception ignored) {
            }
            log.info("退回{}", task.getId());
            wfm_actinstService.setWFM_ACTINST(task1, userId1, "待接收");
        }
        String nextTaskName = task1.getName();
        objectObjectHashMap.put("nextTaskName", nextTaskName);
        array.add(objectObjectHashMap);
        return success(array);
    }

    /**
     * 强制结束任务
     * @param executionId 执行编号
     * @return
     */
    @GetMapping("/qzjs")
    public Result<String> qzjs(String executionId) {
        List<String> executionIds = new ArrayList<>();
        executionIds.add(executionId);

        try {
            runtimeService.createChangeActivityStateBuilder()
                    .moveExecutionsToSingleActivityId(executionIds, endActivityId)
                    .changeState();
        } catch (Exception e) {
            log.error("强制结束失败, 请检查id", e);
            error(ERROR_PARAM);
        }
        return success("任务已强制结束");
    }

    /**
     * 挂起流程实例
     * @param processInstanceId 实例定义 ID
     * @return
     */
    @GetMapping("/gqrw")
    public Result<String> gqrw(String processInstanceId) {

        try {
            runtimeService.suspendProcessInstanceById(processInstanceId);
        } catch (FlowableObjectNotFoundException e) {
            log.error("找不到这样的 processInstance:" + processInstanceId);
            return error(ERROR_PARAM);
        } catch (FlowableException e) {
            log.error("流程实例已经处于暂停状态:" + processInstanceId);
            return error(FAIL);
        }

        return success(SUSPENDED);
    }

    /**
     * 激活流程实例
     * @param processInstanceId 实例定义 ID
     * @return
     */
    @GetMapping("/jhrw")
    public Result<String> jhrw(String processInstanceId) {

        try {
            runtimeService.activateProcessInstanceById(processInstanceId);
        } catch (FlowableObjectNotFoundException e) {
            log.error("找不到这样的 processInstance:" + processInstanceId);
            return error(ERROR_PARAM);
        } catch (FlowableException e) {
            log.error("流程实例已经处于活动状态:" + processInstanceId);
            return error(FAIL);
        }

        return success(ACTIVATE);
    }

    /**
     * 获取流程任务的历史列表
     * @param processDefinitionKey 流程KEY
     * @return 流程任务的历史数据
     */
    @GetMapping("ls2")
    public Result<JSONArray> ls(String processDefinitionKey) {

        List<HistoricTaskInstance> list = new ArrayList<>();
        historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .finished()
                .orderByProcessInstanceEndTime()
                .desc()
                .listPage(0, 20)
                .forEach(HistoricProcessInstance -> {
                    list.add(historyService.createHistoricTaskInstanceQuery()
                            .processInstanceId(HistoricProcessInstance.getId())
                            .orderByHistoricTaskInstanceEndTime()
                            .desc()
                            .listPage(0, 1)
                            .get(0)
                    );
                });

        JSONArray array = new JSONArray();
        list.forEach(HistoricTaskInstance -> array.add(parseObj(HistoricTaskInstance, true, true)));
        return success(array);
    }

}
