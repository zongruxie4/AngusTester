package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.scenario;

import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.assembleAuthJoinTargetCondition;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValueAndRemove;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.api.search.SearchOperation;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioListRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import cloud.xcan.sdf.spec.utils.StringUtils;
import java.util.Set;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

@Repository
public class ScenarioListRepoMySql extends AbstractSearchRepository<Scenario> implements
    ScenarioListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), step, criterias, "scenario", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis);

    // Assemble non mainClass assigneeIds in Conditions
    sql.append(assembleFavouriteByJoinCondition(criterias))
        .append(assembleFollowByJoinCondition(criterias))
        .append(" WHERE 1=1 ")
        // Assemble mainClass Conditions
        .append(getCriteriaAliasCondition(step, criterias, mainAlis, mode, false, matches));

    // Assemble non mainClass authObjectId and grantFlag Conditions
    assembleAuthJoinTargetCondition(CombinedTargetType.SCENARIO.getValue(), sql, criterias);
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params) {
    return "a.id, a.name, a.description, a.project_id, a.auth_flag, a.plugin, a.script_type, a.script_id, a.created_by, a.created_date, a.last_modified_by, a.last_modified_date";
  }

  private StringBuilder assembleFavouriteByJoinCondition(Set<SearchCriteria> criterias) {
    StringBuilder sql = new StringBuilder();
    String favouriteByEqualValue = findFirstValueAndRemove(criterias, "favouriteBy",
        SearchOperation.EQUAL);
    if (isEmpty(favouriteByEqualValue)) {
      return sql;
    }
    sql.append(" INNER join scenario_favourite t3 on a.id = t3.scenario_id");
    if (StringUtils.isNotBlank(favouriteByEqualValue)) {
      sql.append(" AND t3.created_by = ").append(favouriteByEqualValue);
    }
    return sql;
  }

  private StringBuilder assembleFollowByJoinCondition(Set<SearchCriteria> criterias) {
    StringBuilder sql = new StringBuilder();
    String followByEqualValue = findFirstValueAndRemove(criterias, "followBy",
        SearchOperation.EQUAL);
    if (isEmpty(followByEqualValue)) {
      return sql;
    }
    sql.append(" INNER join scenario_follow t3 on a.id = t3.scenario_id");
    if (StringUtils.isNotBlank(followByEqualValue)) {
      sql.append(" AND t3.created_by = ").append(followByEqualValue);
    }
    return sql;
  }

}
