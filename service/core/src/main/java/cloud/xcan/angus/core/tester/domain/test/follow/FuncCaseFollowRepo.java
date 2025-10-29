package cloud.xcan.angus.core.tester.domain.test.follow;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncCaseFollowRepo extends BaseRepository<FuncCaseFollow, Long> {

  List<FuncCaseFollow> findAllByCaseIdInAndCreatedBy(Collection<Long> caseIds, Long userId);

  Long countByCaseIdAndCreatedBy(Long caseId, Long userId);

  @Query(value =
      "select af.id id, a.id caseId,a.name caseName, a.project_id projectId, a.plan_id planId, a.code FROM func_case_follow af, func_case a WHERE a.id = af.case_id "
          + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.plan_deleted = 0 AND a.deleted = 0 ",
      countQuery =
          "select count(af.id) FROM func_case_follow af, func_case a WHERE a.id = af.case_id "
              + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.plan_deleted = 0 AND a.deleted = 0 ",
      nativeQuery = true)
  Page<FuncCaseFollowP> search(Long projectId, Long userId, Pageable pageable);

  /**
   * Note: Overwritten in implementation.
   */
  Page<FuncCaseFollowP> searchByMatch(Long projectId, Long userId, String name, Pageable pageable);

  @Query(value =
      "select count(af.id) FROM func_case_follow af, func_case a WHERE a.id = af.case_id "
          + "AND af.created_by = ?1 AND a.plan_deleted = 0 AND a.deleted = 0", nativeQuery = true)
  Long countByCreatedBy(Long userId);

  @Query(value =
      "select count(af.id) FROM func_case_follow af, func_case a WHERE a.id = af.case_id "
          + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.plan_deleted = 0 AND a.deleted = 0", nativeQuery = true)
  Long countByProjectIdAndCreatedBy(Long projectId, Long userId);

  @Query(value = "select created_by FROM func_case_follow WHERE case_id = ?1", nativeQuery = true)
  List<Long> findUserIdsByCaseId(Long id);

  @Modifying
  @Query(value = "DELETE FROM func_case_follow WHERE case_id = ?1 AND created_by = ?2", nativeQuery = true)
  int deleteByCaseIdAndCreatedBy(Long caseId, Long createdBy);

  @Modifying
  @Query(value = "DELETE FROM func_case_follow WHERE project_id = ?1 AND created_by = ?2", nativeQuery = true)
  void deleteByProjectIdAndCreatedBy(Long projectId, Long userId);

  @Modifying
  @Query(value = "DELETE FROM func_case_follow WHERE created_by = ?1", nativeQuery = true)
  int deleteByCreatedBy(Long createdBy);

  @Modifying
  @Query(value = "DELETE FROM func_case_follow WHERE case_id in ?1", nativeQuery = true)
  void deleteByCaseIdIn(Collection<Long> caseIds);

}
