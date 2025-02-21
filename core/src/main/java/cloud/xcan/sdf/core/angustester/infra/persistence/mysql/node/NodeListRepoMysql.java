package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.node;

import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.getFilterInFirstValue;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.api.search.SearchOperation;
import cloud.xcan.sdf.core.angustester.domain.node.Node;
import cloud.xcan.sdf.core.angustester.domain.node.NodeListRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import cloud.xcan.sdf.spec.utils.StringUtils;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

@Repository
public class NodeListRepoMysql extends AbstractSearchRepository<Node> implements NodeListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), step, criteria, "node", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis);

    // Assemble non mainClass role in Conditions
    sql.append(assembleRoleJoinCondition(criteria))
        .append(" WHERE 1=1 ")
        // Assemble mainClass Conditions
        .append(getCriteriaAliasCondition(step, criteria, mainAlis, mode, false, matches));
    return sql;
  }

  private StringBuilder assembleRoleJoinCondition(Set<SearchCriteria> criteria) {
    StringBuilder sql = new StringBuilder();
    String roleInValue = getFilterInFirstValue(criteria, "role");
    String roleEqualValue = findFirstValue(criteria, "role", SearchOperation.EQUAL);
    if (isEmpty(roleInValue) && isEmpty(roleEqualValue)) {
      return sql;
    }
    sql.append(" INNER join node_role t1 on a.id = t1.node_id");
    if (StringUtils.isNotBlank(roleInValue)) {
      sql.append(" AND t1.role IN (").append(Arrays.stream(roleInValue.split(","))
          .map(s -> "'" + s + "'")
          .collect(Collectors.joining(", "))).append(")");
    }
    if (StringUtils.isNotBlank(roleEqualValue)) {
      sql.append(" AND t1.role = '").append(roleEqualValue).append("'");
    }
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return "a.*";
  }

}
