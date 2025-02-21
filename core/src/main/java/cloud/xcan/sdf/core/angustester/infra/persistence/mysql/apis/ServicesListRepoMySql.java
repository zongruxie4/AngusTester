package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.apis;

import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.assembleAuthJoinTargetCondition;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.ServicesListRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

@Repository
public class ServicesListRepoMySql extends AbstractSearchRepository<Services>
    implements ServicesListRepo {

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, Object[] params, String... matches) {
    return getSqlTemplate0(getSearchMode(), step, criteria, "services", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    String mainAlis = "a";
    // Assemble mainClass table
    StringBuilder sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis);

    // Assemble non mainClass assigneeIds in Conditions
    sql.append(" WHERE 1=1 ")
        .append(getCriteriaAliasCondition(step, criteria, mainAlis, mode, false, matches));

    // Assemble non mainClass authObjectId and grantFlag Conditions
    assembleAuthJoinTargetCondition(CombinedTargetType.SERVICE.getValue(), sql, criteria);
    return sql;
  }

}
