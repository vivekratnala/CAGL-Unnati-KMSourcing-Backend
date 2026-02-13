package com.iexceed.appzillonbanking.cob.core.repository.ab;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cob.core.domain.ab.BCMPIStageVerification;

@Repository
public interface BCMPIStageVerificationRepository extends CrudRepository<BCMPIStageVerification, String> {

}
