package com.xazktx.flowable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WFM_ACTINST {

    private String ID;
    private String YJ;
    private String SLBH;
    private String YWMC;
    private String BLBZ;
    private String SJR;
    private String TJR;
    private Date JSSJ;
    private String BLZT;
    private Date YWXDJZSJ;
    private String PROCNAME;
    private String YWZL;

}
