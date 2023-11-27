package com.xazktx.flowable.util;

import org.flowable.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ConvertUtils {

    @Autowired
    private RepositoryService repositoryService;

    private static ConvertUtils convertUtils;

    @PostConstruct
    public void init() {
        convertUtils = this;
    }

    public static String convert(String description) {
        switch (description) {
            case "收件":
                return "sjusers";
            case "受理":
                return "slusers";
            case "预收件":
                return "ysjusers";
            case "预受理":
                return "yslusers";
            case "收费":
                return "sfusers";
            case "初审":
            case "复审":
            case "核定":
                return "csusers";
            case "登簿缮证":
                return "dbszusers";
            case "发证":
                return "fzusers";
            case "归档":
                return "gdusers";
            case "登簿":
                return "dbusers";
            case "缮证":
                return "szusers";
            case "审核":
                return "shusers";
            case "收费发证":
                return "sffzusers";
            case "发件":
                return "fjusers";
            case "结案":
                return "jausers";
            case "发件归档":
                return "fjgdusers";
            default:
                return "";
        }
    }

    public static String convert1(String description) {
        switch (description) {
            case "sjusers":
                return "收件";
            case "slusers":
                return "受理";
            case "ysjusers":
                return "预收件";
            case "yslusers":
                return "预受理";
            case "sfusers":
                return "收费";
            case "csusers":
                return "初审";
            case "dbszusers":
                return "登簿缮证";
            case "fzusers":
                return "发证";
            case "gdusers":
                return "归档";
            case "dbusers":
                return "登簿";
            case "szusers":
                return "缮证";
            case "shusers":
                return "审核";
            case "sffzusers":
                return "收费发证";
            case "fjusers":
                return "发件";
            case "jausers":
                return "结案";
            case "fjgdusers":
                return "发件归档";
            default:
                return "";
        }
    }

    public static String convert2(String description) {
        switch (description) {
            case "sj":
                return "收件";
            case "sl":
                return "受理";
            case "sf":
                return "收费";
            case "cs":
                return "初审";
            case "fs":
                return "复审";
            case "hd":
                return "核定";
            case "dbsz":
                return "登簿缮证";
            case "fz":
                return "发证";
            case "gd":
                return "归档";
            case "db":
                return "登簿";
            case "sz":
                return "缮证";
            case "sh":
                return "审核";
            case "sffz":
                return "收费发证";
            case "ja":
                return "结案";
            case "fjgd":
                return "发件归档";
            default:
                return "";
        }
    }

    public static String convert3(String description) {
        switch (description) {
            case "收件":
                return "sj";
            case "受理":
                return "sl";
            case "收费":
                return "sf";
            case "初审":
                return "cs";
            case "登簿缮证":
                return "dbsz";
            case "发证":
                return "fz";
            case "归档":
                return "gd";
            case "登簿":
                return "db";
            case "缮证":
                return "sz";
            case "审核":
                return "sh";
            case "收费发证":
                return "sffz";
            case "结案":
                return "ja";
            case "发件归档":
                return "fjgd";
            default:
                return "";
        }
    }

    public static String processDefinitionNameConvert(String description) {
        switch (description) {
            case "变更登记":
                return "BGDJ";
            case "变更土地":
                return "BGTDDJ";
            case "补证登记":
                return "BZHZ";
            case "补证换证带证明":
                return "BZHZDZM";
            case "查封(按单元)补录登记":
                return "CFADYBLDJ";
            case "查封(按证)补录登记":
                return "CFAZBLDJ";
            case "查封登记":
                return "CFDJ";
            case "查封登记按单元":
                return "CFDJADY";
            case "抵押不动产证明补录登记":
                return "DYBDCZMBLDJ";
            case "抵押变更":
                return "DYBG";
            case "抵押预告登记":
                return "DYYGDJ";
            case "抵押注销登记":
                return "DYZXDJ";
            case "房屋不动产证补录登记":
                return "FWBDCZBLDJ";
            case "抵押登记":
                return "GYJSYDSYQJFWSYQDYDJ";
            case "首次登记（商品房）":
                return "GYJSYDSYQJFWSYQSCDJDZ";
            case "国有建设用地使用权及房屋所有权更正登记（证书含证明）登记":
                return "GZDJHZM";
            case "更正登记证明":
                return "GZDJZM";
            case "更正登记证书":
                return "GZDJZS";
            case "解封登记":
                return "JFDJ1";
            case "集体建设用地解封登记":
                return "JTJSYDJFDJ";
            case "集体建设用地使用权查封登记":
                return "JTJSYDSYQCFDJ";
            case "集体建设用地续查封登记":
                return "JTJSYDXCFDJ";
            case "集体土地变更登记":
                return "JTTDBGDJ";
            case "集体土地补证换证":
                return "JTTDBZHZ";
            case "集体土地更正登记":
                return "JTTDGZDJ";
            case "集体土地首次登记":
                return "JTTDSCDJ";
            case "集体土地注销登记":
                return "JTTDZXDJ";
            case "轮候查封按单元":
                return "LHCFADY";
            case "轮候查封登记":
                return "LHCFDJ";
            case "首次登记(纯土地)":
                return "SCDJCTD";
            case "首次登记多幢":
                return "SCDJDZ";
            case "土地不动产证补录登记":
                return "TDBDCZBLDJ";
            case "续查封登记":
                return "XCFDJ";
            case "续查封登记按单元":
                return "XCFDJADY";
            case "预告补录登记":
                return "YGBLDJ";
            case "预告登记":
                return "YGDJ";
            case "预告抵押登记":
                return "YGDYDJ";
            case "预告(证明)登记":
                return "YGFWDJ";
            case "预告商品房":
                return "YGSCDJ";
            case "预告土地登记":
                return "YGTDDJ";
            case "预告(证明)抵押登记":
                return "YGZMDYDJ";
            case "异议登记":
                return "YYDJ";
            case "宅基地变更登记":
                return "ZJDBGDJ";
            case "宅基地补证换证":
                return "ZJDBZHZ";
            case "宅基地房屋首次登记":
                return "ZJDFWSCDJ";
            case "宅基地首次登记":
                return "ZJDSCDJ";
            case "集体土地所有权注销登记":
                return "ZJDSYQZXDJ";
            case "在建工程抵押":
                return "ZJGCDY";
            case "在建工程抵押变更登记":
                return "ZJGCDYBG";
            case "在建工程抵押转本抵押":
                return "ZJGCDYZBDY";
            case "证书注销登记":
                return "ZSZXDJ";
            case "注销登记":
                return "ZXDJ";
            case "转移登记":
                return "ZYDJ";
            case "转移登记按揭":
                return "ZYAJDJ";
            case "转移登记(大证分户)":
                return "ZYDJDZFH";
            case "转移登记(土地分割)":
                return "ZYDJTDFG";
            case "转移登记土地":
                return "ZYTDDJ";
            default:
                return "";
        }
    }

    public static String processDefinitionNameConvert1(String description) {
        switch (description) {
            case "BGDJ"://
                return "变更登记";
            case "BGTDDJ"://
                return "变更登记";
            case "BZHZ"://
                return "补证登记";
            case "BZHZDZM"://
                return "补证登记(证明)";
            case "CFADYBLDJ"://
                return "查封补录(按单元)登记";
            case "CFAZBLDJ"://
                return "查封补录(按证)登记";
            case "CFDJ"://
                return "查封登记";
            case "CFDJADY"://
                return "查封登记(按单元)";
            case "DYBDCZMBLDJ":
                return "抵押不动产证明补录登记";
            case "DYBG"://
                return "抵押变更";
            case "DYYGDJ"://
                return "抵押预告登记";
            case "DYZXDJ":
                return "抵押注销登记";
            case "FWBDCZBLDJ":
                return "房屋不动产证补录登记";
            case "GYJSYDSYQJFWSYQDYDJ":
                return "抵押登记";
            case "GYJSYDSYQJFWSYQSCDJDZ"://
                return "首次登记（商品房）";
            case "GZDJHZM"://
                return "更正登记（证明）";
            case "GZDJZM"://
                return "更正登记（证明）";
            case "GZDJZS"://
                return "更正登记（证书）";
            case "JFDJ1"://
                return "解封登记";
            case "JTJSYDJFDJ":
                return "集体建设用地解封登记";
            case "JTJSYDSYQCFDJ":
                return "集体建设用地使用权查封登记";
            case "JTJSYDXCFDJ":
                return "集体建设用地续查封登记";
            case "JTTDBGDJ":
                return "集体土地变更登记";
            case "JTTDBZHZ":
                return "集体土地补证换证";
            case "JTTDGZDJ":
                return "更正登记";
            case "JTTDSCDJ":
                return "首次登记";
            case "JTTDZXDJ":
                return "注销登记";
            case "LHCFADY":
                return "轮候查封登记(按单元)";
            case "LHCFDJ":
                return "轮候查封登记";
            case "SCDJCTD":
                return "首次登记_纯土地";
            case "SCDJDZ":
                return "首次登记_多幢";
            case "TDBDCZBLDJ":
                return "土地不动产证补录登记";
            case "XCFDJ":
                return "续查封登记";
            case "XCFDJADY":
                return "续查封登记(按单元)";
            case "YGBLDJ":
                return "预告补录登记";
            case "YGDJ":
                return "预告登记";
            case "YGDYDJ":
                return "预告抵押登记";
            case "YGFWDJ":
                return "预告(证明)登记";
            case "YGSCDJ":
                return "预告商品房登记";
            case "YGTDDJ":
                return "预告土地登记";
            case "YGZMDYDJ":
                return "预告(证明)抵押登记";
            case "YYDJ":
                return "异议登记";
            case "ZJDBGDJ":
                return "宅基地变更登记";
            case "ZJDBZHZ":
                return "宅基地补证换证";
            case "ZJDFWSCDJ":
                return "宅基地房屋首次登记";
            case "ZJDSCDJ":
                return "宅基地首次登记";
            case "ZJDSYQZXDJ"://
                return "注销登记";
            case "ZJGCDY":
                return "在建工程抵押";
            case "ZJGCDYBG":
                return "在建工程抵押变更";
            case "ZJGCDYZBDY":
                return "在建工程抵押转本抵押";
            case "ZSZXDJ":
                return "证书注销登记";
            case "ZXDJ":
                return "注销登记";
            case "ZYDJ":
                return "转移登记";
            case "ZYAJDJ":
                return "转移登记按揭";
            case "ZYDJDZFH":
                return "转移登记";
            case "ZYDJTDFG":
                return "转移登记";
            case "ZYTDDJ":
                return "转移登记";
            default:
                return convertUtils.repositoryService
                        .createProcessDefinitionQuery()
                        .processDefinitionKey(description)
                        .orderByProcessDefinitionVersion()
                        .desc()
                        .list()
                        .get(0)
                        .getDescription();
        }
    }

    public static String DJDLConvert(String djdl){
        switch (djdl) {
            case "100":
                return "GYJSYDSYQJFWSYQSCDJDZ";
            case "200":
                return "ZYDJ1";
            case "300":
                return "BGDJ";
            case "400":
                return "ZXDJ";
            case "500":
                return "GZDJZS";
            case "600":
                return "YYDJ";
            case "700":
                return "YGDJ";
            case "800":
                return "CFDJ";
//            case "900":
//                return "";
            case "910":
                return "GYJSYDSYQJFWSYQDYDJ";
            case "901":
                return "BZHZ";
            case "902":
                return "BZHZ";
            default:
                return "";
        }
    }

    public static String LCMCConvert(String djdl){
        switch (djdl) {
            case "预告抵押登记":
                return "YGDYDJ";
            case "抵押登记":
                return "GYJSYDSYQJFWSYQDYDJ";
            case "在建工程抵押":
                return "ZJGCDY";
            case "抵押变更":
                return "DYBG";
            case "在建工程抵押转本抵押":
                return "ZJGCDYZBDY";
            case "在建工程抵押变更登记":
                return "ZJGCDYBG";
            case "更正登记（证明）":
                return "GZDJZM";
            case "更正登记（证书）":
                return "GZDJZS";
            case "更正登记（证书和证明）":
                return "GZDJZSHZM";
            default:
                return "";
        }
    }

}
