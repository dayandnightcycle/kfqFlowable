package com.xazktx.flowable.controller;

import com.alibaba.fastjson2.JSONObject;
import com.xazktx.flowable.base.BaseController;
import com.xazktx.flowable.base.BaseSEC_LOGAnnotation;
import com.xazktx.flowable.base.ResMessage;
import com.xazktx.flowable.base.Result;
import com.xazktx.flowable.base.SEC_LOGAnnotation;
import com.xazktx.flowable.mapper.flowable.ActFoFormResourceMapper;
import com.xazktx.flowable.model.ActFoFormResource;
import com.xazktx.flowable.model.FileTreeNode;
import com.xazktx.flowable.service.DJ_SJDService;
import com.xazktx.flowable.service.Form1Service;
import com.xazktx.flowable.service.HomePageService;
import com.xazktx.flowable.util.CodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.editor.language.json.converter.util.CollectionUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.form.api.FormDeployment;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.xazktx.flowable.base.ResMessage.ERROR_FILE;
import static com.xazktx.flowable.base.ResMessage.ERROR_PARAM;
import static com.xazktx.flowable.base.ResMessage.SUCCESS;
import static com.xazktx.flowable.util.ConvertUtils.DJDLConvert;
import static com.xazktx.flowable.util.ConvertUtils.LCMCConvert;
import static com.xazktx.flowable.util.FileTreeUtils.readZipFile;
import static org.apache.commons.io.FileUtils.readFileToString;

/**
 * ACT_FO_FORM_RESOURCE
 */

@Slf4j
@CrossOrigin
@RestController
public class FormController extends BaseController {

    @Autowired
    private DJ_SJDService dj_sjdService;

    @Autowired
    private Form1Service form1Service;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FormRepositoryService formRepositoryService;

    @Autowired
    private ActFoFormResourceMapper actFoFormResourceMapper;

    @Value("${fromJson}")
    private String fromJson;

    /**
     * 部署form表单
     * @param jsonForm form 表单的 JSON
     * @param name 表单名
     * @return
     */
    @PostMapping("/form")
    public String insertFrom(@RequestBody String jsonForm, String name) {

        List<ActFoFormResource> list = actFoFormResourceMapper.selectJosnName();

        int i = 0;
        for (ActFoFormResource foFormResource : list) {
            try {
                if (foFormResource.getNAME_().equals(name)) {
                    int i1 = actFoFormResourceMapper.deleteFormByName(name);
                    if (i1 <= 0) {
                        return "表单覆盖失败";
                    }
                    i++;
                }
            } catch (Exception e) {
                log.info("空");
            }
        }

        String key = CodeUtils.uuidStr();
        String[] split = jsonForm.split("^\\{");
        String form = "{" + "\"key\":\"" + key + "\"," +
                "\"name\":\"" + name + "\"," + split[1];

        byte[] bytes = form.getBytes(StandardCharsets.UTF_8);
        FormDeployment formDeployment = formRepositoryService.createDeployment()
                .addFormBytes(name, bytes)
                .name(name)
                .parentDeploymentId(key)
                .deploy();
        log.info("formDeployment.getId() = {}", formDeployment.getId());

        if (i == 0) {
            return "表单部署成功";
        }
        return "表单覆盖部署成功";
    }

    /**
     * 获取表单json
     * @param taskName 任务名
     * @param taskId 任务id
     * @return
     */
    @GetMapping("/formJson")
    public String formJson(String taskName, String taskId) {
        List<String> strings = new ArrayList<>();
        strings.add(taskName + "Form");
        Object variable = taskService.getVariables(taskId, strings);
        log.info(variable.toString());
        log.info("\\{" + taskName + "Form=|}");
        String s1 = variable.toString().split("\\{" + taskName + "Form=|}")[1];

        List<ActFoFormResource> actFoFormResources = actFoFormResourceMapper.selectJosn();
        for (ActFoFormResource actFoFormResource : actFoFormResources) {

            if (actFoFormResource.getRESOURCE_BYTES_()
                    .split("^\\{\"key\":\"|\",\"name\":\"")[1].equals(s1)) {
                log.info(s1);
                return actFoFormResource.getRESOURCE_BYTES_();
            }

        }
        return null;
    }

    /**
     * 根据表单名获取表单 JSON
     * @param name
     * @return
     */
    @PostMapping("aa")
    public String dfdf(String name) {
        String resource_bytes_ = "";
        try {
            resource_bytes_ = actFoFormResourceMapper.selectJosnByName(name)
                    .getRESOURCE_BYTES_();
        } catch (Exception e) {
            return "没有该表单";
        }

        return resource_bytes_;
    }

