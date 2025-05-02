package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.exec;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.getFilterInFirstValue;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfoListRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.utils.StringUtils;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class ExecInfoListRepoMySql extends AbstractSearchRepository<ExecInfo> implements
    ExecInfoListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<ExecInfo> mainClass,
      Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), mainClass, criteria, "exec", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, Class<ExecInfo> mainClass,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis);

    // Assemble non mainClass target in Conditions
    sql.append(assembleNodeJoinCondition(criteria))
        .append(" WHERE 1=1 ")
        // Assemble mainClass Conditions
        .append(getCriteriaAliasCondition(criteria, mainClass, mainAlis, mode, false, matches));
    return sql;
  }

  private StringBuilder assembleNodeJoinCondition(Set<SearchCriteria> criteria) {
    StringBuilder sql = new StringBuilder();
    String nodeIdInValue = getFilterInFirstValue(criteria, "nodeId");
    String nodeIdEqualValue = findFirstValue(criteria, "nodeId", SearchOperation.EQUAL);
    if (isEmpty(nodeIdInValue) && isEmpty(nodeIdEqualValue)) {
      return sql;
    }
    sql.append(" INNER join exec_node t2 on a.id = t2.exec_id");
    if (StringUtils.isNotBlank(nodeIdInValue)) {
      sql.append(" AND t2.node_id in (").append(nodeIdInValue).append(")");
    }
    if (StringUtils.isNotBlank(nodeIdEqualValue)) {
      sql.append(" AND t2.node_id = ").append(nodeIdEqualValue);
    }
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return "a.*";
  }
}
