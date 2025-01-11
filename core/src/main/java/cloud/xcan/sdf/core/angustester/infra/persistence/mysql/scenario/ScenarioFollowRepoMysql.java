package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.follow.ScenarioFollow;
import cloud.xcan.sdf.core.angustester.domain.scenario.follow.ScenarioFollowRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioFollowRepoMysql extends ScenarioFollowRepo {

  @Query(value =
      "select sf.* FROM scenario_follow sf, scenario s WHERE s.id = sf.scenario_id "
          + "AND sf.project_id = ?1 AND sf.created_by = ?2 "
          + "AND MATCH(a.name,a.description,a.ext_search_merge) AGAINST (?3 IN BOOLEAN MODE) "
          + "AND s.deleted_flag = 0 ",
      countQuery =
          "select count(sf.id) FROM scenario_favourite sf, scenario s WHERE s.id = sf.scenario_id "
              + "AND sf.project_id = ?1 AND sf.created_by = ?2 "
              + "AND MATCH(a.name,a.description,a.ext_search_merge) AGAINST (?3 IN BOOLEAN MODE) "
              + "AND s.deleted_flag = 0 ",
      nativeQuery = true)
  @Override
  Page<ScenarioFollow> searchByMatch(Long projectId, Long userId, String scenarioName,
      Pageable pageable);

}
