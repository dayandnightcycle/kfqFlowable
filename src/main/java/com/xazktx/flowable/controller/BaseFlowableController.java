package com.xazktx.flowable.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.xazktx.flowable.base.BaseController;
import com.xazktx.flowable.base.BaseSEC_LOGAnnotation;
import com.xazktx.flowable.base.NoNeedLogin;
import com.xazktx.flowable.base.Result;
import com.xazktx.flowable.base.SEC_LOGAnnotation;
import com.xazktx.flowable.base.SEC_LOGErrorAnnotation;
import com.xazktx.flowable.mapper.flowable.ActIdUserMapper;
import com.xazktx.flowable.mapper.flowable.ActRuTaskMapper;
import com.xazktx.flowable.model.HomePage;
import com.xazktx.flowable.model.Lcmc;
import com.xazktx.flowable.model.Page;
import com.xazktx.flowable.service.DJ_SJDService;
import com.xazktx.flowable.service.HomePageService;
import com.xazktx.flowable.service.WFM_ACTINSTService;
import com.xazktx.flowable.util.DiagramUtils;
import com.xazktx.flowable.util.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.flowable.idm.api.UserQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static cn.hutool.json.JSONUtil.parseObj;
import static com.xazktx.flowable.base.ResMessage.ACTIVATE;
import static com.xazktx.flowable.base.ResMessage.BACK;
import static com.xazktx.flowable.base.ResMessage.ERROR_PARAM;
import static com.xazktx.flowable.base.ResMessage.ERROR_THIRD;
import static com.xazktx.flowable.base.ResMessage.FAIL;
import static com.xazktx.flowable.base.ResMessage.SUCCESS;
import static com.xazktx.flowable.base.ResMessage.SUSPENDED;
import static com.xazktx.flowable.util.ConvertUtils.processDefinitionNameConvert1;
import static com.xazktx.flowable.util.UserUtils.getUserId;

@Slf4j
@CrossOrigin
@RestController
public class BaseFlowableController extends BaseController {

    @Autowired
    private DJ_SJDService dj_sjdService;

    @Autowired
    private HomePageService homePageService;

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
    private IdentityService identityService;

    @Autowired
    private ActRuTaskMapper actRuTaskMapper;

    @Autowired
    private WFM_ACTINSTService wfm_actinstService;

    @Autowired
    private ActIdUserMapper actIdUserMapper;

    /**
     * 获取流程任务的历史列表
     * <br/>
     * KEY :
     * <li>转移登记  ---> ZYDJ1</li>
     * <li>首次登记  ---> GYJSYDSYQJFWSYQSCDJDZ</li>
     * <li>查封登记  ---> CFDJ</li>
     * <li>更正登记  ---> GZDJHZM</li>
     * <li>抵押登记  ---> GYJSYDSYQJFWSYQDYDJ</li>
     * @param processDefinitionKey 流程KEY
     * @param pageNumber 页码
     * @param pageSize 每页显示多少条
     * @return 流程任务的历史数据
     */
    @GetMapping("lssj")
    public Result<JSONArray> ls(String processDefinitionKey, int pageNumber, int pageSize) {

        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .finished()
                .orderByProcessInstanceEndTime()
                .desc();

        long count = historicProcessInstanceQuery.count();

        List<HistoricTaskInstance> list = new ArrayList<>();
        historicProcessInstanceQuery.listPage((pageNumber - 1) * pageSize, pageNumber * pageSize)
                .forEach(historicProcessInstance -> {
                    list.add(historyService.createHistoricTaskInstanceQuery()
                            .processInstanceId(historicProcessInstance.getId())
                            .orderByHistoricTaskInstanceEndTime()
                            .desc()
                            .listPage(0, 1)
                            .get(0)
                    );
                });

        JSONArray array = new JSONArray();
        list.forEach(historicTaskInstance -> {
            array.add(parseObj(historicTaskInstance, true, true));
        });

        array.add(new Page(count));

        return success(array);
    }

