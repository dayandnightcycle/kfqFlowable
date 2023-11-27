package com.xazktx.flowable.service;

import com.xazktx.flowable.mapper.flowable.WFM_ACTINSTMapper;
import com.xazktx.flowable.mapper.kfqsxk.DJ_QLRGLMapper;
import com.xazktx.flowable.model.DJ_SJD;
import com.xazktx.flowable.model.WFM_ACTINST;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.TaskService;
import org.flowable.idm.api.User;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.xazktx.flowable.util.ConvertUtils.processDefinitionNameConvert1;

@Slf4j
@Service
public class WFM_ACTINSTService {

    @Autowired
    private IdentityService identityService;

    @Resource
    private WFM_ACTINSTMapper wfm_actinstMapper;

    @Resource
    private DJ_QLRGLMapper dj_qlrglMapper;

    @Autowired
    private DJ_SJDService dj_sjdService;

    public Integer insertWFM_ACTINST(WFM_ACTINST wfm_actinst) {
        log.info("wfm_actinst = {}", wfm_actinst);

        return wfm_actinstMapper.insertWFM_ACTINST(wfm_actinst);
    }

    public Integer updateWFM_ACTINST(WFM_ACTINST wfm_actinst) {
        log.info("wfm_actinst = {}", wfm_actinst);

        return wfm_actinstMapper.updateWFM_ACTINST(wfm_actinst);
    }

    public Integer countWFM_ACTINST(String SLBH) {
        log.info("SLBH= {}", SLBH);

        return wfm_actinstMapper.countWFM_ACTINST(SLBH);
    }

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    public Integer setWFM_ACTINST(Task task, String userId, String BLZT) {
        try {
            String SLBH = task.getDescription();
            DJ_SJD dj_sjd = dj_sjdService.selectDJ_SJDBySLBH(SLBH);
            List<String> list = dj_qlrglMapper.selectDJ_QLRGLBySLBH(SLBH);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(" ");
            list.forEach(l -> stringBuilder.append(l).append(" "));
//todo
            String djdl = dj_sjd.getDJDL();
            String djxl = dj_sjd.getDJXL();
            String lclx = dj_sjd.getLCLX();
            String processDefinitionName = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(taskService.createTaskQuery()
                            .taskDescription(SLBH)
                            .list()
                            .get(0)
                            .getProcessInstanceId())
                    .list()
                    .get(0)
                    .getProcessDefinitionName();

            String YWMC = SLBH + stringBuilder + djdl + " " + djxl + " " + lclx + " " + processDefinitionNameConvert1(processDefinitionName);
            String BLBZ = task.getName();
            Date JSSJ = dj_sjd.getSJSJ();
            Date YWXDJZSJ;
            try {
                YWXDJZSJ = dj_sjd.getCNSJ();
            } catch (Exception e) {
                YWXDJZSJ = new Date();
            }
            String PROCNAME = dj_sjd.getLCMC();
            String YWZL = dj_sjd.getZL();

            User user = identityService.createUserQuery()
                    .userId(userId)
                    .singleResult();
            String firstName = user.getFirstName() == null ? "" : user.getFirstName();
            String lastName = user.getLastName() == null ? "" : user.getLastName();
            String TJR = lastName + firstName;

            WFM_ACTINST wfm_actinst = new WFM_ACTINST(UUID.randomUUID().toString(), null, SLBH,
                    YWMC, BLBZ, dj_sjd.getSJR(), TJR, JSSJ, BLZT, YWXDJZSJ, PROCNAME, YWZL);

            Integer integer;
            if (countWFM_ACTINST(SLBH) > 0) {
                integer = updateWFM_ACTINST(wfm_actinst);
            } else {
                integer = insertWFM_ACTINST(wfm_actinst);
            }
            return integer;
        } catch (Exception e) {
            log.error("新表入库", e);
        }
        return null;

    }

    public Integer deleteWFM_ACTINST(String SLBH){
        return wfm_actinstMapper.deleteWFM_ACTINST(SLBH);
    }

    public String selectYWMCByWFM_ACTINST(String SLBH){
        return wfm_actinstMapper.selectYWMCByWFM_ACTINST(SLBH);
    }

}
