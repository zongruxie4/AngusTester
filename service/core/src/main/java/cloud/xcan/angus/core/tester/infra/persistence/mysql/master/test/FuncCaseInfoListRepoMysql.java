package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.test;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValueAndRemove;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.getFilterInFirstValue;
import static cloud.xcan.angus.core.tester.infra.persistence.mysql.master.issue.TaskListRepoMysql.assembleCommentByInCondition;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.lang.Boolean.parseBoolean;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.page.FixedPageImpl;
import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoListRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncCaseCount;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.utils.StringUtils;
import jakarta.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * @author XiaoLong Liu
 */
@Repository
public class FuncCaseInfoListRepoMysql extends AbstractSearchRepository<FuncCaseInfo> implements
    FuncCaseInfoListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<FuncCaseInfo> mainClz,
      Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), mainClz, criteria, "func_case", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, Class<FuncCaseInfo> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    String enabledGroup = findFirstValueAndRemove(criteria, "enabledGroup");
    ProtocolAssert.assertTrue(isNull(enabledGroup) || !parseBoolean(enabledGroup),
        "Enabled group is not supported");
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT %s FROM ").append(tableName).append(" ").append(mainAlis)
        .append(assembleTagJoinCondition(criteria))
        .append(assembleFavouriteByJoinCondition(criteria))
        .append(assembleFollowByJoinCondition(criteria))
        .append(" WHERE 1=1 ")
        .append(assembleCommentByInCondition(criteria))
        .append(getCriteriaAliasCondition(criteria, mainClz, mainAlis, mode, false, matches));
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return "a.*";
  }

  @Override
  public Page<Long> groups(Set<SearchCriteria> criteria, Pageable pageable, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    String enabledGroup = findFirstValueAndRemove(criteria, "enabledGroup");
    ProtocolAssert.assertTrue(nonNull(enabledGroup) && parseBoolean(enabledGroup),
        "Enabled group is required");

    StringBuilder querySql = new StringBuilder()
        .append("SELECT DISTINCT a.module_id ")
        .append(" FROM func_case ").append(mainAlis)
        .append(assembleTagJoinCondition(criteria))
        .append(" WHERE 1=1 ")
        // Assemble mainClass conditions
        .append(getCriteriaAliasCondition(criteria, FuncCaseInfo.class, mainAlis,
            getSearchMode(), false, matches))
        .append(" ORDER BY a.module_id DESC ");

    Query queryResult = entityManager.createNativeQuery(querySql.toString());
    queryResult.setFirstResult((int) pageable.getOffset());
    queryResult.setMaxResults(pageable.getPageSize());

    if (isNotEmpty(criteria)) {
      setQueryParameter(queryResult, criteria, FuncCaseInfo.class);
    }
    List<Long> groupIds = FuncCaseConverter
        .objectArrToGroup((ArrayList<Object>) queryResult.getResultList());

    if (groupIds.size() < pageable.getPageSize()) {
      return new FixedPageImpl<>(groupIds, pageable, groupIds.size());
    }

    StringBuilder countSql = new StringBuilder()
        .append("SELECT count(DISTINCT a.module_id")
        .append(") FROM func_case ").append(mainAlis)
        .append(assembleTagJoinCondition(criteria))
        .append(assembleFavouriteByJoinCondition(criteria))
        .append(assembleFollowByJoinCondition(criteria))
        .append(" WHERE 1=1 ")
        // Assemble mainClass conditions
        .append(getCriteriaAliasCondition(criteria, FuncCaseInfo.class, mainAlis,
            getSearchMode(), false, matches));
    Query queryCount = this.getEntityManager().createNativeQuery(countSql.toString());
    if (isNotEmpty(criteria)) {
      this.setQueryParameter(queryCount, criteria, FuncCaseInfo.class);
    }
    Object count0 = queryCount.getSingleResult();
    long count = count0 instanceof BigInteger ? ((BigInteger) count0).longValue() : (Long) count0;
    return new FixedPageImpl<>(groupIds, pageable, count);
  }

  @Override
  public FuncCaseCount count(Set<SearchCriteria> criteria) {
    // @formatter:off
    StringBuilder groupBySql = new StringBuilder("SELECT a.`review_status`,a.test_result, COUNT(*) num FROM func_case a ");
    StringBuilder overdueSql = new StringBuilder("SELECT COUNT(a.id) FROM func_case a ");
    StringBuilder oneTimePassReviewSql = new StringBuilder("SELECT COUNT(a.id) FROM func_case a ");
    StringBuilder alreadyTestedSql = new StringBuilder("SELECT COUNT(a.id) FROM func_case a ");
    StringBuilder oneTimePassTestSql = new StringBuilder("SELECT COUNT(a.id) FROM func_case a ");
    //StringBuilder planSql = new StringBuilder("SELECT COUNT(b.id) FROM func_plan b LEFT JOIN func_case a ON b.id = a.plan_id ");
    StringBuilder sumNumSql = new StringBuilder("SELECT SUM(a.test_num), SUM(a.test_fail_num), SUM(a.review_num), SUM(a.eval_workload), SUM(a.actual_workload) FROM func_case a ");
    StringBuilder completedWorkloadSql = new StringBuilder("SELECT SUM(a.actual_workload) FROM func_case a ");

    StringBuilder joinTag = new StringBuilder();
    StringBuilder mainCondition = new StringBuilder();
    String searchValue = "";
    if (isNotEmpty(criteria)) {
      // Assemble non mainClass tagIds in Conditions
      joinTag = assembleTagJoinCondition(criteria);

      searchValue = CriteriaUtils.getFilterMatchFirstValueAndRemove(criteria, "name");
      searchValue = detectFulltextSearchValue(searchValue);

      // Assemble mainClass Conditions
      mainCondition = getCriteriaAliasCondition(criteria, FuncCaseInfo.class, "a",
          getSearchMode(), false);
    }

    groupBySql.append(joinTag)
        .append(" WHERE 1=1 ").append(mainCondition)
        .append(getMatchCondition(searchValue))
        .append(" GROUP BY a.`review_status`, a.test_result "); // Use Covering Index
    overdueSql.append(joinTag)
        .append(" WHERE 1=1 ").append(mainCondition)
        .append(" AND a.test_result <> 'CANCELED' ")
        .append(getMatchCondition(searchValue));
    oneTimePassReviewSql.append(joinTag)
        .append(" WHERE a.review_status = 'PASSED' AND a.review_fail_num = 0 ").append(mainCondition)
        .append(getMatchCondition(searchValue));
    alreadyTestedSql.append(joinTag)
        .append(" WHERE a.test_num > 0 AND a.test_result <> 'CANCELED' ").append(mainCondition)
        .append(getMatchCondition(searchValue));
    oneTimePassTestSql.append(joinTag)
        //.append(" WHERE a.test_result = '").append(CaseTestResult.PASSED.getValue()).append("' AND a.test_num = 1 ") -> Fix:: Allow multiple successful tests to be repeated.
        .append(" WHERE a.test_result = 'PASSED' AND a.test_fail_num = 0 ").append(mainCondition)
        .append(getMatchCondition(searchValue));
    sumNumSql.append(joinTag)
        .append(" WHERE 1=1 ").append(mainCondition)
        .append(getMatchCondition(searchValue));
    completedWorkloadSql.append(joinTag)
        .append(" WHERE a.test_result = 'PASSED' ").append(mainCondition)
        .append(getMatchCondition(searchValue));

    Query groupByQueryResult = entityManager.createNativeQuery(groupBySql.toString());
    Query overdueQueryResult = entityManager.createNativeQuery(overdueSql.toString());
    Query oneTimePassReviewQueryResult = entityManager.createNativeQuery(oneTimePassReviewSql.toString());
    Query alreadyTestedQueryResult = entityManager.createNativeQuery(alreadyTestedSql.toString());
    Query oneTimePassTestQueryResult = entityManager.createNativeQuery(oneTimePassTestSql.toString());
    Query sumNumQueryResult = entityManager.createNativeQuery(sumNumSql.toString());
    Query completedWorkloadQueryResult = entityManager.createNativeQuery(completedWorkloadSql.toString());

    if (isNotEmpty(criteria)) {
      setQueryParameter(groupByQueryResult, criteria, FuncCaseInfo.class);
      setQueryParameter(overdueQueryResult, criteria, FuncCaseInfo.class);
      setQueryParameter(oneTimePassReviewQueryResult, criteria, FuncCaseInfo.class);
      setQueryParameter(alreadyTestedQueryResult, criteria, FuncCaseInfo.class);
      setQueryParameter(oneTimePassTestQueryResult, criteria, FuncCaseInfo.class);
      setQueryParameter(sumNumQueryResult, criteria, FuncCaseInfo.class);
      setQueryParameter(completedWorkloadQueryResult, criteria, FuncCaseInfo.class);
    }
    return FuncCaseConverter.objectArrToCount(
        (List<Object[]>) groupByQueryResult.getResultList(),
        (List<Object[]>) overdueQueryResult.getResultList(),
        (List<Object[]>) oneTimePassReviewQueryResult.getResultList(),
        (List<Object[]>) alreadyTestedQueryResult.getResultList(),
        (List<Object[]>) oneTimePassTestQueryResult.getResultList(),
        (List<Object[]>) sumNumQueryResult.getResultList(),
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
    sql.append(" INNER JOIN tag_target t3 on a.id = t3.target_id");
    if (isNotBlank(tagIdInValue)) {
      sql.append(" AND t3.tag_id in (").append(tagIdInValue).append(")");
    }
    if (isNotBlank(tagIdEqualValue)) {
      sql.append(" AND t3.tag_id = ").append(tagIdEqualValue);
    }
    return sql;
  }

  private String getMatchCondition(String matchValue) {
    return isNotEmpty(matchValue) ? " AND MATCH (a.name, a.description, a.code) AGAINST ('"
        + matchValue + "' IN BOOLEAN MODE) " : "";
  }

  private StringBuilder assembleFavouriteByJoinCondition(Set<SearchCriteria> criteria) {
    StringBuilder sql = new StringBuilder();
    String favouriteByEqualValue = findFirstValueAndRemove(criteria, "favouriteBy",
        SearchOperation.EQUAL);
    if (isEmpty(favouriteByEqualValue)) {
      return sql;
    }
    sql.append(" INNER join func_case_favourite t3 on a.id = t3.case_id");
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
    sql.append(" INNER join func_case_follow t3 on a.id = t3.case_id");
    if (StringUtils.isNotBlank(followByEqualValue)) {
      sql.append(" AND t3.created_by = ").append(followByEqualValue);
    }
    return sql;
  }
}
