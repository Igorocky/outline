package org.igye.outline.config;

import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.dialect.Database;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DbConfig {
    @Value("${db.url}")
    private String dbUrl;

    @Value("${db.user}")
    private String dbUser;

    @Value("${db.pwd}")
    private String dbPwd;

    @Bean
    public DataSource dataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(dbUrl);
        ds.setUser(dbUser);
        ds.setPassword(dbPwd);
        return ds;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean res = new LocalSessionFactoryBean();
        res.setDataSource(dataSource());
        res.setPackagesToScan("org.igye.outline.model");
        Properties props = new Properties();
        props.put("hibernate.dialect", Database.H2);
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.use_sql_comments", "true");
//            props.put("hibernate.show_sql", "true");
//        props.put("hibernate.hbm2ddl.auto", "create");
        res.setHibernateProperties(props);
        return res;
    }

    @Bean
    public HibernateTransactionManager transactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }
}
