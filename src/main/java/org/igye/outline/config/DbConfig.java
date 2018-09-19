package org.igye.outline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DbConfig {
    @Value("${db.url}")
    private String dbUrl;

    @Value("${db.user}")
    private String dbUser;

    @Value("${db.pwd}")
    private String dbPwd;

//    @Bean
//    public DataSource dataSource() {
//        JdbcDataSource ds = new JdbcDataSource();
//        ds.setURL(dbUrl);
//        ds.setUser(dbUser);
//        ds.setPassword(dbPwd);
//        return ds;
//    }

//    @Bean
//    @Primary
//    public LocalSessionFactoryBean entityManagerFactory() {
//        LocalSessionFactoryBean res = new LocalSessionFactoryBean();
//        res.setDataSource(dataSource());
//        res.setPackagesToScan("org.igye.outline.model", "org.igye.outline.modelv2");
//        Properties props = new Properties();
//        props.put("hibernate.dialect", Database.H2);
//        props.put("hibernate.format_sql", "true");
//        props.put("hibernate.use_sql_comments", "true");
////        props.put("hibernate.show_sql", "true");
////        props.put("hibernate.hbm2ddl.auto", "validate");
//        res.setHibernateProperties(props);
//        return res;
//    }

//    @Bean
//    public EntityManager entityManager(LocalSessionFactoryBean localSessionFactoryBean) {
//        return localSessionFactoryBean.getObject().createEntityManager();
//    }

//    @Bean
//    @Primary
//    public HibernateTransactionManager transactionManager() {
//        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
//        transactionManager.setSessionFactory(entityManagerFactory().getObject());
//        return transactionManager;
//    }
}
