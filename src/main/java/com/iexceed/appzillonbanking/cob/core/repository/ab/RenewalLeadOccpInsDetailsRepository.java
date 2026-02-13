package com.iexceed.appzillonbanking.cob.core.repository.ab;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.iexceed.appzillonbanking.cob.core.domain.ab.RenewalLeadOccpInsDetails;


public interface RenewalLeadOccpInsDetailsRepository extends CrudRepository<RenewalLeadOccpInsDetails, BigDecimal> {

	List<RenewalLeadOccpInsDetails> findAll();
	
	Optional<RenewalLeadOccpInsDetails> findByCustomerIdAndPid(String customerId,String pid);
	Optional<RenewalLeadOccpInsDetails> findByPid(String pid);
	
}
