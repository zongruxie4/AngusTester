package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavourite;
import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavouriteRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioFavouriteRepoMysql extends ScenarioFavouriteRepo {

  @Query(value =
      "select sf.* FROM scenario_favourite sf, scenario s WHERE s.id = sf.scenario_id "
          + "AND sf.project_id = ?1 AND sf.created_by = ?2 "
          + "AND MATCH(a.name,a.description,a.ext_search_merge) AGAINST (?3 IN BOOLEAN MODE) "
          + "AND s.deleted = 0 ",
      countQuery =
          "select count(sf.id) FROM scenario_favourite sf, scenario s WHERE s.id = sf.scenario_id "
              + "AND sf.project_id = ?1 AND sf.created_by = ?2 "
              + "AND MATCH(a.name,a.description,a.ext_search_merge) AGAINST (?3 IN BOOLEAN MODE) "
              + "AND s.deleted = 0 ",
      nativeQuery = true)
  @Override
  Page<ScenarioFavourite> searchByMatch(Long projectId, Long userId, String name,
      Pageable pageable);

}
