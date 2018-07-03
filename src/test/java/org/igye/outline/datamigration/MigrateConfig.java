package org.igye.outline.datamigration;

import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.dialect.Database;
import org.igye.outline.config.DbConfig;
import org.igye.outline.data.Dao;
import org.igye.outline.data.UserDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(basePackageClasses = {Migrator.class, DbConfig.class, Dao.class, UserDao.class})
@EnableTransactionManagement
public class MigrateConfig {
    @Bean
    public DataSource dataSourceOldDb() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:tcp://localhost:7072/zorich");
        ds.setUser("zorich");
        ds.setPassword("zorich");
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
    public HibernateTransactionManager transactionManagerOldDb() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactoryOldDb().getObject());
        return transactionManager;
    }

}