    /**
     * 根据受理编号查找任务
     * @param SLBH 受理编号
     * @return
     */
    @NoNeedLogin
    @GetMapping("getTaskBySLBH")
    public Result<JSONArray> getTaskBySLBH(String SLBH) {

        Task task = taskService.createTaskQuery()
                .orderByTaskCreateTime()
                .desc()
                .taskDescription(SLBH)
                .active()
                .singleResult();

        JSONArray array = new JSONArray();
        array.add(parseObj(task, true));

        return success(array);
    }

    /**
     * 获取流程任务的日志数据
     * @param processInstanceId 实例定义 ID
     * @return
     */
    @GetMapping("lslog")
    public Result<JSONArray> lslog(String processInstanceId) {

        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();

        JSONArray array = new JSONArray();
        list.forEach(l -> array.add(parseObj(l, true)));
        return success(array);
    }

    /**
     * 根据登录的用户查询对应的所有的任务 新 FLO.WFM_ACTINST 表
     * @param pageNumber 页码
     * @param pageSize 每页显示多少条
     * @param request
     * @param SLBH 受理编号
     * @param YWMC 业务名称
     * @param YWZL 业务坐落
     * @return 任务列表
     */
    @GetMapping("cxsyrw")
    public Result<JSONArray> cssyrw(Integer pageNumber, Integer pageSize, HttpServletRequest request,
                                    String SLBH, String YWMC, String YWZL) {

        String userId;
        try {
            userId = getUserId(request);
        } catch (Exception e) {
            log.error("token 错误", e);
            return error(ERROR_THIRD);
        }

        //公司专用
        List<String> gszyUsers = actIdUserMapper.gszyUsers();
        //信息中心
        List<String> xxzxUsers = actIdUserMapper.xxzxUsers();
        TaskQuery taskQuery;
        if (gszyUsers.stream()
                .anyMatch(gszyUser -> gszyUser.equals(userId))) {
            taskQuery = taskService.createTaskQuery()
                    .processInstanceIdIn(historyService.createHistoricProcessInstanceQuery()
                            .startedBy(userId)
                            .unfinished()
                            .orderByProcessInstanceStartTime()
                            .desc()
                            .listPage(0, 20)
                            .stream()
                            .map(HistoricProcessInstance::getId)
                            .collect(Collectors.toList()))
                    .orderByTaskCreateTime()
                    .desc()
                    .or()
                    .taskCandidateUser(userId)
                    .taskAssignee(userId)
                    .endOr()
                    .active();
        } else if (xxzxUsers.stream()
                .anyMatch(xxzxUser -> xxzxUser.equals(userId))) {
            taskQuery = taskService.createTaskQuery()
                    .orderByTaskCreateTime()
                    .desc()
                    .active();
        } else {
            taskQuery = taskService.createTaskQuery()
                    .orderByTaskCreateTime()
                    .desc()
                    .or()
                    .taskCandidateUser(userId)
                    .taskAssignee(userId)
                    .endOr()
                    .active();
        }

        if (StringUtils.hasText(SLBH)) {
            taskQuery.taskDescriptionLike("%" + SLBH + "%");
        }

        JSONArray array = new JSONArray();

        if (!StringUtils.hasText(YWMC) && !StringUtils.hasText(YWZL)) {
            List<Task> countList = taskQuery.list();

            List<Task> list = taskQuery
                    .listPage((pageNumber - 1) * pageSize, pageNumber * pageSize * 10);

            ArrayList<String> objects = new ArrayList<>();
            ArrayList<String> countObjects = new ArrayList<>();
            countList.forEach(task -> countObjects.add(task.getDescription()));
            list.forEach(task -> objects.add(task.getDescription()));

            List<HomePage> homePages = homePageService.newhomePage(objects, null, null, null, null);
            Future<Long> countFuture = homePageService.newcount(countObjects, null, null);

            for (Task task : list) {
                JSONObject jsonObject = null;
                for (HomePage homePage : homePages) {
                    if (task.getDescription().equals(homePage.getSLBH())) {
                        jsonObject = parseObj(task, true);
                        jsonObject.set("ggk", homePage);
                        break;
                    }
                }
                if (jsonObject != null) {
                    array.add(jsonObject);
                }
                if (array.size() == pageSize) {
                    break;
                }
            }

            Long count = null;
            try {
                count = countFuture.get();
            } catch (InterruptedException e) {
                log.error("中断异常", e);
            } catch (ExecutionException e) {
                log.error("执行异常", e);
            }
            Page page = new Page(count);
            array.add(page);
            return success(array);

        } else {

            List<Task> list = taskQuery.list();
            ArrayList<String> objects = new ArrayList<>();
            list.forEach(task -> {
                objects.add(task.getDescription());
            });

            Future<Long> countFuture = homePageService.newcount(objects, YWMC, YWZL);
            List<HomePage> homePages = homePageService.newhomePage(objects, YWMC, YWZL, pageNumber, pageSize);

            homePages.forEach(ggk -> {
                JSONObject jsonObject = null;
                for (Task task : list) {
                    if (task.getDescription().equals(ggk.getSLBH())) {
                        jsonObject = parseObj(task, true);
                        jsonObject.set("ggk", ggk);
                        break;
                    }
                }

                array.add(jsonObject);
            });

            Long count = null;
            try {
                count = countFuture.get();
            } catch (InterruptedException e) {
                log.error("中断异常", e);
            } catch (ExecutionException e) {
                log.error("执行异常", e);
            }
            Page page = new Page(count);
            array.add(page);
            return success(array);
        }

    }

