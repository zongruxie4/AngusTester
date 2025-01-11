package cloud.xcan.sdf.core.angustester.domain.scenario.favorite;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author xiaolong.liu
 */
@NoRepositoryBean
public interface ScenarioFavouriteRepo extends BaseRepository<ScenarioFavourite, Long> {

  List<ScenarioFavourite> findAllByScenarioIdInAndCreatedBy(Set<Long> scenarioIds, Long userId);

  Long countByScenarioIdAndCreatedBy(Long scenarioId, Long userId);

  @Query(value =
      "select sf.* FROM scenario_favourite sf, scenario s WHERE s.id = sf.scenario_id "
          + "AND sf.project_id = ?1 AND sf.created_by = ?2 AND s.deleted_flag = 0 ",
      countQuery =
          "select count(sf.id) FROM scenario_favourite sf, scenario s WHERE s.id = sf.scenario_id "
              + "AND sf.project_id = ?1 AND sf.created_by = ?2 AND s.deleted_flag = 0 ",
      nativeQuery = true)
  Page<ScenarioFavourite> search(Long projectId, Long userId, Pageable pageable);

  /**
   * Note: Overwritten in implementation.
   */
  Page<ScenarioFavourite> searchByMatch(Long projectId, Long userId, String scenarioName,
      Pageable pageable);

  @Query(value =
      "select count(af.id) FROM scenario_favourite af, scenario s WHERE s.id = af.scenario_id "
          + "AND af.created_by = ?1 AND s.deleted_flag = 0", nativeQuery = true)
  Long countByCreatedBy(Long userId);

  @Query(value =
      "select count(af.id) FROM scenario_favourite af, scenario s WHERE s.id = af.scenario_id "
          + "AND af.project_id = ?1 AND af.created_by = ?2 AND s.deleted_flag = 0", nativeQuery = true)
  Long countByProjectIdAndCreatedBy(Long projectId, Long userId);

  @Modifying
  @Query(value = "DELETE FROM scenario_favourite WHERE scenario_id = ?1 AND created_by = ?2", nativeQuery = true)
  int deleteByScenarioIdAndCreatedBy(Long scenarioId, Long createdBy);

  @Modifying
  @Query(value = "DELETE FROM scenario_favourite WHERE project_id = ?1 AND created_by = ?2", nativeQuery = true)
  void deleteByProjectIdAndCreatedBy(Long projectId, Long userId);

  @Modifying
  @Query(value = "DELETE FROM scenario_favourite WHERE created_by = ?1", nativeQuery = true)
  int deleteByCreatedBy(Long userId);

  @Modifying
  @Query(value = "DELETE FROM scenario_favourite WHERE scenario_id in ?1", nativeQuery = true)
  void deleteByScenarioIdIn(List<Long> scenarioIds);

}
