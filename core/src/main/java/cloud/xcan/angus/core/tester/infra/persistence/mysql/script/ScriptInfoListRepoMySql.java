package cloud.xcan.angus.core.tester.infra.persistence.mysql.script;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.getFilterInFirstValue;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.getFilterMatchFirstValueAndRemove;
import static cloud.xcan.angus.core.tester.application.converter.ScriptConverter.objectArrToScriptCount;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleAuthJoinTargetCondition;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfoListRepo;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptCount;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class ScriptInfoListRepoMySql extends AbstractSearchRepository<ScriptInfo> implements
    ScriptInfoListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<ScriptInfo> mainClz,
      Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), mainClz, criteria, "script", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, Class<ScriptInfo> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis)
        // Assemble non mainClass tag conditions
        .append(assembleTagJoinCondition(criteria))
        // Assemble non mainClass source target conditions
        .append(assembleSourceTargetJoinCondition(criteria))
        .append(" WHERE 1=1 ")
        // Assemble mainClass Conditions
        .append(getCriteriaAliasCondition(criteria, mainClz, mainAlis, mode, false, matches));
    // Assemble non mainClass authObjectId and grant Conditions
    assembleAuthJoinTargetCondition(CombinedTargetType.SCRIPT.getValue(), sql, criteria);
    return sql;
  }

  private StringBuilder assembleTagJoinCondition(Set<SearchCriteria> criteria) {
    StringBuilder sql = new StringBuilder();
    String tagInValue = getFilterInFirstValue(criteria, "tag");
    String tagEqualValue = findFirstValue(criteria, "tag", SearchOperation.EQUAL);
    if (isEmpty(tagInValue) && isEmpty(tagEqualValue)) {
      return sql;
    }
    sql.append(" INNER join script_tag t on a.id = t.script_id");
    if (isNotBlank(tagInValue)) {
      sql.append(" AND t.name in (").append(tagInValue).append(")");
    }
    if (isNotBlank(tagEqualValue)) {
      sql.append(" AND t.name = '").append(tagEqualValue).append("'");
    }
    return sql;
  }

  private StringBuilder assembleSourceTargetJoinCondition(Set<SearchCriteria> criteria) {
    StringBuilder sql = new StringBuilder();
    String sourceTargetIdInValue = getFilterInFirstValue(criteria, "sourceTargetId");
    String sourceTargetIdEqualValue = findFirstValue(criteria, "sourceTargetId",
        SearchOperation.EQUAL);
    if (isEmpty(sourceTargetIdInValue) && isEmpty(sourceTargetIdEqualValue)) {
      return sql;
    }
    sql.append(" INNER join script_target t on a.id = t.script_id");
    if (isNotBlank(sourceTargetIdInValue)) {
      sql.append(" AND t.target_id in (").append(sourceTargetIdInValue).append(")");
    }
    if (isNotBlank(sourceTargetIdEqualValue)) {
      sql.append(" AND t.target_id = ").append(sourceTargetIdEqualValue);
    }
    return sql;
  }

  @Override
  public ScriptCount count(Set<SearchCriteria> criteria) {
    // @formatter:off
    StringBuilder groupBySql = new StringBuilder("SELECT a.type,a.source,COUNT(*) num FROM script a ");

    StringBuilder joinTag = new StringBuilder();
    StringBuilder joinTarget = new StringBuilder();
    StringBuilder mainCondition = new StringBuilder();
    String matchValue = "";
    if (isNotEmpty(criteria)) {
      // Assemble non mainClass tag conditions
      joinTag = assembleTagJoinCondition(criteria);
      // Assemble non mainClass target conditions
      joinTarget = assembleSourceTargetJoinCondition(criteria);

      matchValue = getFilterMatchFirstValueAndRemove(criteria, "name");
      matchValue = detectFulltextSearchValue(matchValue);

      // Assemble mainClass Conditions
      mainCondition = getCriteriaAliasCondition(criteria, ScriptInfo.class, "a", getSearchMode(), false);
    }

    groupBySql.append(joinTag).append(joinTarget)
        .append(" WHERE 1=1 ").append(mainCondition)
        .append(getMatchCondition(matchValue));

    // Assemble non mainClass authObjectId and grant Conditions
    assembleAuthJoinTargetCondition(CombinedTargetType.SCRIPT.getValue(), groupBySql, criteria);

    groupBySql.append(" GROUP BY a.type,a.source "); // Use Covering Index

    Query groupByQueryResult = entityManager.createNativeQuery(groupBySql.toString());
    if (isNotEmpty(criteria)) {
      setQueryParameter(groupByQueryResult, criteria, ScriptInfo.class);
    }
    return objectArrToScriptCount((List<Object[]>) groupByQueryResult.getResultList()
    );
    // @formatter:on
  }

  private String getMatchCondition(String matchValue) {
    return isNotEmpty(matchValue)
        ? " AND MATCH (a.name, a.description, a.ext_search_merge, a.plugin) AGAINST ('"
        + matchValue + "' IN BOOLEAN MODE) " : "";
  }
}