    /**
     * 旧平台数据
     * @param pageNumber 页码
     * @param pageSize 每页显示多少条
     * @param request
     * @param SLBH 受理编号
     * @param YWMC 业务名称
     * @param YWZL 业务坐落
     * @return 任务列表
     */
    @GetMapping("jptcxsyrw")
    public Result<JSONArray> jptcssyrw(Integer pageNumber, Integer pageSize, HttpServletRequest request,
                                       String SLBH, String YWMC, String YWZL) {

        JSONArray array = new JSONArray();

        Future<Long> countFuture = homePageService.oldcount(SLBH, YWMC, YWZL);
        List<HomePage> homePages = homePageService.oldhomepagepro(SLBH, YWMC, YWZL, pageNumber, pageSize);
        String DJDL;

        for (HomePage hp : homePages) {
            HomePage oldhomepageact = homePageService.oldhomepageact(hp.getSLBH());
            DJDL = dj_sjdService.selectDjdlzzd(hp.getSLBH());
            if ("首次登记".equals(DJDL) || "注销登记".equals(DJDL)) {
                if (dj_sjdService.djdldy(hp.getSLBH()) > 0) {
                    DJDL = "抵押登记";
                }
            }
            hp.setYWMC(hp.getYWMC() + " " + DJDL);
            hp.setBLBZ(oldhomepageact.getBLBZ());
            hp.setName(oldhomepageact.getBLBZ());
            hp.setTJR(oldhomepageact.getTJR());
            hp.setBLZT(oldhomepageact.getBLZT());
        }

        array.add(homePages);

        Long count = null;
        try {
            count = countFuture.get();
        } catch (InterruptedException e) {
            log.error("中断异常", e);
        } catch (ExecutionException e) {
            log.error("执行异常", e);
        }
        Page page = new Page(count);
        array.add(page);
        return success(array);

    }

