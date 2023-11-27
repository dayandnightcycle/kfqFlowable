package com.xazktx.flowable.service;

import com.xazktx.flowable.mapper.flowable.NewHomePageMapper;
import com.xazktx.flowable.mapper.kfqggk.OldHomePageMapper;
import com.xazktx.flowable.mapper.kfqggk.HomePageMapper;
import com.xazktx.flowable.model.HomePage;
import com.xazktx.flowable.model.Lcmc;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class HomePageService {

    @Resource
    private HomePageMapper homePageMapper;

    @Resource
    private NewHomePageMapper newHomePageMapper;

    @Resource
    private OldHomePageMapper oldHomePageMapper;

    public List<HomePage> homePage(List<String> list, String YWMC, String YWZL, Integer pageNumber, Integer pageSize) {
        return homePageMapper.homepage(list, YWMC, YWZL, pageNumber, pageSize);
    }

    public List<HomePage> newhomePage(List<String> list, String YWMC, String YWZL, Integer pageNumber, Integer pageSize) {
        return newHomePageMapper.newhomepage(list, YWMC, YWZL, pageNumber, pageSize);
    }

    public List<HomePage> oldhomepagepro(String SLBH, String YWMC, String YWZL, Integer pageNumber, Integer pageSize) {
        return oldHomePageMapper.oldhomepagepro(SLBH, YWMC, YWZL, pageNumber, pageSize);
    }

    public HomePage oldhomepageact(String SLBH) {
        return oldHomePageMapper.oldhomepageact(SLBH);
    }

    @Async
    public Future<Long> count(List<String> list, String YWMC, String YWZL) {
        if(!StringUtils.hasText(YWMC) && !StringUtils.hasText(YWZL) && list.size() == 0){
            return new AsyncResult<>(0L);
        }
        return new AsyncResult<>(homePageMapper.count(list, YWMC, YWZL));
    }

    @Async
    public Future<Long> newcount(List<String> list, String YWMC, String YWZL) {
        if(!StringUtils.hasText(YWMC) && !StringUtils.hasText(YWZL) && list.size() == 0){
            return new AsyncResult<>(0L);
        }
        return new AsyncResult<>(newHomePageMapper.newcount(list, YWMC, YWZL));
    }

    @Async
    public Future<Long> oldcount(String SLBH, String YWMC, String YWZL) {
        return new AsyncResult<>(oldHomePageMapper.oldcount(SLBH, YWMC, YWZL));
    }

    public List<Lcmc> lcmc(String SLBH){
        return oldHomePageMapper.lcmc(SLBH);
    }

    public String lcmc1(String SLBH){
        return oldHomePageMapper.lcmc1(SLBH);
    }

}
