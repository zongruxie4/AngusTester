package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.indicator;

import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.assembleTargetNameLikeCondition;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFunc;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFuncListRepo;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerfListRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import java.util.Set;
import javax.annotation.Resource;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

@Repository
public class IndicatorFuncListRepoMysql extends AbstractSearchRepository<IndicatorFunc> implements
    IndicatorFuncListRepo {

  @Resource
  private IndicatorPerfListRepo indicatorPerfListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, Object[] params, String... matches) {
    StringBuilder sql = indicatorPerfListRepo.getTargetSqlTemplate0(getSearchMode(), step,
        criteria, "indicator_func", matches);

    // Assemble non mainClass match Conditions
    assembleTargetNameLikeCondition(criteria, sql);
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    String targetTypeValue = findFirstValue(criteria, "targetType");
    assertNotNull(targetTypeValue, "targetType is required");
    CombinedTargetType type = CombinedTargetType.valueOf(targetTypeValue);
    if (type == CombinedTargetType.API) {
      return "ip.id,ip.target_id,ip.smoke,ip.smoke_check_setting,ip.user_defined_smoke_assertion,ip.security,ip.security_check_setting,ip.user_defined_security_assertion,ip.created_by,ip.created_date,a.summary targetName,'API'";
    }
    //return "ip.id,ip.target_id,ip.smoke,ip.smoke_check_setting,ip.user_defined_smoke_assertion,ip.security,ip.security_check_setting,ip.user_defined_security_assertion,ip.created_by,ip.created_date,a.name targetName, 'SERVICE'";
    return null;
  }

}