    /**
     * 根据登录的用户查询对应的待签收任务
     * @param pageNumber 页码
     * @param pageSize 每页显示多少条
     * @param request
     * @param SLBH 受理编号
     * @param YWMC 业务名称
     * @param YWZL 业务坐落
     * @return 任务列表
     */
    @GetMapping("cxsydqsrw")
    public Result<JSONArray> cxsydqsrw(Integer pageNumber, Integer pageSize, HttpServletRequest request,
                                       String SLBH, String YWMC, String YWZL) {

        String userId;
        try {
            userId = getUserId(request);
        } catch (Exception e) {
            log.error("token 错误", e);
            return error(ERROR_THIRD);
        }

        List<String> gszyUsers = actIdUserMapper.gszyUsers();
        List<String> xxzxUsers = actIdUserMapper.xxzxUsers();
        TaskQuery taskQuery;
        if (gszyUsers.stream()
                .anyMatch(gszyUser -> gszyUser.equals(userId))) {
            taskQuery = taskService.createTaskQuery()
                    .processInstanceIdIn(historyService.createHistoricProcessInstanceQuery()
                            .startedBy(userId)
                            .unfinished()
                            .orderByProcessInstanceStartTime()
                            .desc()
                            .list()
                            .stream()
                            .map(HistoricProcessInstance::getId)
                            .collect(Collectors.toList()))
                    .orderByTaskCreateTime()
                    .desc()
                    .taskCandidateUser(userId)
                    .active();
        } else if (xxzxUsers.stream()
                .anyMatch(xxzxUser -> xxzxUser.equals(userId))) {
            taskQuery = taskService.createTaskQuery()
                    .orderByTaskCreateTime()
                    .desc()
                    .taskCandidateUser(userId)
                    .active();
        } else {
            taskQuery = taskService.createTaskQuery()
                    .orderByTaskCreateTime()
                    .desc()
                    .taskCandidateUser(userId)
                    .active();
        }

        if (StringUtils.hasText(SLBH)) {
            taskQuery.taskDescriptionLike("%" + SLBH + "%");
        }

        JSONArray array = new JSONArray();

        if (!StringUtils.hasText(YWMC) && !StringUtils.hasText(YWZL)) {
            List<Task> countList = taskQuery.list();

            List<Task> list = taskQuery
                    .listPage((pageNumber - 1) * pageSize, pageNumber * pageSize * 10);

            ArrayList<String> objects = new ArrayList<>();
            ArrayList<String> countObjects = new ArrayList<>();
            countList.forEach(task -> countObjects.add(task.getDescription()));
            list.forEach(task -> objects.add(task.getDescription()));

            List<HomePage> homePages = homePageService.newhomePage(objects, null, null, null, null);
            Future<Long> countFuture = homePageService.newcount(countObjects, null, null);

            for (Task task : list) {
                JSONObject jsonObject = null;
                for (HomePage homePage : homePages) {
                    if (task.getDescription().equals(homePage.getSLBH())) {
                        jsonObject = parseObj(task, true);
                        jsonObject.set("ggk", homePage);
                        break;
                    }
                }
                if (jsonObject != null) {
                    array.add(jsonObject);
                }
                if (array.size() == pageSize) {
                    break;
                }
            }

            Long count = null;
            try {
                count = countFuture.get();
            } catch (InterruptedException e) {
                log.error("中断异常", e);
            } catch (ExecutionException e) {
                log.error("执行异常", e);
            }
            Page page = new Page(count);
            array.add(page);
            return success(array);

        } else {
            List<Task> list = taskQuery.list();
            ArrayList<String> objects = new ArrayList<>();
            list.forEach(task -> {
                objects.add(task.getDescription());
            });

            Future<Long> countFuture = homePageService.newcount(objects, YWMC, YWZL);
            List<HomePage> homePages = homePageService.newhomePage(objects, YWMC, YWZL, pageNumber, pageSize);

            homePages.forEach(ggk -> {
                JSONObject jsonObject = null;
                for (Task task : list) {
                    if (task.getDescription().equals(ggk.getSLBH())) {
                        jsonObject = parseObj(task, true);
                        jsonObject.set("ggk", ggk);
                        break;
                    }
                }

                array.add(jsonObject);
            });

            Long count = null;
            try {
                count = countFuture.get();
            } catch (InterruptedException e) {
                log.error("中断异常", e);
            } catch (ExecutionException e) {
                log.error("执行异常", e);
            }
            Page page = new Page(count);
            array.add(page);
            return success(array);
        }

    }

