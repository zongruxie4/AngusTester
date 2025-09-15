package cloud.xcan.angus.core.tester.domain.scenario;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
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

  @Query(value = "SELECT count(*) from scenario", nativeQuery = true)
  long countAll0();

  @Query(value = "SELECT count(*) from scenario s WHERE s.name = ?1 AND s.project_id = ?2", nativeQuery = true)
  Long countAll0ByNameAndProjectId(String name, Long projectId);

  @Query(value = "SELECT count(*) FROM scenario s WHERE s.name = ?1 AND s.project_id = ?2 AND s.id <> ?3", nativeQuery = true)
  Long countAll0ByNameAndProjectIdAndIdNot(String name, Long projectId, Long id);

  @Query(value = "SELECT id FROM scenario WHERE id IN ?1 AND auth = ?2", nativeQuery = true)
  List<Long> findIds0ByIdInAndAuth(Collection<Long> ids, boolean auth);

  // @Query(value = "SELECT count(*) FROM scenario WHERE name = ?1 limit 1", nativeQuery = true) <- Fix:: class java.math.BigInteger cannot be cast to class java.lang.Boolean
  boolean existsByProjectIdAndName(Long projectId, String name);

  @Query(value = "SELECT count(s.id) FROM Scenario s WHERE s.projectId = ?1 ")
  long countByProjectId(long projectId);

  @Query(value = "SELECT * FROM scenario WHERE project_id = ?1 AND plugin = ?2 AND script_type IN ?3 ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
  Scenario findLeastByProjectIdAndPluginAndTypeIn(Long projectId, String plugin,
      List<String> scriptTypes);

  @Modifying
  @Query(value = "UPDATE scenario s SET s.auth=?2 WHERE s.id=?1", nativeQuery = true)
  void updateAuthById(Long id, Boolean auth);

  @Modifying
  @Query(value = "UPDATE scenario s SET s.deleted = ?2, s.deleted_by= ?3, s.deleted_date = ?4 WHERE s.id = ?1", nativeQuery = true)
  void updateDeleteStatus(Long ids, Boolean deleted, Long deletedBy, LocalDateTime deletedDate);

  @Modifying
  @Query(value = "UPDATE scenario s SET s.deleted = false, s.deleted_by = -1, s.deleted_date = null WHERE s.id in ?1", nativeQuery = true)
  void updateToUndeletedStatusByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "DELETE FROM scenario WHERE id IN ?1", nativeQuery = true)
  void deleteAllByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "DELETE FROM scenario WHERE script_id IN ?1", nativeQuery = true)
  void deleteByScriptIdIn(Collection<Long> ids);

}
