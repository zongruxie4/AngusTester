package cloud.xcan.sdf.core.angustester.domain.scenario.auth;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
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
  @Query(value = "DELETE FROM scenario_auth WHERE scenario_id = ?1 AND creator_flag = ?2", nativeQuery = true)
  void deleteByScenarioIdAndCreatorFlag(Long scenarioId, Boolean creatorFlag);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM scenario_auth WHERE scenario_id = ?1 AND auth_object_id in ?2 AND creator_flag = ?3", nativeQuery = true)
  void deleteByScenarioIdAndAuthObjectIdInAndCreatorFlag(Long scenarioId, Set<Long> creatorIds,
      boolean creatorFlag);
}