    /**
     * 获取所有表单名
     * @return
     */
    @GetMapping("/form")
    public List<String> findAllFormName() {
        List<ActFoFormResource> list = actFoFormResourceMapper.selectJosnName();
        return list.stream()
                .map(ActFoFormResource::getNAME_)
                .collect(Collectors.toList());
    }

    @PostMapping("/lc")
    public List<String> asdasd() {
        ArrayList<String> list = new ArrayList<>();
        String processInstanceId = "6b6191d4-7762-11ed-a4c2-6c4b90705d2f";

        String definitionId = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult()
                .getProcessDefinitionId();

        List<org.flowable.bpmn.model.Process> processes = repositoryService.getBpmnModel(definitionId)
                .getProcesses();
        log.info("processes size = {}", processes.size());

        for (org.flowable.bpmn.model.Process process : processes) {
            Collection<FlowElement> flowElements = process.getFlowElements();

            if (CollectionUtils.isNotEmpty(flowElements)) {
                for (FlowElement flowElement : flowElements) {
                    if (flowElement instanceof UserTask) {
                        log.info("UserTask = {}", flowElement.getName());
                        list.add(flowElement.getName());
                    }
                }
            }
        }
        return list;
    }

    /**
     * 获取表单JSON
     * @param slbh 受理编号
     * @param name 流程步骤名
     * @param response
     */
    @PostMapping("getFromJson")
    public void getForm(String slbh, String name, HttpServletResponse response) {
        Task task = taskService.createTaskQuery().taskName(name).taskDescription(slbh).singleResult();
        String formKey = task.getFormKey();
        String[] split = formKey.split("\\\\");
//        String filePath = "";
//        for (String s : split) {
//            if (s != null && !s.equals("null")) {
//                filePath += "\\" + s;
//            }
//        }
        StringBuilder filePath = new StringBuilder();
        for (String s : split) {
            if (s != null && !s.equals("null")) {
                filePath.append("\\").append(s);
            }
        }
        try (FileInputStream fileInputStream = new FileInputStream(fromJson + filePath)) {
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private HomePageService homePageService;

    @PostMapping("getHistoryFromJson")
    public void getHistoryFromJson(String slbh, String name, HttpServletResponse response) {
        log.info("name = " + name);
        String formKey = null;
        StringBuilder filePath = new StringBuilder();
        try {
            formKey = historyService.createHistoricTaskInstanceQuery()
                    .taskName(name)
                    .taskDescription(slbh)
                    .list()
                    .get(0)
                    .getFormKey();
        } catch (Exception e) {
            String dj_sjd_djdl = dj_sjdService.selectDjdl(slbh);
//            String dj_sjd_lcmc = dj_sjdService.selectlcmc(slbh);
            String lcmc = homePageService.lcmc1(slbh);
            String[] s = lcmc.split(" ");
            System.out.println("s[s.length -1] = " + s[s.length - 1]);
            String key = LCMCConvert(s[s.length -1]);

            if("".equals(key) && dj_sjd_djdl != null){
                key = DJDLConvert(dj_sjd_djdl);
            }
            log.error("", e);
            if ("核费".equals(name)) {
                name = "收费";
            }
            if ("收费发证".equals(name)) {
                name = "发证";
            }
            if ("收费相关".equals(name)) {
                name = "发证";
            }
            try {
                formKey = historyService.createHistoricTaskInstanceQuery()
                        .taskName(name)
                        .processDefinitionKey(key)
                        .orderByHistoricTaskInstanceStartTime()
                        .desc()
                        .list()
                        .get(0)
                        .getFormKey();
            } catch (IndexOutOfBoundsException ex) {
                log.error("", ex);
            }

        }

        String[] split = formKey.split("\\\\");

        for (String s : split) {
            if (s != null && !s.equals("null")) {
                filePath.append("\\").append(s);
            }
        }

        try (FileInputStream fileInputStream = new FileInputStream(fromJson + filePath)) {
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Value("${backupDir}")
    private String backupDir;

    /**
     * 保存JSON
     * @param Json 表单 JSON
     * @param filePath 文件路径
     */
    @BaseSEC_LOGAnnotation(value = {
            @SEC_LOGAnnotation(value = "保存表单：[{FROMJSONID}]{FROMJSONNAME}",
                    mdlname = "表单管理")
    })
    @PostMapping("saveFormJson")
    public Result<Object> saveForm(@RequestBody String Json, String filePath) {
        try {
            File file = new File(fromJson);
            if (!file.exists()) {
                file.mkdirs();
            }
            File txt = new File(filePath);
            File bd = new File(backupDir);
            if (!bd.isDirectory()) {
                bd.mkdirs();
            }
            String name = txt.getName();
            SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = timestampFormat.format(new Date());
            File txt1 = new File(backupDir + "\\" + name + timestamp + ".json");
            if (!txt.exists()) {
                txt.createNewFile();
            }
            OutputStreamWriter oStreamWriter = new OutputStreamWriter(new FileOutputStream(txt), "UTF-8");
            OutputStreamWriter oStreamWriter1 = new OutputStreamWriter(new FileOutputStream(txt1), "UTF-8");
            oStreamWriter.append(Json);
            oStreamWriter.flush();
            oStreamWriter.close();
            oStreamWriter1.append(Json);
            oStreamWriter1.flush();
            oStreamWriter1.close();
        } catch (FileNotFoundException e) {
            log.error("文件接收失败", e);
            return error(ERROR_FILE);
        } catch (IOException e) {
            log.error("文件接收失败", e);
            return error(ERROR_FILE);
        }
        return success("保存成功");
    }

    /**
     * 接收表单 JSON 文件
     * @param multipartFile 文件
     * @return
     * @throws IOException
     */
    @PostMapping("/saveFormFile")
    public Result<ResMessage> saveFormFile(@RequestParam("file") MultipartFile multipartFile) {

        File file;
        String jsonForm;
        try {
            file = new File("F:\\FromJson");
            if (!file.exists()) {
                file.mkdirs();
            }
            File txt = new File("F:\\FromJson" + "\\" + multipartFile.getOriginalFilename());
            if (!txt.exists()) {
                txt.createNewFile();
            }
            jsonForm = readFileToString(file, "UTF-8");
            byte bytes[] = jsonForm.getBytes();
            FileOutputStream fos = new FileOutputStream(txt);
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            log.error("文件接收失败", e);
            return error(ERROR_FILE);
        }
        return success(SUCCESS);

    }

    /**
     * 表单JSON文件树结构
     * @return
     */
    @GetMapping("fileTree")
    public Result<FileTreeNode> fileTree() {
        FileTreeNode fileTreeNode = readZipFile(new File(fromJson), 0);
        log.info("表单树结构 = {}", JSONObject.toJSONString(fileTreeNode));
        return success(fileTreeNode);
    }

    /**
     * 读取表单JSON文件
     * @return
     */
    @GetMapping("getJson")
    public void getJson(String filePath, HttpServletResponse response) {
        String fromJson;
        try {
            fromJson = filePath.split("FromJson")[1];
        } catch (Exception e) {
            fromJson = filePath;
        }
        filePath = this.fromJson + fromJson;
        log.info("filePath = " + filePath);
        File file = new File(filePath);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取表单Json存储路径
     * @return
     */
    @GetMapping("getFromPath")
    public Result<String> getFromPath(HttpServletRequest request) {
        return success(fromJson);
    }

    @GetMapping("getFromByName")
    public Result<String> asd(String fileName) {
        FileTreeNode fileTreeNode = readZipFile(new File(fromJson), 0);

        String filePath = "";
        for (FileTreeNode nextNode : fileTreeNode.getNextNodes()) {
            if (nextNode.getNextNodes() != null) {
                filePath = getFilePath(nextNode, fileName);
                if (StringUtils.hasText(filePath)) {
                    return success(filePath);
                }
            }
        }
        return error(ERROR_PARAM);
    }

    public String getFilePath(FileTreeNode node, String fileName) {
        for (FileTreeNode nextNode : node.getNextNodes()) {
            if ((fileName + ".json").equals(nextNode.getName())) {
                return nextNode.getFile().toString();
            }
            if (nextNode.getNextNodes() != null) {
                String filepath = getFilePath(nextNode, fileName);
                return filepath;
            }
        }
        return null;
    }

    @GetMapping("getFormJsonById")
    public void getFormJsonById(String id, HttpServletResponse response) {
        String formJsonById = form1Service.getFormJsonById(id);
        String filePath = fromJson + "\\" + formJsonById;
        File file = new File(filePath);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("getFormJsonByName")
    public void getFormJsonByName(String name, HttpServletResponse response) {
        String formJsonByName = form1Service.getFormJsonByName(name);
        String filePath = fromJson + "\\" + formJsonByName;
        File file = new File(filePath);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
