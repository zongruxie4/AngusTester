package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.indicator;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleAuthJoinTargetCondition;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleIndicatorJoinTargetSql;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleTargetNameLikeCondition;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerf;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerfListRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class IndicatorPerfListRepoMysql extends AbstractSearchRepository<IndicatorPerf> implements
    IndicatorPerfListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<IndicatorPerf> mainClz,
      Object[] params, String... matches) {
    StringBuilder sql = getTargetSqlTemplate0(getSearchMode(), mainClz, criteria,
        "indicator_perf", matches);

    // Assemble non mainClass match Conditions
    assembleTargetNameLikeCondition(criteria, sql);
    return sql;
  }

  @Override
  public StringBuilder getTargetSqlTemplate0(SearchMode mode, Class mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String targetTypeValue = findFirstValue(criteria, "targetType");
    assertNotNull(targetTypeValue, "targetType is required");

    String mainAlis = "ip";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis);

    // Assemble non mainClass join table
    assembleIndicatorJoinTargetSql(targetTypeValue, sql);

    // Assemble mainClass Conditions
    sql.append(getCriteriaAliasCondition(criteria, mainClz, mainAlis, mode, false, matches));

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
