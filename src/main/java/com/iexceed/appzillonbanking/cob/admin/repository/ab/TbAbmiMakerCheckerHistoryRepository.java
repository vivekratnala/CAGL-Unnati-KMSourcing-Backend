package com.iexceed.appzillonbanking.cob.admin.repository.ab;


import com.iexceed.appzillonbanking.cob.admin.domain.ab.TbAbmiMakerCheckerHistory;
import com.iexceed.appzillonbanking.cob.admin.domain.ab.TbAbmiMakerCheckerHistoryIds;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TbAbmiMakerCheckerHistoryRepository extends CrudRepository<TbAbmiMakerCheckerHistory, TbAbmiMakerCheckerHistoryIds> {

}