    /**
     * 根据登录的用户查询对应的待提交任务
     * @param pageNumber 页码
     * @param pageSize 每页显示多少条
     * @param request
     * @param SLBH 受理编号
     * @param YWMC 业务名称
     * @param YWZL 业务坐落
     * @return 任务列表
     */
    @GetMapping("cxsydtjrw")
    public Result<JSONArray> cxsydtjrw(Integer pageNumber, Integer pageSize, HttpServletRequest request,
                                       String SLBH, String YWMC, String YWZL) {

        String userId;
        try {
            userId = getUserId(request);
        } catch (Exception e) {
            log.error("token 错误", e);
            return error(ERROR_THIRD);
        }

        List<String> gszyUsers = actIdUserMapper.gszyUsers();
        List<String> xxzxUsers = actIdUserMapper.xxzxUsers();
        TaskQuery taskQuery;
        if (gszyUsers.stream()
                .anyMatch(gszyUser -> gszyUser.equals(userId))) {
            taskQuery = taskService.createTaskQuery()
                    .processInstanceIdIn(historyService.createHistoricProcessInstanceQuery()
                            .startedBy(userId)
                            .unfinished()
                            .orderByProcessInstanceStartTime()
                            .desc()
                            .list()
                            .stream()
                            .map(HistoricProcessInstance::getId)
                            .collect(Collectors.toList()))
                    .orderByTaskCreateTime()
                    .desc()
                    .taskAssignee(userId)
                    .active();
        } else if (xxzxUsers.stream()
                .anyMatch(xxzxUser -> xxzxUser.equals(userId))) {
            taskQuery = taskService.createTaskQuery()
                    .orderByTaskCreateTime()
                    .desc()
                    .taskAssigned()
                    .active();
        } else {
            taskQuery = taskService.createTaskQuery()
                    .orderByTaskCreateTime()
                    .desc()
                    .taskAssignee(userId)
                    .active();
        }

        if (StringUtils.hasText(SLBH)) {
            taskQuery.taskDescriptionLike("%" + SLBH + "%");
        }

        JSONArray array = new JSONArray();

        if (!StringUtils.hasText(YWMC) && !StringUtils.hasText(YWZL)) {
            List<Task> countList = taskQuery.list();

            List<Task> list = taskQuery
                    .listPage((pageNumber - 1) * pageSize, pageNumber * pageSize * 10);
            ArrayList<String> objects = new ArrayList<>();
            ArrayList<String> countObjects = new ArrayList<>();
            countList.forEach(task -> countObjects.add(task.getDescription()));
            list.forEach(task -> objects.add(task.getDescription()));

            List<HomePage> homePages = homePageService.newhomePage(objects, null, null, null, null);
            Future<Long> countFuture = homePageService.newcount(countObjects, null, null);

            for (Task task : list) {
                JSONObject jsonObject = null;
                for (HomePage homePage : homePages) {
                    if (task.getDescription().equals(homePage.getSLBH())) {
                        jsonObject = parseObj(task, true);
                        jsonObject.set("ggk", homePage);
                        break;
                    }
                }
                if (jsonObject != null) {
                    array.add(jsonObject);
                }
                if (array.size() == pageSize) {
                    break;
                }
            }

            Long count = null;
            try {
                count = countFuture.get();
            } catch (InterruptedException e) {
                log.error("中断异常", e);
            } catch (ExecutionException e) {
                log.error("执行异常", e);
            }
            Page page = new Page(count);
            array.add(page);
            return success(array);

        } else {
            List<Task> list = taskQuery.list();
            ArrayList<String> objects = new ArrayList<>();
            list.forEach(task -> {
                objects.add(task.getDescription());
            });

            Future<Long> countFuture = homePageService.newcount(objects, YWMC, YWZL);
            List<HomePage> homePages = homePageService.newhomePage(objects, YWMC, YWZL, pageNumber, pageSize);

            homePages.forEach(ggk -> {
                JSONObject jsonObject = null;
                for (Task task : list) {
                    if (task.getDescription().equals(ggk.getSLBH())) {
                        jsonObject = parseObj(task, true);
                        jsonObject.set("ggk", ggk);
                        break;
                    }
                }

                array.add(jsonObject);
            });

            Long count = null;
            try {
                count = countFuture.get();
            } catch (InterruptedException e) {
                log.error("中断异常", e);
            } catch (ExecutionException e) {
                log.error("执行异常", e);
            }
            Page page = new Page(count);
            array.add(page);
            return success(array);
        }

    }

