package com.iexceed.appzillonbanking.cob;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableTransactionManagement
@PropertySource("file:${dbProperties.path}")
@EnableJpaRepositories(entityManagerFactoryRef = "abCOBEntityManagerFactory", transactionManagerRef = "abCOBTransactionManager", 
basePackages = {
		"com.iexceed.appzillonbanking.*.repository.ab",
		"com.iexceed.appzillonbanking.*.*.repository.ab"})
public class SpringCOBDataConfiguration {
	@Value("${springcob.datasource.hibernate.dialect}")
	private String dialect;

	@Primary
	@Bean(name = "abCOBDataSource")
	@ConfigurationProperties(prefix = "springcob.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Primary
	@Bean(name = "abCOBEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean barEntityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier("abCOBDataSource") DataSource abCOBDataSource) {

				Map<String, Object> properties1 = new HashMap<>();
				properties1.put("hibernate.dialect", dialect);
				return builder.dataSource(abCOBDataSource).packages("com.iexceed.appzillonbanking.*.domain.ab",
															"com.iexceed.appzillonbanking.*.*.domain.ab").persistenceUnit("abdatacob").properties(properties1).build();
	}

	@Primary
	@Bean(name = "abCOBTransactionManager")
	public PlatformTransactionManager abTransactionManager(
			@Qualifier("abCOBEntityManagerFactory") EntityManagerFactory abCOBEntityManagerFactory) {
		return new JpaTransactionManager(abCOBEntityManagerFactory);
	}
}
