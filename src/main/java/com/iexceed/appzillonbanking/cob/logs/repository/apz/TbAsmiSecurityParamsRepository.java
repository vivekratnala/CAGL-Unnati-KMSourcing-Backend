package com.iexceed.appzillonbanking.cob.logs.repository.apz;

import com.iexceed.appzillonbanking.cob.logs.domain.apz.TbAsmiSecurityParams;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TbAsmiSecurityParamsRepository extends JpaRepository<TbAsmiSecurityParams, String>, JpaSpecificationExecutor<TbAsmiSecurityParams>{
	
	@Cacheable(value = "securityParams", key = "#appId")
	@Query("SELECT c FROM TbAsmiSecurityParams as c WHERE c.appId =:appId")
	TbAsmiSecurityParams findSecurityParamsbyAppId(@Param("appId") String appId);
}
