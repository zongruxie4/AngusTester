package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.func;

import static cloud.xcan.sdf.core.angustester.infra.persistence.mysql.task.TaskListRepoMysql.assembleCommentByInCondition;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValueAndRemove;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.getFilterInFirstValue;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static java.lang.Boolean.parseBoolean;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.api.search.SearchOperation;
import cloud.xcan.sdf.core.angustester.application.converter.FuncCaseConverter;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCase;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfoListRepo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.FuncCaseCount;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.sdf.core.jpa.page.FixedPageImpl;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import cloud.xcan.sdf.spec.utils.StringUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Query;
import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * @author xiaolong.liu
 */
@Repository
public class FuncCaseInfoListRepoMysql extends AbstractSearchRepository<FuncCaseInfo> implements
    FuncCaseInfoListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), step, criterias, "func_case", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    String enabledGroup = findFirstValueAndRemove(criterias, "enabledGroup");
    ProtocolAssert.assertTrue(isNull(enabledGroup) || !parseBoolean(enabledGroup),
        "Enabled group is not supported");
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT %s FROM ").append(tableName).append(" ").append(mainAlis)
        .append(assembleTagJoinCondition(criterias))
        .append(assembleFavouriteByJoinCondition(criterias))
        .append(assembleFollowByJoinCondition(criterias))
        .append(" WHERE 1=1 ")
        .append(assembleCommentByInCondition(criterias))
        .append(getCriteriaAliasCondition(step, criterias, mainAlis, mode, false, matches));
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params) {
    return "a.*";
  }

  @Override
  public Page<Long> groups(Set<SearchCriteria> criterias, Pageable pageable, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    String enabledGroup = findFirstValueAndRemove(criterias, "enabledGroup");
    ProtocolAssert.assertTrue(nonNull(enabledGroup) && parseBoolean(enabledGroup),
        "Enabled group is required");
    SingleTableEntityPersister step = getSingleTableEntityPersister();

    StringBuilder querySql = new StringBuilder()
        .append("SELECT DISTINCT a.module_id ")
        .append(" FROM func_case ").append(mainAlis)
        .append(assembleTagJoinCondition(criterias))
        .append(" WHERE 1=1 ")
        // Assemble mainClass conditions
        .append(getCriteriaAliasCondition(step, criterias, mainAlis,
            getSearchMode(), false, matches))
        .append(" ORDER BY a.module_id DESC ");

    Query queryResult = entityManager.createNativeQuery(querySql.toString());
    queryResult.setFirstResult((int) pageable.getOffset());
    queryResult.setMaxResults(pageable.getPageSize());

    if (isNotEmpty(criterias)) {
      setQueryParameter(step, queryResult, criterias, FuncCaseInfo.class);
    }
    List<Long> groupIds = FuncCaseConverter
        .objectArrToGroup((ArrayList<Object>) queryResult.getResultList());

    if (groupIds.size() < pageable.getPageSize()) {
      return new FixedPageImpl<>(groupIds, pageable, groupIds.size());
    }

    StringBuilder countSql = new StringBuilder()
        .append("SELECT count(DISTINCT a.module_id")
        .append(") FROM func_case ").append(mainAlis)
        .append(assembleTagJoinCondition(criterias))
        .append(assembleFavouriteByJoinCondition(criterias))
        .append(assembleFollowByJoinCondition(criterias))
        .append(" WHERE 1=1 ")
        // Assemble mainClass conditions
        .append(getCriteriaAliasCondition(step, criterias, mainAlis,
            getSearchMode(), false, matches));
    Query queryCount = this.getEntityManager().createNativeQuery(countSql.toString());
    if (isNotEmpty(criterias)) {
      this.setQueryParameter(step, queryCount, criterias, FuncCaseInfo.class);
    }
    long count = ((BigInteger) queryCount.getSingleResult()).longValue();
    return new FixedPageImpl<>(groupIds, pageable, count);
  }

  @Override
  public FuncCaseCount count(Set<SearchCriteria> criterias) {
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
    if (isNotEmpty(criterias)) {
      // Assemble non mainClass tagIds in Conditions
      joinTag = assembleTagJoinCondition(criterias);

      searchValue = CriteriaUtils.getFilterMatchFirstValueAndRemove(criterias, "name");
      searchValue = detectFulltextSearchValue(searchValue);

      // Assemble mainClass Conditions
      SingleTableEntityPersister step = getSingleTableEntityPersister();
      mainCondition = getCriteriaAliasCondition(step, criterias, "a",
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

    MetamodelImpl metaModel = (MetamodelImpl) getEntityManager().getMetamodel();
    Map<String, EntityPersister> entityPm = metaModel.entityPersisters();
    SingleTableEntityPersister step = (SingleTableEntityPersister) entityPm.get(FuncCaseInfo.class.getName());
    Query groupByQueryResult = entityManager.createNativeQuery(groupBySql.toString());
    Query overdueQueryResult = entityManager.createNativeQuery(overdueSql.toString());
    Query oneTimePassReviewQueryResult = entityManager.createNativeQuery(oneTimePassReviewSql.toString());
    Query alreadyTestedQueryResult = entityManager.createNativeQuery(alreadyTestedSql.toString());
    Query oneTimePassTestQueryResult = entityManager.createNativeQuery(oneTimePassTestSql.toString());
    Query sumNumQueryResult = entityManager.createNativeQuery(sumNumSql.toString());
    Query completedWorkloadQueryResult = entityManager.createNativeQuery(completedWorkloadSql.toString());

    if (isNotEmpty(criterias)) {
      setQueryParameter(step, groupByQueryResult, criterias, FuncCaseInfo.class);
      setQueryParameter(step, overdueQueryResult, criterias, FuncCaseInfo.class);
      setQueryParameter(step, oneTimePassReviewQueryResult, criterias, FuncCaseInfo.class);
      setQueryParameter(step, alreadyTestedQueryResult, criterias, FuncCaseInfo.class);
      setQueryParameter(step, oneTimePassTestQueryResult, criterias, FuncCaseInfo.class);
      setQueryParameter(step, sumNumQueryResult, criterias, FuncCaseInfo.class);
      setQueryParameter(step, completedWorkloadQueryResult, criterias, FuncCaseInfo.class);
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

  private SingleTableEntityPersister getSingleTableEntityPersister() {
    MetamodelImpl metaModel = (MetamodelImpl) this.getEntityManager().getMetamodel();
    Map<String, EntityPersister> entityPm = metaModel.entityPersisters();
    return (SingleTableEntityPersister) entityPm.get(FuncCase.class.getName());
  }

  private StringBuilder assembleTagJoinCondition(Set<SearchCriteria> criterias) {
    StringBuilder sql = new StringBuilder();
    String tagIdInValue = getFilterInFirstValue(criterias, "tagId");
    String tagIdEqualValue = findFirstValue(criterias, "tagId", SearchOperation.EQUAL);
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

  private StringBuilder assembleFavouriteByJoinCondition(Set<SearchCriteria> criterias) {
    StringBuilder sql = new StringBuilder();
    String favouriteByEqualValue = findFirstValueAndRemove(criterias, "favouriteBy",
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

  private StringBuilder assembleFollowByJoinCondition(Set<SearchCriteria> criterias) {
    StringBuilder sql = new StringBuilder();
    String followByEqualValue = findFirstValueAndRemove(criterias, "followBy",
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
