package cloud.xcan.sdf.core.angustester.domain.func.plan;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
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

  @Query(value = "SELECT DISTINCT id FROM func_plan WHERE id IN (?1) AND auth_flag = ?2 ", nativeQuery = true)
  Collection<Long> findIds0ByIdInAndAuthFlag(Collection<Long> planIds, Boolean authFlag);

  boolean existsByProjectIdAndName(Long projectId, String name);

  long countByProjectIdAndName(Long projectId, String name);

  int countByProjectId(Long projectId);

  @Modifying
  @Query("UPDATE FuncPlan a SET a.deletedFlag = 0, a.deletedBy = null, a.deletedDate = null WHERE a.id in ?1")
  void updateToUndeletedStatusByIdIn(List<Long> allPlanIds);

  @Modifying
  @Query("UPDATE FuncPlan s SET s.authFlag=?2 WHERE s.id=?1")
  void updateAuthFlagById(Long id, Boolean enabledFlag);

  @Deprecated
  @Modifying
  @Query(value = "UPDATE func_plan s SET s.project_id =?2 WHERE s.id = ?1", nativeQuery = true)
  void updateDirById(Long id, Long projectId);

  @Modifying
  @Query(value = "DELETE FROM func_plan WHERE id in (?1)", nativeQuery = true)
  void deleteAllByIdIn(List<Long> ids);


}
