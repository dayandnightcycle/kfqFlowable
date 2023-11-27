package com.xazktx.flowable;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class FlowableApplicationTests {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

//    @Test
//    public void sd() {
//
//        for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery().orderByStartTime().asc().list()) {
//            try {
//                System.out.println(processInstance.getDeploymentId());
//                repositoryService.deleteDeployment(processInstance.getDeploymentId(), true);
//                //delete from FLO.WFM_ACTINST
//            } catch (Exception e) {
//                log.error("删除失败", e);
//            }
//        }
//
//    }

}
