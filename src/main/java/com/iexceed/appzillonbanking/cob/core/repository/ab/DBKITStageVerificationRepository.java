package com.iexceed.appzillonbanking.cob.core.repository.ab;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cob.core.domain.ab.DBKITStageVerification;


@Repository
public interface DBKITStageVerificationRepository extends CrudRepository<DBKITStageVerification, String>{

}
