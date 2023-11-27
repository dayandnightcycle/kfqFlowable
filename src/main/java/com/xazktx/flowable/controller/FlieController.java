package com.xazktx.flowable.controller;

import com.xazktx.flowable.base.BaseController;
import com.xazktx.flowable.base.ResMessage;
import com.xazktx.flowable.base.Result;
import com.xazktx.flowable.mapper.flowable.ActFoFormResourceMapper;
import com.xazktx.flowable.model.ActFoFormResource;
import com.xazktx.flowable.util.CodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.flowable.form.api.FormDeployment;
import org.flowable.form.api.FormRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.xazktx.flowable.base.ResMessage.ERROR_DB;
import static com.xazktx.flowable.base.ResMessage.ERROR_FILE;
import static com.xazktx.flowable.base.ResMessage.NO_RESULT;
import static com.xazktx.flowable.base.ResMessage.SUCCESS;

/**
 * 文件接收接口
 *
 * @return
 */

@Slf4j
@RestController
public class FlieController extends BaseController {

    @Autowired
    private FormRepositoryService formRepositoryService;

    @Autowired
    private ActFoFormResourceMapper actFoFormResourceMapper;

    /**
     * 接收表单 JSON 文件
     * @param multipartFile 文件
     * @param name 表单名
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public Result<ResMessage> upload(@RequestParam("file") MultipartFile multipartFile,
                                     HttpServletRequest request, String name) {

        File file;
        String jsonForm;
        try {
            file = new File(request.getSession()
                    .getServletContext()
                    .getRealPath("/"));
            multipartFile.transferTo(file);
            jsonForm = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            log.error("文件接收失败", e);
            return error(ERROR_FILE);
        }

        List<ActFoFormResource> list = actFoFormResourceMapper.selectJosnName();

        for (ActFoFormResource foFormResource : list) {
            try {
                if (foFormResource.getNAME_().equals(name)) {
                    int i = actFoFormResourceMapper.deleteFormByName(name);
                    if (i <= 0) {
                        return error(ERROR_DB);
                    }
                }
            } catch (Exception e) {
                log.info("空");
                return error(NO_RESULT);
            }
        }

        String key = CodeUtils.uuidStr();
        String[] split = jsonForm.split("^\\{");
        String form = "{" + "\"key\":\"" + key + "\"," +
                "\"name\":\"" + name + "\"," + split[1];

        byte[] bytes = form.getBytes(StandardCharsets.UTF_8);
        FormDeployment formDeployment;
        try {
            formDeployment = formRepositoryService.createDeployment()
                    .addFormBytes(name, bytes)
                    .name(name)
                    .parentDeploymentId(key)
                    .deploy();
            log.info("表单部署成功, formDeployment.getId() = {}", formDeployment.getId());
            return success(SUCCESS);
        } catch (Exception e) {
            log.error("表单部署失败", e);
            return error(ERROR_DB);
        }

    }

}
