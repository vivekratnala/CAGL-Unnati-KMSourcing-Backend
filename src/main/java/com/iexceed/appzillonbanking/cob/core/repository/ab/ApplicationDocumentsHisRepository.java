package com.iexceed.appzillonbanking.cob.core.repository.ab;

import com.iexceed.appzillonbanking.cob.core.domain.ab.ApplicationDocumentsHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ApplicationDocumentsHisRepository extends CrudRepository<ApplicationDocumentsHistory, BigDecimal> { 

}