package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.task;

import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValueAndRemove;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.getFilterInFirstValue;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.getFilterMatchFirstValueAndRemove;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.api.search.SearchOperation;
import cloud.xcan.sdf.core.angustester.application.converter.TaskConverter;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskListRepo;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskCount;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Query;
import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

/**
 * @author xiaolong.liu
 */
@Repository
public class TaskListRepoMysql extends AbstractSearchRepository<Task> implements TaskListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), step, criterias, "task", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    // DISTINCT for repeated comments
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis);

    // Assemble non mainClass assigneeIds in Conditions
    sql.append(assembleTagJoinCondition(criterias))
        .append(assembleFavouriteByJoinCondition(criterias))
        .append(assembleFollowByJoinCondition(criterias))
        .append(" WHERE 1=1 ")
        // Assemble mainClass Conditions
        .append(assembleCommentByInCondition(criterias))
        .append(getCriteriaAliasCondition(step, criterias, mainAlis, mode, false, matches));
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params) {
    return "a.*";
  }

  @Override
  public TaskCount count(Set<SearchCriteria> criterias) {
    // @formatter:off
    /*******************************************************
     * status  | test_type | task_type | exec_result | num
     * *****************************************************
     * PENDING	FUNCTIONAL		                    1
     * PENDING	FUNCTIONAL	API_TEST	            2
     * PENDING	PERF		                          6
     * PENDING	STABILITY                         1
     ******************************************************/
    StringBuilder groupBySql = new StringBuilder("SELECT a.`status`,a.`test_type`,a.task_type,a.exec_result,COUNT(*) num FROM task a ");
    StringBuilder overdueSql = new StringBuilder("SELECT COUNT(a.id) FROM task a ");
    StringBuilder oneTimePassSql = new StringBuilder("SELECT COUNT(a.id) FROM task a ");
    StringBuilder sumNumSql = new StringBuilder("SELECT SUM(a.total_num), SUM(a.fail_num), SUM(a.eval_workload), SUM(a.actual_workload) FROM task a ");
    StringBuilder completedWorkloadSql = new StringBuilder("SELECT SUM(a.actual_workload) FROM task a ");

    StringBuilder joinAssignee = new StringBuilder();
    StringBuilder joinConformor = new StringBuilder();
    StringBuilder joinTag = new StringBuilder();
    StringBuilder mainCondition = new StringBuilder();
    String matchValue = "";
    if (isNotEmpty(criterias)) {
      // Assemble non mainClass tagIds in Conditions
      joinTag = assembleTagJoinCondition(criterias);

      matchValue = getFilterMatchFirstValueAndRemove(criterias, "name");
      matchValue = detectFulltextSearchValue(matchValue);

      // Assemble mainClass Conditions
      SingleTableEntityPersister step = getSingleTableEntityPersister();
      mainCondition = getCriteriaAliasCondition(step, criterias, "a",
          getSearchMode(), false);
    }

    groupBySql.append(joinAssignee).append(joinConformor).append(joinTag)
        .append(" WHERE 1=1 ").append(mainCondition)
        .append(getMatchCondition(matchValue))
        .append(" GROUP BY a.`status`,a.test_type,a.task_type,a.exec_result "); // Use Covering Index
    overdueSql.append(joinAssignee).append(joinConformor).append(joinTag)
        .append(" WHERE 1=1 AND a.overdue_flag = 1 ").append(mainCondition)
        .append(getMatchCondition(matchValue));
    oneTimePassSql.append(joinAssignee).append(joinConformor).append(joinTag)
        .append(" WHERE a.status = 'COMPLETED' AND a.fail_num = 0 ").append(mainCondition)
        .append(getMatchCondition(matchValue));
    sumNumSql.append(joinAssignee).append(joinConformor).append(joinTag)
        .append(" WHERE 1=1 ").append(mainCondition)
        .append(getMatchCondition(matchValue));
    completedWorkloadSql.append(joinAssignee).append(joinConformor).append(joinTag)
        .append(" WHERE a.status = 'COMPLETED' ").append(mainCondition)
        .append(getMatchCondition(matchValue));

    MetamodelImpl metaModel = (MetamodelImpl) getEntityManager().getMetamodel();
    Map<String, EntityPersister> entityPm = metaModel.entityPersisters();
    SingleTableEntityPersister step = (SingleTableEntityPersister) entityPm
        .get(Task.class.getName());
    Query groupByQueryResult = entityManager.createNativeQuery(groupBySql.toString());
    Query overdueQueryResult = entityManager.createNativeQuery(overdueSql.toString());
    Query oneTimePassQueryResult = entityManager.createNativeQuery(oneTimePassSql.toString());
    Query sumNumSqlQueryResult = entityManager.createNativeQuery(sumNumSql.toString());
    Query completedWorkloadQueryResult = entityManager.createNativeQuery(completedWorkloadSql.toString());

    if (isNotEmpty(criterias)) {
      setQueryParameter(step, groupByQueryResult, criterias, Task.class);
      setQueryParameter(step, overdueQueryResult, criterias, Task.class);
      setQueryParameter(step, oneTimePassQueryResult, criterias, Task.class);
      setQueryParameter(step, sumNumSqlQueryResult, criterias, Task.class);
      setQueryParameter(step, completedWorkloadQueryResult, criterias, Task.class);
    }
    return TaskConverter.objectArrToTaskCount(
        (List<Object[]>) groupByQueryResult.getResultList(),
        (List<Object[]>) overdueQueryResult.getResultList(),
        (List<Object[]>) oneTimePassQueryResult.getResultList(),
        (List<Object[]>) sumNumSqlQueryResult.getResultList(),
        (List<Object[]>) completedWorkloadQueryResult.getResultList()
        );
    // @formatter:on
  }

  private SingleTableEntityPersister getSingleTableEntityPersister() {
    MetamodelImpl metaModel = (MetamodelImpl) this.getEntityManager().getMetamodel();
    Map<String, EntityPersister> entityPm = metaModel.entityPersisters();
    return (SingleTableEntityPersister) entityPm.get(Task.class.getName());
  }

  private StringBuilder assembleTagJoinCondition(Set<SearchCriteria> criterias) {
    StringBuilder sql = new StringBuilder();
    String tagIdInValue = getFilterInFirstValue(criterias, "tagId");
    String tagIdEqualValue = findFirstValue(criterias, "tagId", SearchOperation.EQUAL);
    if (isEmpty(tagIdInValue) && isEmpty(tagIdEqualValue)) {
      return sql;
    }
    sql.append(" INNER join tag_target t3 on a.id = t3.target_id");
    if (isNotBlank(tagIdInValue)) {
      sql.append(" AND t3.tag_id in (").append(tagIdInValue).append(")");
    }
    if (isNotBlank(tagIdEqualValue)) {
      sql.append(" AND t3.tag_id = ").append(tagIdEqualValue);
    }
    return sql;
  }

  private String getMatchCondition(String matchValue) {
    return isNotEmpty(matchValue) ? " AND MATCH (a.name, a.code) AGAINST ('" + matchValue
        + "' IN BOOLEAN MODE) " : "";
  }

  private StringBuilder assembleFavouriteByJoinCondition(Set<SearchCriteria> criterias) {
    StringBuilder sql = new StringBuilder();
    String favouriteByEqualValue = findFirstValueAndRemove(criterias, "favouriteBy",
        SearchOperation.EQUAL);
    if (isEmpty(favouriteByEqualValue)) {
      return sql;
    }
    sql.append(" INNER join task_favourite t3 on a.id = t3.task_id");
    if (isNotBlank(favouriteByEqualValue)) {
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
    sql.append(" INNER join task_follow t3 on a.id = t3.task_id");
    if (isNotBlank(followByEqualValue)) {
      sql.append(" AND t3.created_by = ").append(followByEqualValue);
    }
    return sql;
  }

  public static StringBuilder assembleCommentByInCondition(Set<SearchCriteria> criterias) {
    StringBuilder sql = new StringBuilder();
    String followByEqualValue = findFirstValueAndRemove(criterias, "commentBy",
        SearchOperation.EQUAL);
    if (isEmpty(followByEqualValue)) {
      return sql;
    }
    sql.append(" AND a.id IN (SELECT t3.target_id FROM comment t3 WHERE t3.user_id = ")
        .append(followByEqualValue).append(")");
    return sql;
  }
}
