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
 * mybatis连接kfqsxk数据库
 */

@Configuration
@MapperScan(basePackages = "com.xazktx.flowable.mapper.kfqsxk", sqlSessionTemplateRef = "sqlSessionTemplatekfqsxk")
public class MybatisConfigKFQSXKOracle {

    @Bean("sqlSessionFactorykfqsxk")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("kfqsxkDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource);
        sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath" +
                ":mappers/kfqsxk/*.xml"));
        return sqlSessionFactory.getObject();
    }

    @Bean("sqlSessionTemplatekfqsxk")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactorykfqsxk") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
