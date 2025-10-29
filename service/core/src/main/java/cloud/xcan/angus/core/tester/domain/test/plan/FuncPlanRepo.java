package cloud.xcan.angus.core.tester.domain.test.plan;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncPlanRepo extends BaseRepository<FuncPlan, Long>,
    NameJoinRepository<FuncPlan, Long> {

  @Query(value = "SELECT * FROM func_plan WHERE id = ?1", nativeQuery = true)
  Optional<FuncPlan> find0ById(Long id);

  List<FuncPlan> findByProjectId(Long projectId);

  @Query(value = "SELECT * FROM func_plan WHERE id IN (?1)", nativeQuery = true)
  List<FuncPlan> findAll0ByIdIn(List<Long> ids);

  @Query(value = "SELECT DISTINCT id FROM func_plan WHERE id IN (?1) AND auth = ?2 ", nativeQuery = true)
  Collection<Long> findIds0ByIdInAndAuth(Collection<Long> planIds, Boolean auth);

  @Query(value = "SELECT * FROM func_plan WHERE project_id = ?1 ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
  FuncPlan findLeastByProjectId(Long projectId);

  boolean existsByProjectIdAndName(Long projectId, String name);

  long countByProjectIdAndName(Long projectId, String name);

  int countByProjectId(Long projectId);

  @Modifying
  @Query(value = "UPDATE func_plan a SET a.deleted = false, a.deleted_by = null, a.deleted_date = null WHERE a.id in ?1", nativeQuery = true)
  void updateToUndeletedStatusByIdIn(Collection<Long> allPlanIds);

  @Modifying
  @Query(value = "UPDATE func_plan s SET s.auth=?2 WHERE s.id=?1", nativeQuery = true)
  void updateAuthById(Long id, Boolean enabled);

  @Deprecated
  @Modifying
  @Query(value = "UPDATE func_plan s SET s.project_id =?2 WHERE s.id = ?1", nativeQuery = true)
  void updateDirById(Long id, Long projectId);

  @Modifying
  @Query(value = "DELETE FROM func_plan WHERE id in (?1)", nativeQuery = true)
  void deleteAllByIdIn(List<Long> ids);

}
