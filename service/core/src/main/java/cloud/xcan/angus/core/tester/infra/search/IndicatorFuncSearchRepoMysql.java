package cloud.xcan.angus.core.tester.infra.search;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleTargetNameMatchCondition;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFunc;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFuncListRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFuncSearchRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerfListRepo;
import cloud.xcan.angus.core.tester.infra.persistence.mysql.master.indicator.IndicatorPerfListRepoMysql;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * Complete assembly custom sql method reference: {@link IndicatorPerfListRepoMysql}
 */
@Repository
public class IndicatorFuncSearchRepoMysql extends
    AbstractSearchRepository<IndicatorFunc> implements IndicatorFuncSearchRepo {

  @Resource
  private IndicatorFuncListRepo indicatorFuncListRepo;

  @Resource
  private IndicatorPerfListRepo indicatorPerfListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<IndicatorFunc> mainClz,
      Object[] params, String... matches) {
    String targetTypeValue = findFirstValue(criteria, "targetType");
    StringBuilder sql = indicatorPerfListRepo.getTargetSqlTemplate0(getSearchMode(), mainClz,
        criteria, "indicator_func", matches);

    // Assemble non mainClass match Conditions
    assembleTargetNameMatchCondition(criteria, targetTypeValue, sql);
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return indicatorFuncListRepo.getReturnFieldsCondition(criteria, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }
}
