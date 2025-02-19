package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.project;

import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.getFilterInFirstValue;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.getInConditionValue;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.project.ProjectListRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.Set;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectListRepoMySql extends AbstractSearchRepository<Project> implements
    ProjectListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), step, criterias, "project", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder(
        "SELECT %s FROM " + tableName + " " + mainAlis + " WHERE 1=1 ");

    // Assemble mainClass Conditions
    sql.append(getCriteriaAliasCondition(step, criterias, mainAlis, mode, false, matches));

    // Assemble non mainClass members conditions
    assembleMembersJoinTargetCondition(sql, criterias);
    return sql;
  }

  private void assembleMembersJoinTargetCondition(StringBuilder sql,
      Set<SearchCriteria> criterias) {
    String authObjectIds = getFilterInFirstValue(criterias, "authObjectIds");
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
  public String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params) {
    return "a.*";
  }

}