    /**
     * 根据登录的用户查询对应已挂起的任务
     * @param userId 用户ID
     * @param pageNumber 页码
     * @param pageSize 每页显示多少条
     * @return 任务列表
     */
    @GetMapping("cxgqrw")
    public Result<JSONArray> cxgqrw(String userId, Integer pageNumber, Integer pageSize) {
        if (userId.equals("undefined")) {
            return error(ERROR_PARAM);
        }

        TaskQuery taskQuery = taskService.createTaskQuery()
                .orderByTaskCreateTime()
                .desc()
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
     * 获取流程实例状态
     * @param processInstanceId 实例定义 ID
     * @return
     */
    @GetMapping("/rwzt")
    public Result<String> rwzt(String processInstanceId) {

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance.isSuspended()) {
            return success(SUSPENDED);
        }

        return success(ACTIVATE);
    }

    /**
     * 生成流程图
     * @param processInstanceId 流程实例ID  (holidayRequest.getId())
     */
    @RequestMapping(value = "processDiagrams0")
    public void genProcessDiagram0(HttpServletResponse httpServletResponse, String processInstanceId) {

        byte[] buf = new byte[1024];
        int legth;
        try (InputStream inputStream = DiagramUtils.processRecordImage0(processInstanceId);
             OutputStream out = httpServletResponse.getOutputStream()) {
            while ((legth = inputStream.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } catch (NullPointerException e) {
            log.error("IO流为空, 无需关闭");
        } catch (Exception e) {
            log.error("IO异常, 这个资源不能被关闭", e);
        }
    }

    /**
     * 生成流程图
     * @param processInstanceId 流程实例ID  (holidayRequest.getId())
     */
    @RequestMapping(value = "processDiagrams1")
    public void genProcessDiagram1(HttpServletResponse httpServletResponse, String processInstanceId) {

        byte[] buf = new byte[1024];
        int legth;
        try (InputStream inputStream = DiagramUtils.processRecordImage1(processInstanceId);
             OutputStream out = httpServletResponse.getOutputStream()) {
            while ((legth = inputStream.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } catch (NullPointerException e) {
            log.error("IO流为空, 无需关闭");
        } catch (Exception e) {
            log.error("IO异常, 这个资源不能被关闭", e);
        }
    }

    @RequestMapping(value = "processDiagrams2")
    public void genProcessDiagram2(HttpServletResponse httpServletResponse, String processInstanceId) {

        byte[] buf = new byte[1024];
        int legth;
        try (InputStream inputStream = DiagramUtils.processRecordImage2(processInstanceId);
             OutputStream out = httpServletResponse.getOutputStream()) {
            while ((legth = inputStream.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } catch (NullPointerException e) {
            log.error("IO流为空, 无需关闭");
        } catch (Exception e) {
            log.error("IO异常, 这个资源不能被关闭", e);
        }
    }

    /**
     * 获取当前登录用户ID
     * @param request
     * @return
     */
    @PostMapping(value = "getUserId")
    public Result<HashMap<String, String>> getUserId1(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return error(ERROR_PARAM);
        }
        String userId;
        token = token.substring(7);
        try {
            userId = TokenUtils.getUserId(token);
        } catch (Exception exception) {
            return error(ERROR_THIRD);
        }

        User user = identityService.createUserQuery()
                .userId(userId)
                .singleResult();
        String firstName = user.getFirstName() == null ? "" : user.getFirstName();
        String lastName = user.getLastName() == null ? "" : user.getLastName();

        HashMap<String, String> map = new HashMap<>();
        map.put("id", userId);
        map.put("name", lastName + firstName);
        return success(map);
    }

    /**
     * 判断该步骤是否退回
     * @param taskId
     * @return
     */
    @GetMapping("thbz")
    public Result<JSONArray> thbz(String taskId) {

        try {
            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .taskCategory("退回")
                    .singleResult();

            if (task != null) {
                return success(BACK);
            }
        } catch (Exception e) {
            return success(SUCCESS);
        }

        return success(SUCCESS);
    }

    /**
     * 获取当前步骤
     * @param slbh
     * @return
     */
    @GetMapping("dqlcmc")
    public Result<JSONArray> dqlcmc(String slbh) {
        String name = null;
        for (Task task : taskService.createTaskQuery()
                .taskDescription(slbh)
                .orderByTaskCreateTime()
                .desc()
                .listPage(0, 1)) {
            name = task.getName();
        }
        Lcmc lcmc = new Lcmc(name, null);
        JSONArray array = new JSONArray();
        array.add(lcmc);
        return success(array);
    }

    /**
     * 获取当前task
     * @param slbh
     * @param name 步骤名
     * @return
     */
    @GetMapping("dqlc")
    public Result<JSONArray> dqlcmc(String slbh, String name) {
        Task task1 = taskService.createTaskQuery()
                .taskDescription(slbh)
                .taskName(name)
                .singleResult();
        JSONArray array = new JSONArray();
        array.add(task1);
        return success(array);
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

        ArrayList<String> list = new ArrayList<>();
        list.add("退回");

        try {

            processInstanceId = task.getProcessInstanceId();

            for (HistoricTaskInstance historicTaskInstance : historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    //TODO
                    .processCategoryNotIn(list)
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
                    //TODO
                    .processCategoryNotIn(list)
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
//TODO
//        List<Lcmc> newList = lcmc.stream()
//                .distinct()
//                .collect(Collectors.toList());
//TODO 放开这行, 注掉下一行
//        newList.forEach(l -> array.add(parseObj(l, true, true)));
        lcmc.forEach(l -> array.add(parseObj(l, true, true)));
        return success(array);
    }

    @NoNeedLogin
    @GetMapping("getProcessDefinitionName")
    public String getProcessDefinitionName(String SLBH) {
        log.info("SLBH = " + SLBH);
        String processDefinitionName = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(taskService.createTaskQuery()
                        .taskDescription(SLBH)
                        .list()
                        .get(0)
                        .getProcessInstanceId())
                .orderByProcessInstanceStartTime()
                .desc()
                .list()
                .get(0)
                .getProcessDefinitionName();
        return processDefinitionNameConvert1(processDefinitionName);
    }

    /**
     * 退回信息
     * @param SLBH
     * @return
     */
    @PostMapping("thxx")
    public Result<Object> thxx(String SLBH) {
        HashMap<String, String> stringStringHashMap = new LinkedHashMap<>();
        Task task = taskService.createTaskQuery().taskDescription(SLBH).listPage(0, 1).get(0);
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        //业务类型
        String ywlx = processDefinitionNameConvert1(processDefinition.getName());
        stringStringHashMap.put("ywlx", ywlx);
        //步骤名称
        String bzmc = task.getName();
        stringStringHashMap.put("bzmc", bzmc);
        //当前状态
        String dqzt = "";
        //主办
        String zb = "";
        String assignee = task.getAssignee();
        UserQuery userQuery = identityService.createUserQuery();
        if (assignee != null) {
            dqzt = "待完成";
            User user = userQuery.userId(assignee).singleResult();
            String first = user.getFirstName() == null ? "" : user.getFirstName();
            String last = user.getLastName() == null ? "" : user.getLastName();
            zb = last + first;
        } else {
            dqzt = "待接收";
            Group group = identityService.createGroupQuery().groupNameLike("%" + bzmc + "%").singleResult();
            zb = group.getName();
        }
        stringStringHashMap.put("dqzt", dqzt);
        stringStringHashMap.put("zb", zb);
        HistoricTaskInstance task1 = historyService.createHistoricTaskInstanceQuery().processInstanceId(task.getProcessInstanceId()).orderByHistoricTaskInstanceStartTime().desc().list().get(1);
        User user = userQuery.userId(task1.getAssignee()).singleResult();
        String first = user.getFirstName() == null ? "" : user.getFirstName();
        String last = user.getLastName() == null ? "" : user.getLastName();
        //可退回步骤
        String kthbz = task1.getName() + "-" + last + first;
        stringStringHashMap.put("kthbz", kthbz);
        //业务名称
        String ywmc = wfm_actinstService.selectYWMCByWFM_ACTINST(SLBH);
        stringStringHashMap.put("ywmc", ywmc);

        return success(stringStringHashMap);
    }

    /**
     * 获取上一步签收人
     * @param SLBH
     * @return
     */
    @PostMapping("thyh")
    public Result<String> thyh(String SLBH) {
        return success(identityService.createUserQuery()
                .userId(historyService.createHistoricTaskInstanceQuery()
                        .processInstanceId(taskService.createTaskQuery()
                                .taskDescription(SLBH)
                                .listPage(0, 1)
                                .get(0)
                                .getProcessInstanceId()
                        ).orderByHistoricTaskInstanceStartTime()
                        .desc()
                        .list()
                        .get(1)
                        .getAssignee()
                ).singleResult()
                .getId()
        );
    }

    /**
     * 删除实例
     * @param SLBH
     * @return
     */
    @BaseSEC_LOGAnnotation(value = {
            @SEC_LOGAnnotation(value = "验证用户登录[廊坊开发区不动产统一登记平台 PRO]成功!", opertype = "删除", remark = "业务监察"),
    }, error = {
            @SEC_LOGErrorAnnotation(value = "◆您选定的身份信息[{USERNAME}]与输入的密码等验证信息不匹配，已拒绝登录[第{COUNT}次]")
    })
    @NoNeedLogin
    @GetMapping("scsl")
    public Result<String> scsl(String SLBH) {
        Integer integer = 0;
        try {
            for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery()
                    .processInstanceId(taskService.createTaskQuery()
                            .taskDescription(SLBH)
                            .singleResult()
                            .getProcessInstanceId())
                    .orderByStartTime().asc().list()) {
                log.info("processInstance.getDeploymentId()" + processInstance.getDeploymentId());
                runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), null);
                integer = wfm_actinstService.deleteWFM_ACTINST(SLBH);
            }
        } catch (Exception e) {
            log.error("删除失败, 旧平台业务", e);
        }
        return success(integer.toString());
    }

    /**
     * 根据受理编号获取 key
     * @param SLBH
     * @return
     */
    @PostMapping("getKey")
    public Result<String> getKey(String SLBH){
        return success(historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(historyService.createHistoricTaskInstanceQuery()
                        .taskDescription(SLBH)
                        .listPage(0,1)
                        .get(0).getProcessInstanceId()
                ).singleResult()
                .getProcessDefinitionKey()
        );
    }

}
