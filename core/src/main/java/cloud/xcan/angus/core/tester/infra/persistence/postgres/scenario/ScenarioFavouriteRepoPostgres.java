package cloud.xcan.angus.core.tester.infra.persistence.postgres.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavourite;
import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavouriteRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioFavouriteRepoPostgres extends ScenarioFavouriteRepo {

  @Query(value =
      "select sf.* FROM scenario_favourite sf, scenario s WHERE s.id = sf.scenario_id "
          + "AND sf.project_id = ?1 AND sf.created_by = ?2 AND s.deleted = 0 "
          + "AND to_tsvector(s.name) @@ plainto_tsquery(?3)",
      countQuery =
          "select count(sf.id) FROM scenario_favourite sf, scenario s WHERE s.id = sf.scenario_id "
              + "AND sf.project_id = ?1 AND sf.created_by = ?2 AND s.deleted = 0 "
              + "AND to_tsvector(s.name) @@ plainto_tsquery(?3)",
      nativeQuery = true)
  @Override
  Page<ScenarioFavourite> searchByMatch(Long projectId, Long userId, String scenarioName,
      Pageable pageable);

}
