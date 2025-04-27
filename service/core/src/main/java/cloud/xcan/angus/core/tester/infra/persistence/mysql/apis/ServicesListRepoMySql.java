package cloud.xcan.angus.core.tester.infra.persistence.mysql.apis;

import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleAuthJoinTargetCondition;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.ServicesListRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class ServicesListRepoMySql extends AbstractSearchRepository<Services>
    implements ServicesListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<Services> mainClz,
      Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), mainClz, criteria, "services", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, Class<Services> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis);

    // Assemble non mainClass assigneeIds in Conditions
    sql.append(" WHERE 1=1 ")
        .append(getCriteriaAliasCondition(criteria, mainClz, mainAlis, mode, false, matches));

    // Assemble non mainClass authObjectId and grant Conditions
    assembleAuthJoinTargetCondition(CombinedTargetType.SERVICE.getValue(), sql, criteria);
    return sql;
  }

}
