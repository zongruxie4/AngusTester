package cloud.xcan.sdf.core.angustester.infra.search;


import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.assembleTargetNameMatchCondition;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerf;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerfListRepo;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerfSearchRepo;
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
public class IndicatorPerfSearchRepoMysql extends AbstractSearchRepository<IndicatorPerf>
    implements IndicatorPerfSearchRepo {

  @Resource
  private IndicatorPerfListRepo indicatorPerfListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... matches) {
    String targetTypeValue = findFirstValue(criterias, "targetType");
    StringBuilder sql = indicatorPerfListRepo.getTargetSqlTemplate0(getSearchMode(),
        step, criterias, "indicator_perf", matches);

    // Assemble non mainClass match Conditions
    assembleTargetNameMatchCondition(criterias, targetTypeValue, sql);
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params) {
    return indicatorPerfListRepo.getReturnFieldsCondition(criterias, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }
}
