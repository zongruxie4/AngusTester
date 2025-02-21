package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.indicator;

import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.assembleTargetNameLikeCondition;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerfListRepo;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStability;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStabilityListRepo;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import java.util.Set;
import javax.annotation.Resource;
import org.hibernate.persister.entity.SingleTableEntityPersister;
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
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, Object[] params, String... matches) {
    StringBuilder sql = indicatorPerfListRepo.getTargetSqlTemplate0(getSearchMode(), step,
        criteria, "indicator_stability", matches);

    // Assemble non mainClass match Conditions
    assembleTargetNameLikeCondition(criteria, sql);
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    String targetTypeValue = findFirstValue(criteria, "targetType");
    ProtocolAssert.assertNotNull(targetTypeValue, "targetType is required");
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
