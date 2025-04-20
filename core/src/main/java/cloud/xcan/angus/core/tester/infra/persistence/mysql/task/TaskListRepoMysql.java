package cloud.xcan.angus.core.tester.infra.persistence.mysql.task;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValueAndRemove;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.getFilterInFirstValue;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.getFilterMatchFirstValueAndRemove;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.application.converter.TaskConverter;
import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.core.tester.domain.task.TaskListRepo;
import cloud.xcan.angus.core.tester.domain.task.count.TaskCount;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * @author XiaoLong Liu
 */
@Repository
public class TaskListRepoMysql extends AbstractSearchRepository<Task> implements TaskListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<Task> mainClz,
      Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), mainClz, criteria, "task", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, Class<Task> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    // DISTINCT for repeated comments
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis);

    // Assemble non mainClass assigneeIds in Conditions
    sql.append(assembleTagJoinCondition(criteria))
        .append(assembleFavouriteByJoinCondition(criteria))
        .append(assembleFollowByJoinCondition(criteria))
        .append(" WHERE 1=1 ")
        // Assemble mainClass Conditions
        .append(assembleCommentByInCondition(criteria))
        .append(getCriteriaAliasCondition(criteria, Task.class, mainAlis, mode, false, matches));
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return "a.*";
  }

  @Override
  public TaskCount count(Set<SearchCriteria> criteria) {
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
    if (isNotEmpty(criteria)) {
      // Assemble non mainClass tagIds in Conditions
      joinTag = assembleTagJoinCondition(criteria);

      matchValue = getFilterMatchFirstValueAndRemove(criteria, "name");
      matchValue = detectFulltextSearchValue(matchValue);

      // Assemble mainClass Conditions
      mainCondition = getCriteriaAliasCondition(criteria, Task.class,"a",
          getSearchMode(), false);
    }

    groupBySql.append(joinAssignee).append(joinConformor).append(joinTag)
        .append(" WHERE 1=1 ").append(mainCondition)
        .append(getMatchCondition(matchValue))
        .append(" GROUP BY a.`status`,a.test_type,a.task_type,a.exec_result "); // Use Covering Index
    overdueSql.append(joinAssignee).append(joinConformor).append(joinTag)
        .append(" WHERE 1=1 AND a.overdue = 1 ").append(mainCondition)
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

    Query groupByQueryResult = entityManager.createNativeQuery(groupBySql.toString());
    Query overdueQueryResult = entityManager.createNativeQuery(overdueSql.toString());
    Query oneTimePassQueryResult = entityManager.createNativeQuery(oneTimePassSql.toString());
    Query sumNumSqlQueryResult = entityManager.createNativeQuery(sumNumSql.toString());
    Query completedWorkloadQueryResult = entityManager.createNativeQuery(completedWorkloadSql.toString());

    if (isNotEmpty(criteria)) {
      setQueryParameter(groupByQueryResult, criteria, Task.class);
      setQueryParameter(overdueQueryResult, criteria, Task.class);
      setQueryParameter(oneTimePassQueryResult, criteria, Task.class);
      setQueryParameter(sumNumSqlQueryResult, criteria, Task.class);
      setQueryParameter(completedWorkloadQueryResult, criteria, Task.class);
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

  private StringBuilder assembleTagJoinCondition(Set<SearchCriteria> criteria) {
    StringBuilder sql = new StringBuilder();
    String tagIdInValue = getFilterInFirstValue(criteria, "tagId");
    String tagIdEqualValue = findFirstValue(criteria, "tagId", SearchOperation.EQUAL);
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

  private StringBuilder assembleFavouriteByJoinCondition(Set<SearchCriteria> criteria) {
    StringBuilder sql = new StringBuilder();
    String favouriteByEqualValue = findFirstValueAndRemove(criteria, "favouriteBy",
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

  private StringBuilder assembleFollowByJoinCondition(Set<SearchCriteria> criteria) {
    StringBuilder sql = new StringBuilder();
    String followByEqualValue = findFirstValueAndRemove(criteria, "followBy",
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

  public static StringBuilder assembleCommentByInCondition(Set<SearchCriteria> criteria) {
    StringBuilder sql = new StringBuilder();
    String followByEqualValue = findFirstValueAndRemove(criteria, "commentBy",
        SearchOperation.EQUAL);
    if (isEmpty(followByEqualValue)) {
      return sql;
    }
    sql.append(" AND a.id IN (SELECT t3.target_id FROM comment t3 WHERE t3.user_id = ")
        .append(followByEqualValue).append(")");
    return sql;
  }
}
