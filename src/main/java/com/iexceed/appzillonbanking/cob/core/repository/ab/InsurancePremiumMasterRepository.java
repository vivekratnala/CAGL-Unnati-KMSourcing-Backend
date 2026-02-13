package com.iexceed.appzillonbanking.cob.core.repository.ab;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cob.core.domain.ab.InsurancePremiumMaster;


@Repository
public interface InsurancePremiumMasterRepository extends JpaRepository<InsurancePremiumMaster, Integer> {

}
