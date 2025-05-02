package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.project;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.getFilterInFirstValue;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.getInConditionValue;
import static org.springframework.util.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.ProjectListRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectListRepoMySql extends AbstractSearchRepository<Project> implements
    ProjectListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<Project> mainClz,
      Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), mainClz, criteria, "project", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, Class<Project> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder(
        "SELECT %s FROM " + tableName + " " + mainAlis + " WHERE 1=1 ");

    // Assemble mainClass Conditions
    sql.append(getCriteriaAliasCondition(criteria, mainClz, mainAlis, mode, false, matches));

    // Assemble non mainClass members conditions
    assembleMembersJoinTargetCondition(sql, criteria);
    return sql;
  }

  private void assembleMembersJoinTargetCondition(StringBuilder sql,
      Set<SearchCriteria> criteria) {
    String authObjectIds = getFilterInFirstValue(criteria, "authObjectIds");
    if (isEmpty(authObjectIds)) {
      // Admin query all resource when authObjectIds is empty
      return;
    }
    String authObjectIdsInValue = getInConditionValue(authObjectIds);
    sql.append(
            " AND a.id IN (SELECT a1.id FROM project a1 INNER JOIN project_members a2 ON a2.id = a3.project_id AND a1.tenant_id = ")
        .append(" AND a2.member_id IN ").append(authObjectIdsInValue).append(")");
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return "a.*";
  }

}
