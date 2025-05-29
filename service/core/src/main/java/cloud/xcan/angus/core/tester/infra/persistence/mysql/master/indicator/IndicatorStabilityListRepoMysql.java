package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.indicator;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleTargetNameLikeCondition;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerfListRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStability;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStabilityListRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * Complete assembly custom sql method reference: {@link IndicatorPerfListRepoMysql}
 */
@Repository
public class IndicatorStabilityListRepoMysql extends
    AbstractSearchRepository<IndicatorStability> implements IndicatorStabilityListRepo {

  @Resource
  private IndicatorPerfListRepo indicatorPerfListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria,
      Class<IndicatorStability> mainClz, Object[] params, String... matches) {
    StringBuilder sql = indicatorPerfListRepo.getTargetSqlTemplate0(getSearchMode(), mainClz,
        criteria, "indicator_stability", matches);

    // Assemble non mainClass match Conditions
    assembleTargetNameLikeCondition(criteria, sql);
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    String targetTypeValue = findFirstValue(criteria, "targetType");
    assertNotNull(targetTypeValue, "targetType is required");
    CombinedTargetType type = CombinedTargetType.valueOf(targetTypeValue);
    if (type == CombinedTargetType.SCENARIO) {
      return "ip.id,ip.target_id,ip.threads,ip.duration,ip.art,ip.percentile,ip.tps,ip.error_rate,ip.cpu,ip.memory,ip.disk,ip.network,ip.created_by,ip.created_date,a.name as targetName,'SCENARIO'";
    }
    if (type == CombinedTargetType.API) {
      return "ip.id,ip.target_id,ip.threads,ip.duration,ip.art,ip.percentile,ip.tps,ip.error_rate,ip.cpu,ip.memory,ip.disk,ip.network,ip.created_by,ip.created_date,a.summary as targetName,'API'";
    }
    //return "ip.id,ip.target_id,ip.threads,ip.duration,ip.art,ip.percentile,ip.tps,ip.error_rate,ip.cpu,ip.memory,ip.disk,ip.network,ip.created_by,ip.created_date,a.name as targetName,'SERVICE'";
    return null;
  }

}
