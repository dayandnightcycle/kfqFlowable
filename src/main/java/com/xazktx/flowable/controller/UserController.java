package com.xazktx.flowable.controller;

import com.hebca.pki.Cert;
import com.hebca.pki.CertParse;
import com.hebca.pki.RandomGen;
import com.hebca.svs.client.SvsClientHelper;
import com.hebca.svs.client.st.THostInfoSt;
import com.xazktx.flowable.base.BaseController;
import com.xazktx.flowable.base.BaseSEC_LOGAnnotation;
import com.xazktx.flowable.base.BizException;
import com.xazktx.flowable.base.NoNeedLogin;
import com.xazktx.flowable.base.ResMessage;
import com.xazktx.flowable.base.Result;
import com.xazktx.flowable.base.SEC_LOGAnnotation;
import com.xazktx.flowable.base.SEC_LOGErrorAnnotation;
import com.xazktx.flowable.mapper.flowable.ActIdUserMapper;
import com.xazktx.flowable.model.ActIdUser;
import com.xazktx.flowable.service.ActIdUserService;
//import com.xazktx.flowable.util.ConfigUtils;
import com.xazktx.flowable.util.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org2.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Security;
import java.util.List;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private ActIdUserService actIdUserService;

    @Resource
    private ActIdUserMapper actIdUserMapper;

    @GetMapping("/{id}")
    public Result<ActIdUser> getUserByUsername(@PathVariable("id") String id) {
        if (!StringUtils.hasText(id)) {
            return error(ResMessage.ERROR_PARAM);
        }

        ActIdUser user = actIdUserService.findById(id);
        if (user == null) {
            return error(ResMessage.NO_RESULT);
        }

        return success(user);
    }

    @BaseSEC_LOGAnnotation(value = {
            @SEC_LOGAnnotation(value = "验证用户登录[廊坊开发区不动产统一登记平台 PRO]成功!", opertype = "登录"),
            @SEC_LOGAnnotation(value = "用户名为[{USERNAME}]的用户从地址[{IP}]以[正常]方式于[{DATE}]登录了系统", hostaddr = "SERVERIP")
    }, error = {
            @SEC_LOGErrorAnnotation(value = "◆您选定的身份信息[{USERNAME}]与输入的密码等验证信息不匹配，已拒绝登录[第{COUNT}次]")
    })
    @NoNeedLogin
    @PostMapping("/login")
    public Result<String> login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return error(ResMessage.ERROR_PARAM);
        }

        try {
            String token = actIdUserService.login(username, password);
            return success(token);
        } catch (BizException exception) {
            return error(exception);
        }
    }

    /**
     * 获取 uk randomString
     * @param req
     */
    @NoNeedLogin
    @GetMapping("/LoginBefore")
    public Result<String> LoginBefore(HttpServletRequest req) {
        RandomGen mRandomGen = new RandomGen();
        String randomString = (String) mRandomGen.getstrRandom();
        log.info("randomString = " + randomString);
        req.getSession().setAttribute("randomString", randomString);
        return success(randomString);
    }

    @Value("${svsIp}")
    String svsIp;

    @Value("${svsPort}")
    String svsPortStr;

    /**
     * uk 登录
     * @param signature 密码签名字符串，是通过用用户私钥对上面随机字符串进行签名后得到的字符串。
     * @param cert 用户签名证书字符串
     * @param request
     * @return
     */
    @NoNeedLogin
    @PostMapping("/LoginAction")
    public Result<String> LoginAction(String signature, String cert, HttpServletRequest request, String randomString) {
        /******************************************************
         * 数字证书登陆步骤二(共三步)： 后台服务器验证：调用jar包中的相关类进行操作，THostInfoSt()初始化服务器，
         * 服务器获取随机字符串、随机字符串签名值和签名证书的Base64字符串，
         * SvsClientHelper.getInstance()连接svs服务器并用verifyCertSign()进行证书和签名验证
         *****************************************************/
//        String randomString = (String) request.getSession().getAttribute(
//                "randomString");
        log.info("randomString = " + randomString);
        if (null == randomString || "".equals(randomString)) {
            return error(ResMessage.ERROR_PARAM);
        }

//        String token = TokenUtils.getToken(cert);
//        return success(token);

        //SVS------------------------------------
        String ret = "";
        int resultNumber = -1;// svs服务器返回错误代号
        //Security.addProvider(new BouncyCastleProvider()); // 添加
        // 初始化服务器
//        String svsIp = ConfigUtils.getProperty("svsIp");// SVS服务器IP
//        Integer svsPort = Integer.parseInt(ConfigUtils.getProperty("svsPort"));// SVS服务器IP
        Integer svsPort = Integer.parseInt(svsPortStr);// SVS服务器IP

        THostInfoSt hostInfo = new THostInfoSt();
        hostInfo.setSvrIP(svsIp);// 配置svs服务器ip
        hostInfo.setPort(svsPort);// 配置svs服务器端口

        // 进行svs服务器验证
        try {
            byte[] arrayOriginData = randomString.getBytes();// 获取接收到的随机字符串的字节数组
            int nOriginDataLen = arrayOriginData.length; // 获取接收到的随机字符串的字节数组长度

            log.info("svsIp = " + svsIp);
            log.info("svsPort = " + svsPort);
            // 初始化svs验证工具类
            String CN = "";
            SvsClientHelper svsclientHelper = SvsClientHelper.getInstance(); // 获取svs验证类实例
            svsclientHelper.initialize(svsIp, svsPort, 1000, false, 5000); // 与svs服务器建立连接初始化验证
            // 进行SVS验证，包含三方面：（1）用户身份验证 （2）证书状态验证 （3）证书有效期验证
            resultNumber = svsclientHelper.verifyCertSign(-1, // 签名类型
                    0, arrayOriginData, // 签名原文（即随机字符串）对应的字节数组
                    nOriginDataLen, // 签名原文长度
                    cert, // 用户签名证书B64字符串
                    signature, // 对随机字符串的签名值B64串
                    1, hostInfo).getResult();

            if (resultNumber == 0) {
                /********************************************************
                 * 数字证书登陆步骤三（共三步）： 获取签名证书的唯一标识 getSubjectByOid(
                 * '1.2.156.112586.1.4')、CN项、G项；以上三项依次进行比较确认证书唯一项
                 * 调用jar包中的相关类进行操作
                 * ，THostInfoSt()初始化服务器，服务器获取随机字符串、随机字符串签名值和签名证书的Base64字符串，
                 * SvsClientHelper
                 * .getInstance()连接svs服务器并用verifyCertSign()进行证书和签名验证
                 * 根据证书唯一标识在数据库中检查数字证书是否已经与用户进行绑定
                 * ，假如没有绑定则提示不是该系统用户；假如已经绑定则从数据库获取用户权限并进入相应系统。
                 *********************************************************/

                Security.addProvider(new BouncyCastleProvider());
                CertParse cp = new CertParse(new Cert(cert), "BC2"); // 创建证书解析对象
                CN = cp.getSubject(CertParse.DN_CN); // 获取证书CN项
                ActIdUser byName = actIdUserService.findByName(CN);
                String gName = cp.getSubject(CertParse.DN_GIVENNAME); // 获取证书名称G项
                //	String CertParse.DN_ORG
                String uniqueFlag = ""; // 设置证书唯一标识
                if (gName.length() > CN.length()) {
                    uniqueFlag = gName;
                } else {
                    uniqueFlag = CN;
                }
                ret = uniqueFlag;

                String token = TokenUtils.getToken(byName.getId());
                return success(token);

            } else {
                switch (resultNumber) {
                    case -1:
                        ret = "(无法连接svs服务器)";
                        break;
                    case 1:
                        ret = "(证书还未生效)";
                        break;
                    case 2:
                        ret = "(证书已经过期，需要延期后才能使用)";
                        break;
                    case 4:
                        ret = "(非河北CA签发的有效证书)";
                        break;
                    case 7:
                        ret = "(证书状态无效)";
                        break;
                    case 12:
                        ret = "(不是该项目用的证书)";
                        break;
                    case 1002:
                        ret = "(非河北CA签发的有效证书)";
                        break;
                    case -6805:
                        ret = "(无效的证书文件)";
                        break;
                    case -6406:
                        ret = "(非法的证书)";
                        break;
                    case -6504:
                        ret = "(非法的证书)";
                        break;
                    case -6505:
                        ret = "(非法的证书)";
                        break;
                    default:
                        ret = "(errorCode:" + resultNumber + ")";
                }
                log.error("SVS服务器异常");
                return error(ret);
            }

        } catch (Exception e) {
            log.error("UK登录失败", e);
            ret = e.getMessage();
            return error(ret);
        }

    }

    @Value("${CAPATH}")
    private String CAPATH;
    private static final String encoding = System.getProperty("file.encoding");
    private static final String LANGUE_ISO = "iso-8859-1";

    /**
     * 下载 CA 驱动
     * @param response
     */
    @NoNeedLogin
    @GetMapping("getCAEXE")
    public void getCAEXE(HttpServletResponse response){
        File file = new File(CAPATH);
        String filename = file.getName();
        log.info("filename = " + filename);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            try {
                response.setHeader("Content-Disposition",
                        "attachment;filename=" + URLEncoder.encode(filename,"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @NoNeedLogin
//    @PostMapping("/login")
//    public Result<String> login(String username, String password, String signature, String cert, HttpServletRequest request) {
//        if(StringUtils.hasText(signature) && StringUtils.hasText(cert)){
//            String randomString = (String) request.getSession().getAttribute(
//                    "randomString");
//            if (null == randomString || "".equals(randomString)) {
//                return error(ResMessage.ERROR_PARAM);
//            }
//            String ret="";
//            int resultNumber = -1;// svs服务器返回错误代号
//            //Security.addProvider(new BouncyCastleProvider()); // 添加
//            // 初始化服务器
//            String svsIp = ConfigUtils.getProperty("svsIp");// SVS服务器IP
//            Integer svsPort = Integer.parseInt(ConfigUtils.getProperty("svsPort"));// SVS服务器IP
//
//            THostInfoSt hostInfo = new THostInfoSt();
//            hostInfo.setSvrIP(svsIp);// 配置svs服务器ip
//            hostInfo.setPort(svsPort);// 配置svs服务器端口
//
//            // 进行svs服务器验证
//            try {
//                byte[] arrayOriginData = randomString.getBytes();// 获取接收到的随机字符串的字节数组
//                int nOriginDataLen = arrayOriginData.length; // 获取接收到的随机字符串的字节数组长度
//
//                // 初始化svs验证工具类
//                String CN="";
//                SvsClientHelper svsclientHelper = SvsClientHelper.getInstance(); // 获取svs验证类实例
//                svsclientHelper.initialize(svsIp, svsPort, 1000, false, 5000); // 与svs服务器建立连接初始化验证
//                // 进行SVS验证，包含三方面：（1）用户身份验证 （2）证书状态验证 （3）证书有效期验证
//                resultNumber = svsclientHelper.verifyCertSign(-1, // 签名类型
//                        0, arrayOriginData, // 签名原文（即随机字符串）对应的字节数组
//                        nOriginDataLen, // 签名原文长度
//                        cert, // 用户签名证书B64字符串
//                        signature, // 对随机字符串的签名值B64串
//                        1, hostInfo).getResult();
//
//                if (resultNumber == 0) {
//                    /********************************************************
//                     * 数字证书登陆步骤三（共三步）： 获取签名证书的唯一标识 getSubjectByOid(
//                     * '1.2.156.112586.1.4')、CN项、G项；以上三项依次进行比较确认证书唯一项
//                     * 调用jar包中的相关类进行操作
//                     * ，THostInfoSt()初始化服务器，服务器获取随机字符串、随机字符串签名值和签名证书的Base64字符串，
//                     * SvsClientHelper
//                     * .getInstance()连接svs服务器并用verifyCertSign()进行证书和签名验证
//                     * 根据证书唯一标识在数据库中检查数字证书是否已经与用户进行绑定
//                     * ，假如没有绑定则提示不是该系统用户；假如已经绑定则从数据库获取用户权限并进入相应系统。
//                     *********************************************************/
//
//
//                    Security.addProvider(new BouncyCastleProvider());
//                    CertParse cp = new CertParse(new Cert(cert), "BC2"); // 创建证书解析对象
//                    CN = cp.getSubject(CertParse.DN_CN); // 获取证书CN项
//                    String gName = cp.getSubject(CertParse.DN_GIVENNAME); // 获取证书名称G项
//                    //	String CertParse.DN_ORG
//                    String uniqueFlag = ""; // 设置证书唯一标识
//                    if (gName.length() > CN.length()) {
//                        uniqueFlag = gName;
//                    } else {
//                        uniqueFlag = CN;
//                    }
//                    ret=uniqueFlag;
//
//
//                }else{
//                    switch (resultNumber) {
//                        case -1:
//                            ret="(无法连接svs服务器)";
//                            break;
//                        case 1:
//                            ret="(证书还未生效)";
//                            break;
//                        case 2:
//                            ret="(证书已经过期，需要延期后才能使用)";
//                            break;
//                        case 4:
//                            ret="(非河北CA签发的有效证书)";
//                            break;
//                        case 7:
//                            ret="(证书状态无效)";
//                            break;
//                        case 12:
//                            ret="(不是该项目用的证书)";
//                            break;
//                        case 1002:
//                            ret="(非河北CA签发的有效证书)";
//                            break;
//                        case -6805:
//                            ret="(无效的证书文件)";
//                            break;
//                        case -6406:
//                            ret="(非法的证书)";
//                            break;
//                        case -6504:
//                            ret="(非法的证书)";
//                            break;
//                        case -6505:
//                            ret="(非法的证书)";
//                            break;
//                        default:
//                            ret="(errorCode:" + resultNumber + ")";
//                    }
//                    return error(ResMessage.ERROR_THIRD);
//                }
//
//            } catch (Exception e) {
//                log.error("UK登录失败",e);
//                ret = e.getMessage();
//                return error(ResMessage.ERROR_THIRD);
//            }
//        }else {
//            if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
//                return error(ResMessage.ERROR_PARAM);
//            }
//
//            try {
//                String token = actIdUserService.login(username, password);
//                return success(token);
//            } catch (BizException exception) {
//                return error(exception);
//            }
//        }
//        return error(ResMessage.ERROR_PARAM);
//    }

    /**
     * 获取所有用户
     * @return
     */
    @GetMapping("/users")
    public Result<List<ActIdUser>> findAll() {
        List<ActIdUser> all = actIdUserMapper.findAll();
        return success(all);
    }

}
