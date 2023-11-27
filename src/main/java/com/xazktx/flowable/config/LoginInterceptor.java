package com.xazktx.flowable.config;

import com.xazktx.flowable.base.BaseSEC_LOGAnnotation;
import com.xazktx.flowable.base.NoNeedLogin;
import com.xazktx.flowable.base.Result;
import com.xazktx.flowable.base.SEC_LOGAnnotation;
import com.xazktx.flowable.base.SEC_LOGErrorAnnotation;
import com.xazktx.flowable.model.ActIdUser;
import com.xazktx.flowable.model.HomePage;
import com.xazktx.flowable.model.SEC_LOG;
import com.xazktx.flowable.service.ActIdUserService;
import com.xazktx.flowable.service.Form1Service;
import com.xazktx.flowable.service.SEC_LOGService;
import com.xazktx.flowable.util.TokenUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static com.xazktx.flowable.util.CodeUtils.LOGID;
import static com.xazktx.flowable.util.IPUtils.getIPAddress;
import static com.xazktx.flowable.util.IPUtils.getMacAddress;

@Component
@ControllerAdvice
public class LoginInterceptor implements HandlerInterceptor, ResponseBodyAdvice<Object> {

    @Resource
    private ActIdUserService actIdUserService;

    @Resource
    private SEC_LOGService sec_logService;

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private HistoryService historyService;

    @Resource
    private Form1Service form1Service;

    @Value("${fromJson}")
    private String fromJson;

    LinkedHashMap<String, Integer> map = new LinkedHashMap<>();

