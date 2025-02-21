package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.activity;

import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findValueAndRemove;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityListRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityListRepoMysql extends AbstractSearchRepository<Activity>
    implements ActivityListRepo {

  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, Object[] objects, String... matches) {
    return getSqlTemplate0(getSearchMode(), step, criteria, "activity", matches);
  }

  @Override
  public StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, String tableName, String... matches) {
    StringBuilder sql;
    String targetTypeValue = findFirstValue(criteria, "targetType");
    String targetIdValue = findFirstValue(criteria, "targetId");
    if (isEmpty(targetTypeValue) || targetTypeValue.contains(",") /* IN filter*/
        || !CombinedTargetType.valueOf(targetTypeValue).isParent() || isEmpty(targetIdValue)) {
      String mainAlis = "a";

      // Assemble mainClass table
      sql = new StringBuilder("SELECT %s FROM " + tableName + " " + mainAlis + " WHERE 1=1 ");

      // Assemble mainClass Conditions
      sql.append(getCriteriaAliasCondition(step, criteria, mainAlis, mode, false,
          matches));
    } else {
      // Note: Only a single targetType value is supported.
      String mainAlis = "a";
      String subAlis = "b";
      findValueAndRemove(criteria, "targetType");
      findValueAndRemove(criteria, "targetId");

      // Query parent activities
      sql = new StringBuilder(
          "SELECT %s FROM " + tableName + " " + mainAlis + " WHERE " + mainAlis + ".id IN (");
      sql.append("SELECT id FROM " + tableName + " " + subAlis + " WHERE " + subAlis
          + ".target_id=" + targetIdValue + " AND " + subAlis + ".target_type='" + targetTypeValue
          + "' ").append(getCriteriaAliasCondition(step, criteria, subAlis, mode, false,
          matches));

      // Union sub activities

      sql.append(" UNION SELECT id FROM " + tableName + " " + subAlis + " WHERE " + subAlis
          + ".parent_target_id=" + targetIdValue);
      sql.append(getCriteriaAliasCondition(step, criteria, subAlis, mode, false, matches));

      sql.append(" )");
    }
    return sql;
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return "a.*";
  }

}
