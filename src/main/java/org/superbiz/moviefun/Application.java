package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials dbCred(@Value("${VCAP_SERVICES}") String vcapServices) {
        return new DatabaseServiceCredentials(vcapServices);
    }

    @Bean("moviesDataSource")
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(serviceCredentials.jdbcUrl("movies-mysql", "p-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        return hikariDataSource;
    }

    @Bean("albumsDataSource")
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(serviceCredentials.jdbcUrl("albums-mysql", "p-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        return hikariDataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter(){
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean("movieEM")
    public LocalContainerEntityManagerFactoryBean movieEM(@Qualifier("moviesDataSource") DataSource dataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter){
        LocalContainerEntityManagerFactoryBean lcemfb = new LocalContainerEntityManagerFactoryBean();
        lcemfb.setDataSource(dataSource);
        lcemfb.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        lcemfb.setPackagesToScan("org.superbiz.moviefun.movies");
        lcemfb.setPersistenceUnitName("movies");
        return lcemfb;
    }

    @Bean("albumEM")
    public LocalContainerEntityManagerFactoryBean albumEM(@Qualifier("albumsDataSource")DataSource dataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter){
        LocalContainerEntityManagerFactoryBean lcemfb = new LocalContainerEntityManagerFactoryBean();
        lcemfb.setDataSource(dataSource);
        lcemfb.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        lcemfb.setPackagesToScan("org.superbiz.moviefun.albums");
        lcemfb.setPersistenceUnitName("albums");

        return lcemfb;
    }

    @Bean("moviesTM")
    public PlatformTransactionManager moviesTM(@Qualifier("movieEM") LocalContainerEntityManagerFactoryBean  lcEMgr) {
        JpaTransactionManager pTM = new JpaTransactionManager(lcEMgr.getObject());
        return pTM;
    }

    @Bean("albumsTM")
    public PlatformTransactionManager albumsTM(@Qualifier("albumEM") LocalContainerEntityManagerFactoryBean  lcEMgr) {
        JpaTransactionManager pTM = new JpaTransactionManager(lcEMgr.getObject());
        return pTM;
    }
}
