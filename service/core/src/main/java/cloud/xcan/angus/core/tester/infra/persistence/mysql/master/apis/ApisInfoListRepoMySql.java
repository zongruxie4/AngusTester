package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.apis;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValueAndRemove;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleAuthJoinTargetCondition;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisInfoListRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.utils.StringUtils;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class ApisInfoListRepoMySql extends AbstractSearchRepository<ApisBasicInfo> implements
    ApisInfoListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<ApisBasicInfo> mainClz,
      Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), mainClz, criteria, "apis", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, Class<ApisBasicInfo> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis);

    // Assemble non mainClass assigneeIds in Conditions
    sql.append(assembleFavouriteByJoinCondition(criteria))
        .append(assembleFollowByJoinCondition(criteria))
        .append(" WHERE 1=1 ")
        .append(getCriteriaAliasCondition(criteria, mainClz, mainAlis, mode, false, matches));

    // Assemble non mainClass authObjectId and grant Conditions
    assembleAuthJoinTargetCondition(CombinedTargetType.API.getValue(), sql, criteria);
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return
        "a.id,a.summary,a.operation_id,a.project_id,a.auth,a.service_auth,a.source,a.import_source,a.owner_id,"
            + "a.created_by,a.created_date,a.last_modified_by,a.last_modified_date,a.endpoint,a.method,a.status,a.tags,a.protocol,a.deprecated,"
            + "a.test_func,a.test_func_passed,a.test_func_failure_message,a.test_perf,a.test_perf_passed,a.test_perf_failure_message,a.test_stability,a.test_stability_passed,a.test_stability_failure_message";
  }

  private StringBuilder assembleFavouriteByJoinCondition(Set<SearchCriteria> criteria) {
    StringBuilder sql = new StringBuilder();
    String favouriteByEqualValue = findFirstValueAndRemove(criteria, "favouriteBy",
        SearchOperation.EQUAL);
    if (isEmpty(favouriteByEqualValue)) {
      return sql;
    }
    sql.append(" INNER join apis_favourite t3 on a.id = t3.apis_id");
    if (StringUtils.isNotBlank(favouriteByEqualValue)) {
      sql.append(" AND t3.created_by = ").append(favouriteByEqualValue);
    }
    return sql;
  }

  private StringBuilder assembleFollowByJoinCondition(Set<SearchCriteria> criteria) {
    StringBuilder sql = new StringBuilder();
    String followByEqualValue = findFirstValueAndRemove(criteria, "followBy",
        SearchOperation.EQUAL);
    if (isEmpty(followByEqualValue)) {
      return sql;
    }
    sql.append(" INNER join apis_follow t3 on a.id = t3.apis_id");
    if (StringUtils.isNotBlank(followByEqualValue)) {
      sql.append(" AND t3.created_by = ").append(followByEqualValue);
    }
    return sql;
  }

}