    Result<Object> result = new Result<>();

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        boolean isHandlerMethod = handler instanceof HandlerMethod;
        if (!isHandlerMethod) {
            return true;
        }
        boolean isNoNeedLogin = ((HandlerMethod) handler).getMethodAnnotation(NoNeedLogin.class) != null;
        if (isNoNeedLogin) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        token = token.substring(7);
        try {
            String userId = TokenUtils.getUserId(token);
            ActIdUser user = actIdUserService.findById(userId);
            if (user == null) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
            request.setAttribute("user", user);
            return true;
        } catch (Exception exception) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    @Override
    public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, ModelAndView modelAndView) {
        try {
            int code = 0;
            try {
                code = result.getCode();
            } catch (Exception ignored) {
            }

            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                BaseSEC_LOGAnnotation baseSEC_logAnnotation = handlerMethod.getMethodAnnotation(BaseSEC_LOGAnnotation.class);

                if (baseSEC_logAnnotation != null) {
                    Date date = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String format = simpleDateFormat.format(date);
                    boolean isNoNeedLogin = handlerMethod.getMethodAnnotation(NoNeedLogin.class) != null;
                    SEC_LOG sec_log;
                    //客户端IP
                    String ip = "";
                    //服务器IP
                    String serverip = "";
                    try {
                        ip = request.getHeader("X-Real-IP");
                        //svnip
                        serverip = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException ignored) {
                    }
                    String username = "";
                    String mdlname;
                    List<HomePage> homePages = null;

                    if (isNoNeedLogin) {

                        String userId = request.getParameterValues("username")[0];
                        ActIdUser actIdUser = actIdUserService.findById(userId);

                        try {
                            String first = actIdUser.getFirst() == null ? "" : actIdUser.getFirst();
                            String last = actIdUser.getLast()  == null ? "" : actIdUser.getLast();
                            username = last + first;
                        } catch (Exception e) {
                            try {
                                username = getMacAddress();
                            } catch (Exception ignored) {
                            }
                        }

                        if (code == 0) {

                            for (SEC_LOGAnnotation sec_logAnnotation : baseSEC_logAnnotation.value()) {
                                String value = sec_logAnnotation.value();
                                String hostaddr = sec_logAnnotation.hostaddr();
                                map.remove(userId);
                                if (StringUtils.hasText(sec_logAnnotation.mdlname())) {
                                    mdlname = sec_logAnnotation.mdlname();
                                } else {
                                    mdlname = username;
                                }
                                if (value.contains("{USERNAME}")) {
                                    value = value.replace("{USERNAME}", username);
                                }
                                if (value.contains("{IP}")) {
                                    if (StringUtils.hasText(hostaddr) && hostaddr.equals("SERVERIP")) {
                                        ip = serverip;
                                    }
                                    value = value.replace("{IP}", ip);
                                }
                                if (value.contains("{DATE}")) {
                                    value = value.replace("{DATE}", format);
                                }
                                sec_log = new SEC_LOG(LOGID(), username, ip, sec_logAnnotation.appname(), mdlname, value, date, sec_logAnnotation.remark(), sec_logAnnotation.opertype(), sec_logAnnotation.warntype());
                                sec_logService.insertSEC_LOG(sec_log);
                            }

                        } else {

                            for (SEC_LOGErrorAnnotation sec_logErrorAnnotation : baseSEC_logAnnotation.error()) {
                                ip = getIPAddress(request);
                                String value = sec_logErrorAnnotation.value();
                                value = value.replace("{USERNAME}", userId);
                                Integer integer;
                                try {
                                    integer = map.get(userId);
                                } catch (Exception e) {
                                    integer = 0;
                                }
                                integer = (integer == null ? 0 : integer) + 1;
                                value = value.replace("{COUNT}", integer.toString());
                                map.put(userId, integer);
                                sec_log = new SEC_LOG(LOGID(), username, ip, sec_logErrorAnnotation.appname(), "管理员", value, date, "", sec_logErrorAnnotation.opertype(), sec_logErrorAnnotation.warntype());
                                sec_logService.insertSEC_LOG(sec_log);
                            }
                        }

                    } else {

                        String token = request.getHeader("Authorization");
                        if (token == null || !token.startsWith("Bearer ")) {
                            return;
                        } else token = token.substring(7);
                        try {
                            String userId = TokenUtils.getUserId(token);
                            ActIdUser actIdUser = actIdUserService.findById(userId);
                            String first = actIdUser.getFirst() == null ? "" : actIdUser.getFirst();
                            String last = actIdUser.getLast()  == null ? "" : actIdUser.getLast();
                            username = last + first;
                        } catch (Exception ignored) {
                        }

                        if (code == 0 || code == -8) {

                            for (SEC_LOGAnnotation sec_logAnnotation : baseSEC_logAnnotation.value()) {
                                ip = getIPAddress(request);
                                String value = sec_logAnnotation.value();
                                String hostaddr = sec_logAnnotation.hostaddr();

                                ifslbh:
                                if (value.contains("{SLBH}")) {
                                    String slbh;
                                    try {
                                        slbh = request.getParameterValues("slbh")[0];
                                    } catch (Exception e) {
                                        break ifslbh;
                                    }
                                    if (!StringUtils.hasText(slbh)) {
                                        slbh = request.getParameterValues("SLBH")[0];
                                    }
                                    if (StringUtils.hasText(slbh)) {
                                        value = value.replace("{SLBH}", slbh);
                                    }
                                }
                                if (value.contains("{DATE}")) {
                                    value = value.replace("{DATE}", format);
                                }
                                if (value.contains("{USERNAME}")) {
                                    value = value.replace("{USERNAME}", username);
                                }
                                if (value.contains("{YWMC}")) {
                                    String taskName = request.getParameterValues("taskName")[0];
                                    if (!taskName.equals("gd") && !taskName.equals("ja")) {
                                        return;
                                    }
                                    String taskId = request.getParameterValues("taskId")[0];
                                    String ywmc;
                                    String slbh = historyService.createHistoricTaskInstanceQuery().taskId(taskId).listPage(0, 1).get(0).getDescription();
                                    ArrayList<String> objects = new ArrayList<>();
                                    objects.add(slbh);
                                    homePages = sec_logService.ywmc(objects);
                                    try {
                                        ywmc = homePages.get(0).getYWMC();
                                    } catch (Exception e) {
                                        ywmc = "";
                                    }
                                    value = value.replace("{SLBH}", slbh);
                                    value = value.replace("{YWMC}", ywmc);
                                }
                                if (value.contains("{DJDL}")) {
                                    String key = request.getParameterValues("key")[0];
                                    String djdl = repositoryService.createProcessDefinitionQuery()
                                            .processDefinitionKey(key)
                                            .listPage(0, 1)
                                            .get(0)
                                            .getDescription();
                                    value = value.replace("{DJDL}", djdl);
                                }
                                if (value.contains("{FROMJSONID}")) {
                                    String filePath = request.getParameterValues("filePath")[0];
                                    String replace = filePath.replace(fromJson + "\\", "");
                                    String formJsonIDByPath = form1Service.getFormJsonIDByPath(replace);
                                    try {
                                        value = value.replace("{FROMJSONID}", formJsonIDByPath);
                                    } catch (Exception e) {
                                        value = value.replace("{FROMJSONID}", "");
                                    }
                                }
                                if (value.contains("{FROMJSONNAME}")) {
                                    String filePath = request.getParameterValues("filePath")[0];
                                    String replace = filePath.replace(fromJson + "\\", "")
                                            .replace(".json", "")
                                            .replace("\\", "-");
                                    value = value.replace("{FROMJSONNAME}", replace);
                                }
                                if (value.contains("{IP}")) {
                                    if (StringUtils.hasText(hostaddr) && hostaddr.equals("SERVERIP")) {
                                        ip = serverip;
                                    }
                                    value = value.replace("{IP}", ip);
                                }
                                if (sec_logAnnotation.mdlname().equals("{WrkID}") && homePages != null) {
                                    mdlname = homePages.get(0).getWRKID();
                                } else mdlname = sec_logAnnotation.mdlname();
                                sec_log = new SEC_LOG(LOGID(), username, ip, sec_logAnnotation.appname(), mdlname, value, date, sec_logAnnotation.remark(), sec_logAnnotation.opertype(), sec_logAnnotation.warntype());
                                sec_logService.insertSEC_LOG(sec_log);
                            }
                        }
                    }
                }
            }

        } catch (Exception ignored) {
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body, @NotNull MethodParameter returnType, @NotNull MediaType selectedContentType, @NotNull Class selectedConverterType, @NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response) {
        try {
            result = (Result<Object>) body;
        } catch (Exception ignored) {
        }
        return body;
    }

    @Override
    public boolean supports(@NotNull MethodParameter returnType, @NotNull Class converterType) {
        return true;
    }

}
