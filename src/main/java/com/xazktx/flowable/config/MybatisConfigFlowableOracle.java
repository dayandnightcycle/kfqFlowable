package com.xazktx.flowable.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * mybatis连接flowable数据库
 */

@Configuration
@MapperScan(basePackages = "com.xazktx.flowable.mapper.flowable", sqlSessionTemplateRef = "sqlSessionTemplateflowable")
public class MybatisConfigFlowableOracle {

    @Primary
    @Bean("sqlSessionFactoryflowable")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("flowableDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource);
        sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath" +
                ":mappers/flowable/*.xml"));
        return sqlSessionFactory.getObject();
    }

    @Primary
    @Bean("sqlSessionTemplateflowable")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactoryflowable") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
