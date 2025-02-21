package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.indicator;

import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.assembleAuthJoinTargetCondition;
import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.assembleIndicatorJoinTargetSql;
import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.assembleTargetNameLikeCondition;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerf;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerfListRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

@Repository
public class IndicatorPerfListRepoMysql extends AbstractSearchRepository<IndicatorPerf> implements
    IndicatorPerfListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, Object[] params, String... matches) {
    StringBuilder sql = getTargetSqlTemplate0(getSearchMode(), step, criteria,
        "indicator_perf", matches);

    // Assemble non mainClass match Conditions
    assembleTargetNameLikeCondition(criteria, sql);
    return sql;
  }

  @Override
  public StringBuilder getTargetSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String targetTypeValue = findFirstValue(criteria, "targetType");
    assertNotNull(targetTypeValue, "targetType is required");

    String mainAlis = "ip";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis);

    // Assemble non mainClass join table
    assembleIndicatorJoinTargetSql(targetTypeValue, sql);

    // Assemble mainClass Conditions
    sql.append(getCriteriaAliasCondition(step, criteria, mainAlis, mode, false, matches));

    // Assemble non mainClass authObjectId Conditions
    assembleAuthJoinTargetCondition(targetTypeValue, sql, criteria);
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    String targetTypeValue = findFirstValue(criteria, "targetType");
    assertNotNull(targetTypeValue, "targetType is required");
    CombinedTargetType type = CombinedTargetType.valueOf(targetTypeValue);
    if (type == CombinedTargetType.SCENARIO) {
      return "ip.id,ip.target_id,ip.threads,ip.duration,ip.ramp_up_threads,ip.ramp_up_interval,ip.art,ip.percentile,ip.tps,ip.error_rate,ip.created_by,ip.created_date,a.name targetName,'SCENARIO'";
    }
    if (type == CombinedTargetType.API) {
      return "ip.id,ip.target_id,ip.threads,ip.duration,ip.ramp_up_threads,ip.ramp_up_interval,ip.art,ip.percentile,ip.tps,ip.error_rate,ip.created_by,ip.created_date,a.summary targetName,'API'";
    }
    // CombinedTargetType.SERVICE
    //return "ip.id,ip.target_id,ip.threads,ip.duration,ip.ramp_up_threads,ip.ramp_up_interval,ip.art,ip.percentile,ip.tps,ip.error_rate,ip.created_by,ip.created_date,a.name targetName, 'SERVICE'";
    return null;
  }

}
