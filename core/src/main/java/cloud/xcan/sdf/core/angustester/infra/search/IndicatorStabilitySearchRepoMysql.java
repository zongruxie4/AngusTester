package cloud.xcan.sdf.core.angustester.infra.search;

import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.assembleTargetNameMatchCondition;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerfListRepo;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStability;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStabilityListRepo;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStabilitySearchRepo;
import cloud.xcan.sdf.core.angustester.infra.persistence.mysql.indicator.IndicatorPerfListRepoMysql;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import javax.annotation.Resource;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

/**
 * Complete assembly custom sql method reference: {@link IndicatorPerfListRepoMysql}
 */
@Repository
public class IndicatorStabilitySearchRepoMysql extends
    AbstractSearchRepository<IndicatorStability> implements IndicatorStabilitySearchRepo {

  @Resource
  private IndicatorStabilityListRepo indicatorStabilityListRepo;

  @Resource
  private IndicatorPerfListRepo indicatorPerfListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, Object[] params, String... matches) {
    String targetTypeValue = findFirstValue(criteria, "targetType");
    StringBuilder sql = indicatorPerfListRepo.getTargetSqlTemplate0(getSearchMode(), step,
        criteria, "indicator_stability", matches);

    // Assemble non mainClass match Conditions
    assembleTargetNameMatchCondition(criteria, targetTypeValue, sql);
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return indicatorStabilityListRepo.getReturnFieldsCondition(criteria, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }
}
