package com.xazktx.flowable.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * mybatis连接kfqggk数据库
 */

@Configuration
@MapperScan(basePackages = "com.xazktx.flowable.mapper.kfqggk", sqlSessionTemplateRef = "sqlSessionTemplatekfqggk")
public class MybatisConfigKFQGGKOracle {

    @Bean("sqlSessionFactorykfqggk")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("kfqggkDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource);
        sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath" +
                ":mappers/kfqggk/*.xml"));
        return sqlSessionFactory.getObject();
    }

    @Bean("sqlSessionTemplatekfqggk")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactorykfqggk") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
