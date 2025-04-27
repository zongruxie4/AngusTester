package cloud.xcan.angus.core.tester.domain.scenario.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ScenarioAuthRepo extends BaseRepository<ScenarioAuth, Long> {

  List<ScenarioAuth> findAllByScenarioIdAndAuthObjectIdIn(Long scenarioId, List<Long> orgIds);

  List<ScenarioAuth> findAllByAuthObjectIdIn(Collection<Long> orgIds);

  List<ScenarioAuth> findAllByScenarioIdInAndAuthObjectIdIn(Collection<Long> scenarioIds,
      Collection<Long> orgIds);

  Long countByScenarioIdAndAuthObjectIdAndAuthObjectType(Long scenarioId, Long authObjectId,
      AuthObjectType authObjectType);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM scenario_auth WHERE scenario_id in ?1", nativeQuery = true)
  void deleteByScenarioIdIn(List<Long> scenarioIds);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM scenario_auth WHERE scenario_id = ?1 AND creator = ?2", nativeQuery = true)
  void deleteByScenarioIdAndCreator(Long scenarioId, Boolean creator);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM scenario_auth WHERE scenario_id = ?1 AND auth_object_id in ?2 AND creator = ?3", nativeQuery = true)
  void deleteByScenarioIdAndAuthObjectIdInAndCreator(Long scenarioId, Set<Long> creatorIds,
      boolean creator);
}
