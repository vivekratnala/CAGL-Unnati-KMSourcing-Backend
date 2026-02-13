package com.iexceed.appzillonbanking.cob;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableTransactionManagement
@PropertySource("file:${dbProperties.path}")
@EnableJpaRepositories(entityManagerFactoryRef = "apzCOBEntityManagerFactory", transactionManagerRef = "apzCOBTransactionManager", 
basePackages = {
		"com.iexceed.appzillonbanking.*.repository.apz", "com.iexceed.appzillonbanking.*.*.repository.apz"})
public class ApzCOBDataConfiguration {
	@Bean(name = "apzCOBDataSource")
	@ConfigurationProperties(prefix = "apzcob.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "apzCOBEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean barEntityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier("apzCOBDataSource") DataSource apzCOBDataSource) {
		return builder.dataSource(apzCOBDataSource).packages("com.iexceed.appzillonbanking.*.domain.apz", "com.iexceed.appzillonbanking.*.*.domain.apz").persistenceUnit("apzdatacob").build();
	}

	@Bean(name = "apzCOBTransactionManager")
	public PlatformTransactionManager apzTransactionManager(
			@Qualifier("apzCOBEntityManagerFactory") EntityManagerFactory apzCOBEntityManagerFactory) {
		return new JpaTransactionManager(apzCOBEntityManagerFactory);
	}
}
