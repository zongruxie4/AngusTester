package cloud.xcan.angus.core.tester.infra.search;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleTargetNameMatchCondition;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorPerfListRepo;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorStability;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorStabilityListRepo;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorStabilitySearchRepo;
import cloud.xcan.angus.core.tester.infra.persistence.mysql.master.indicator.IndicatorPerfListRepoMysql;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
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
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria,
      Class<IndicatorStability> mainClz, Object[] params, String... matches) {
    String targetTypeValue = findFirstValue(criteria, "targetType");
    StringBuilder sql = indicatorPerfListRepo.getTargetSqlTemplate0(getSearchMode(), mainClz,
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
