package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.script;

import static cloud.xcan.sdf.core.angustester.application.converter.ScriptConverter.objectArrToScriptCount;
import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.assembleAuthJoinTargetCondition;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.getFilterInFirstValue;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.getFilterMatchFirstValueAndRemove;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.api.search.SearchOperation;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfo;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfoListRepo;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptCount;
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

@Repository
public class ScriptInfoListRepoMySql extends AbstractSearchRepository<ScriptInfo> implements
    ScriptInfoListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), step, criterias, "script", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis)
        // Assemble non mainClass tag conditions
        .append(assembleTagJoinCondition(criterias))
        // Assemble non mainClass source target conditions
        .append(assembleSourceTargetJoinCondition(criterias))
        .append(" WHERE 1=1 ")
        // Assemble mainClass Conditions
        .append(getCriteriaAliasCondition(step, criterias, mainAlis, mode, false, matches));
    // Assemble non mainClass authObjectId and grantFlag Conditions
    assembleAuthJoinTargetCondition(CombinedTargetType.SCRIPT.getValue(), sql, criterias);
    return sql;
  }

  private StringBuilder assembleTagJoinCondition(Set<SearchCriteria> criterias) {
    StringBuilder sql = new StringBuilder();
    String tagInValue = getFilterInFirstValue(criterias, "tag");
    String tagEqualValue = findFirstValue(criterias, "tag", SearchOperation.EQUAL);
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

  private StringBuilder assembleSourceTargetJoinCondition(Set<SearchCriteria> criterias) {
    StringBuilder sql = new StringBuilder();
    String sourceTargetIdInValue = getFilterInFirstValue(criterias, "sourceTargetId");
    String sourceTargetIdEqualValue = findFirstValue(criterias, "sourceTargetId",
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
  public ScriptCount count(Set<SearchCriteria> criterias) {
    // @formatter:off
    StringBuilder groupBySql = new StringBuilder("SELECT a.type,a.source,COUNT(*) num FROM script a ");

    StringBuilder joinTag = new StringBuilder();
    StringBuilder joinTarget = new StringBuilder();
    StringBuilder mainCondition = new StringBuilder();
    String matchValue = "";
    if (isNotEmpty(criterias)) {
      // Assemble non mainClass tag conditions
      joinTag = assembleTagJoinCondition(criterias);
      // Assemble non mainClass target conditions
      joinTarget = assembleSourceTargetJoinCondition(criterias);

      matchValue = getFilterMatchFirstValueAndRemove(criterias, "name");
      matchValue = detectFulltextSearchValue(matchValue);

      // Assemble mainClass Conditions
      SingleTableEntityPersister step = getSingleTableEntityPersister();
      mainCondition = getCriteriaAliasCondition(step, criterias, "a", getSearchMode(), false);
    }

    groupBySql.append(joinTag).append(joinTarget)
        .append(" WHERE 1=1 ").append(mainCondition)
        .append(getMatchCondition(matchValue));

    // Assemble non mainClass authObjectId and grantFlag Conditions
    assembleAuthJoinTargetCondition(CombinedTargetType.SCRIPT.getValue(), groupBySql, criterias);

    groupBySql.append(" GROUP BY a.type,a.source "); // Use Covering Index

    MetamodelImpl metaModel = (MetamodelImpl) getEntityManager().getMetamodel();
    Map<String, EntityPersister> entityPm = metaModel.entityPersisters();
    SingleTableEntityPersister step = (SingleTableEntityPersister) entityPm
        .get(ScriptInfo.class.getName());
    Query groupByQueryResult = entityManager.createNativeQuery(groupBySql.toString());

    if (isNotEmpty(criterias)) {
      setQueryParameter(step, groupByQueryResult, criterias, ScriptInfo.class);
    }
    return objectArrToScriptCount((List<Object[]>) groupByQueryResult.getResultList()
    );
    // @formatter:on
  }

  private SingleTableEntityPersister getSingleTableEntityPersister() {
    MetamodelImpl metaModel = (MetamodelImpl) this.getEntityManager().getMetamodel();
    Map<String, EntityPersister> entityPm = metaModel.entityPersisters();
    return (SingleTableEntityPersister) entityPm.get(ScriptInfo.class.getName());
  }

  private String getMatchCondition(String matchValue) {
    return isNotEmpty(matchValue)
        ? " AND MATCH (a.name, a.description, a.ext_search_merge, a.plugin) AGAINST ('"
        + matchValue + "' IN BOOLEAN MODE) " : "";
  }
}
