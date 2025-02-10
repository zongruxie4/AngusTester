package cloud.xcan.sdf.core.angustester.domain.scenario;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScenarioRepo extends BaseRepository<Scenario, Long>,
    NameJoinRepository<Scenario, Long> {

  @Query(value = "SELECT s.* FROM scenario s WHERE s.id = ?1", nativeQuery = true)
  Optional<Scenario> find0ById(Long id);

  @Query(value = "SELECT s.* FROM scenario s WHERE s.id IN ?1", nativeQuery = true)
  List<Scenario> findAll0ByIdIn(Collection<Long> ids);

  @Query(value = "SELECT DISTINCT s.project_id FROM scenario s WHERE s.id IN ?1", nativeQuery = true)
  List<Long> findAll0ProjectIdByIdIn(Collection<Long> ids);

  @Query(value = "SELECT count(*) from scenario", nativeQuery = true)
  long countAll0();

  @Query(value = "SELECT count(*) from scenario s WHERE s.name = ?1 AND s.project_id = ?2", nativeQuery = true)
  Long countAll0ByNameAndProjectId(String name, Long projectId);

  @Query(value = "SELECT count(*) FROM scenario s WHERE s.name = ?1 AND s.project_id = ?2 AND s.id <> ?3", nativeQuery = true)
  Long countAll0ByNameAndProjectIdAndIdNot(String name, Long projectId, Long id);

  @Query(value = "SELECT id FROM scenario WHERE id IN ?1 AND auth_flag = ?2", nativeQuery = true)
  List<Long> findIds0ByIdInAndAuthFlag(Collection<Long> ids, boolean authFlag);

  // @Query(value = "SELECT count(*) FROM scenario WHERE name = ?1 limit 1", nativeQuery = true) <- Fix:: class java.math.BigInteger cannot be cast to class java.lang.Boolean
  boolean existsByProjectIdAndName(Long projectId, String name);

  @Query(value = "SELECT count(s.id) FROM Scenario s WHERE s.projectId = ?1 ")
  long countByProjectId(long projectId);

  @Modifying
  @Query("UPDATE Scenario s SET s.authFlag=?2 WHERE s.id=?1")
  void updateAuthFlagById(Long id, Boolean authFlag);

  @Modifying
  @Query("UPDATE Scenario s SET s.deletedFlag=?2, s.deletedBy =?3, s.deletedDate = ?4 WHERE s.id = ?1")
  void updateDeleteStatus(Long ids, Boolean deletedFlag, Long deletedBy, LocalDateTime deletedDate);

  @Modifying
  @Query("UPDATE Scenario s SET s.deletedFlag = 0, s.deletedBy = -1, s.deletedDate = null WHERE s.id in ?1")
  void updateToUndeletedStatusByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "DELETE FROM scenario WHERE id IN ?1", nativeQuery = true)
  void deleteAllByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "DELETE FROM scenario WHERE script_id IN ?1", nativeQuery = true)
  void deleteByScriptIdIn(Collection<Long> ids);

}
