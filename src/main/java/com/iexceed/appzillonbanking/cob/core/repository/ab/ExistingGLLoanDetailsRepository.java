package com.iexceed.appzillonbanking.cob.core.repository.ab;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.iexceed.appzillonbanking.cob.core.domain.ab.ExistingGLLoanDetails;

public interface ExistingGLLoanDetailsRepository extends CrudRepository<ExistingGLLoanDetails, BigDecimal> {

	List<ExistingGLLoanDetails> findAll();

	List<ExistingGLLoanDetails> findByCustomerId(String customerId);

}
