package org.igye.outline;

import org.hibernate.dialect.Database;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

@Configuration
@EnableTransactionManagement
public class HibernateTestConfigClass {
    @Bean
    public EmbeddedDatabase dataSource() {
        EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(H2)
                .setScriptEncoding("UTF-8")
                .ignoreFailedDrops(true)
//                    .addScript("schema.sql")
//                    .addScripts("user_data.sql", "country_data.sql")
                .build();

        return db;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean res = new LocalSessionFactoryBean();
        res.setDataSource(dataSource);
        res.setPackagesToScan("org.igye.outline.model");
        Properties props = new Properties();
        props.put("hibernate.dialect", Database.H2);
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.use_sql_comments", "true");
//            props.put("hibernate.show_sql", "true");
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        res.setHibernateProperties(props);
        return res;
    }

    @Bean
    public HibernateTransactionManager transactionManager(DataSource dataSource) {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory(dataSource).getObject());
        return transactionManager;
    }
}
