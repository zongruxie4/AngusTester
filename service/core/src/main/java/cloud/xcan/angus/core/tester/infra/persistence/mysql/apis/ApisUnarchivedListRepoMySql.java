package cloud.xcan.angus.core.tester.infra.persistence.mysql.apis;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedListRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * @author XiaoLong Liu
 */
@Repository
public class ApisUnarchivedListRepoMySql extends
    AbstractSearchRepository<ApisUnarchived> implements ApisUnarchivedListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<ApisUnarchived> mainClz,
      Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), mainClz, criteria, "apis_unarchived", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, Class<ApisUnarchived> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder(
        "SELECT %s FROM " + tableName + " " + mainAlis + " WHERE 1=1 ");

    // Assemble mainClass Conditions
    sql.append(getCriteriaAliasCondition(criteria, mainClz, mainAlis, mode, false, matches));
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return "a.id,a.protocol,a.summary,a.endpoint,a.method,a.created_by,a.created_date,a.last_modified_by,a.last_modified_date";
  }

}
