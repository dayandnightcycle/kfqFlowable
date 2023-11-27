package com.xazktx.flowable.util;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class DiagramUtils {

    private static DiagramUtils diagramUtils;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @PostConstruct
    public void init() {
        diagramUtils = this;
    }

    public static InputStream processRecordImage0(String processInstanceId) throws IOException {
        ProcessInstance pi = diagramUtils.runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (pi == null) {
            return null;
        }

        Task task = diagramUtils.taskService.createTaskQuery()
                .processInstanceId(pi.getId())
                .singleResult();

        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = diagramUtils.runtimeService.createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();

        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = diagramUtils.runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        BpmnModel bpmnModel = diagramUtils.repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = diagramUtils.processEngine.getProcessEngineConfiguration();
        return engconf.getProcessDiagramGenerator()
                .generateDiagram(
                        bpmnModel,
                        "png",
                        activityIds,
                        flows,
                        engconf.getActivityFontName(),
                        engconf.getLabelFontName(),
                        engconf.getAnnotationFontName(),
                        engconf.getClassLoader(),
                        1.0,
                        true
                );
    }

    public static InputStream processRecordImage1(String processInstanceId) throws IOException {
        HistoricProcessInstance hiProcInst = Optional.ofNullable(
                diagramUtils
                        .historyService
                        .createHistoricProcessInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .singleResult()
        ).orElseThrow(() -> new IOException("流程实例没有找到")
        );

        List<String> highLightedActivities = diagramUtils.taskService
                .createTaskQuery()
                .processInstanceId(processInstanceId)
                .list()
                .stream()
                .map(task -> diagramUtils.runtimeService
                        .createExecutionQuery()
                        .executionId(task.getExecutionId())
                        .singleResult()
                        .getActivityId()
                )
                .collect(Collectors.toList());

        List<String> flowIds;
        if (highLightedActivities.size() > 0) {
            flowIds = diagramUtils.runtimeService
                    .createActivityInstanceQuery()
                    .orderByActivityInstanceStartTime()
                    .asc()
                    .orderByActivityInstanceEndTime()
                    .asc()
                    .processInstanceId(processInstanceId)
                    .list()
                    .stream()
                    .filter(hiActInst -> "sequenceFlow".equals(hiActInst.getActivityType()))
                    .map(ActivityInstance::getActivityId)
                    .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        } else {
            flowIds = diagramUtils.historyService
                    .createHistoricActivityInstanceQuery()
                    .orderByHistoricActivityInstanceStartTime()
                    .asc()
                    .orderByHistoricActivityInstanceEndTime()
                    .asc()
                    .processInstanceId(processInstanceId)
                    .list()
                    .stream()
                    .filter(hiActInst -> "sequenceFlow".equals(hiActInst.getActivityType()))
                    .map(HistoricActivityInstance::getActivityId)
                    .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        }

        List<String> highLightedFlows = new CopyOnWriteArrayList<>();

        flowIds.forEach(id -> {
            if (highLightedFlows.contains(id)) {
                int index = highLightedFlows.indexOf(id);
                highLightedFlows.removeIf(id1 -> highLightedFlows.indexOf(id1) > index);
            } else {
                highLightedFlows.add(id);
            }
        });

        ProcessEngineConfiguration engconf = diagramUtils.processEngine.getProcessEngineConfiguration();

        return new DefaultProcessDiagramGenerator().generateDiagram(
                diagramUtils.repositoryService
                        .getBpmnModel(hiProcInst.getProcessDefinitionId()),
                "PNG",
                highLightedActivities,
                highLightedFlows,
                engconf.getActivityFontName(),
                engconf.getLabelFontName(),
                engconf.getAnnotationFontName(),
                null,
                1.0,
                true
        );
    }

    public static InputStream processRecordImage2(String processInstanceId) throws IOException {
        ProcessInstance pi = diagramUtils.runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (pi == null) {
            return null;
        }

        List<HistoricActivityInstance> historyProcess = diagramUtils.historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .list();
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();

        BpmnModel bpmnModel = diagramUtils.repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        for (HistoricActivityInstance hi : historyProcess) {
            String activityType = hi.getActivityType();
            if (activityType.equals("sequenceFlow") || activityType.equals("exclusiveGateway")) {
                flows.add(hi.getActivityId());
            } else if (activityType.equals("userTask") || activityType.equals("startEvent")) {
                activityIds.add(hi.getActivityId());
            }
        }

        List<Task> tasks = diagramUtils.taskService
                .createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
        for (Task task : tasks) {
            activityIds.add(task.getTaskDefinitionKey());
        }

        ProcessEngineConfiguration engConf = ProcessEngines.getDefaultProcessEngine()
                .getProcessEngineConfiguration();
        ProcessDiagramGenerator processDiagramGenerator = engConf.getProcessDiagramGenerator();
        return processDiagramGenerator.generateDiagram(
                bpmnModel,
                "jpg",
                activityIds,
                flows,
                engConf.getActivityFontName(),
                engConf.getLabelFontName(),
                engConf.getAnnotationFontName(),
                engConf.getClassLoader(),
                1.0,
                true
        );
    }

}
