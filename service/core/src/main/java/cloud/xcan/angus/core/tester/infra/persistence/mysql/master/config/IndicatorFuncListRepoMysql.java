package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.config;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleTargetNameLikeCondition;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorFunc;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorFuncListRepo;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorPerfListRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
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
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<IndicatorFunc> mainClz,
      Object[] params, String... matches) {
    StringBuilder sql = indicatorPerfListRepo.getTargetSqlTemplate0(getSearchMode(), mainClz,
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
