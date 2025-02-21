package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.apis;

import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.assembleAuthJoinTargetCondition;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValueAndRemove;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.api.search.SearchOperation;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBasicInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisInfoListRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import cloud.xcan.sdf.spec.utils.StringUtils;
import java.util.Set;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

@Repository
public class ApisInfoListRepoMySql extends AbstractSearchRepository<ApisBasicInfo> implements
    ApisInfoListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), step, criteria, "apis", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis);

    // Assemble non mainClass assigneeIds in Conditions
    sql.append(assembleFavouriteByJoinCondition(criteria))
        .append(assembleFollowByJoinCondition(criteria))
        .append(" WHERE 1=1 ")
        .append(getCriteriaAliasCondition(step, criteria, mainAlis, mode, false, matches));

    // Assemble non mainClass authObjectId and grantFlag Conditions
    assembleAuthJoinTargetCondition(CombinedTargetType.API.getValue(), sql, criteria);
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return "a.id,a.summary,a.operation_id,a.project_id,a.auth_flag,a.service_auth_flag,a.source,a.import_source,a.owner_id,"
        + "a.created_by,a.created_date,a.last_modified_by,a.last_modified_date,a.endpoint,a.method,a.status,a.tags,a.protocol,a.deprecated,"
        + "a.test_func_flag,a.test_func_passed_flag,a.test_func_failure_message,a.test_perf_flag,a.test_perf_passed_flag,a.test_perf_failure_message,a.test_stability_flag,a.test_stability_passed_flag,a.test_stability_failure_message";
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
