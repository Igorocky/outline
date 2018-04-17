package org.igye.outline.datamigration;

import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.dialect.Database;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(basePackageClasses = {Migrator.class})
@EnableTransactionManagement
public class MigrateConfig {
    @Bean
    public DataSource dataSourceOldDb() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:tcp://localhost:9092/zorich");
        ds.setUser("zorich");
        ds.setPassword("zorich");
        return ds;
    }

    @Bean
    public DataSource dataSourceNewDb() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:tcp://localhost:9092/zorich2");
        ds.setUser("zorich2");
        ds.setPassword("zorich2");
        return ds;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactoryOldDb() {
        LocalSessionFactoryBean res = new LocalSessionFactoryBean();
        res.setDataSource(dataSourceOldDb());
        res.setPackagesToScan("org.igye.outline.oldmodel");
        Properties props = new Properties();
        props.put("hibernate.dialect", Database.H2);
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.use_sql_comments", "true");
//            props.put("hibernate.show_sql", "true");
//        props.put("hibernate.hbm2ddl.auto", "validate");
        res.setHibernateProperties(props);
        return res;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactoryNewDb() {
        LocalSessionFactoryBean res = new LocalSessionFactoryBean();
        res.setDataSource(dataSourceNewDb());
        res.setPackagesToScan("org.igye.outline.model");
        Properties props = new Properties();
        props.put("hibernate.dialect", Database.H2);
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.use_sql_comments", "true");
//            props.put("hibernate.show_sql", "true");
        props.put("hibernate.hbm2ddl.auto", "create");
        res.setHibernateProperties(props);
        return res;
    }

    @Bean
    public HibernateTransactionManager transactionManagerOldDb() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactoryOldDb().getObject());
        return transactionManager;
    }

    @Bean
    public HibernateTransactionManager transactionManagerNewDb() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactoryNewDb().getObject());
        return transactionManager;
    }


}