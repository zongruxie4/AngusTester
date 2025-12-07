package cloud.xcan.angus.core.tester.domain.test.cases;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.core.jpa.entity.projection.IdAndName;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.domain.test.cases.count.PlanCaseNum;
import cloud.xcan.angus.core.tester.domain.test.plan.PlanMember;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncCaseRepo extends BaseRepository<FuncCase, Long> {

  @Query(value = "SELECT id FROM func_case WHERE plan_id in ?1", nativeQuery = true)
  List<Long> findAll0IdByPlanIdIn(Collection<Long> planIds);

  @Query(value = "SELECT id FROM func_case WHERE plan_id = ?1 AND id IN ?2", nativeQuery = true)
  Set<Long> findAll0IdByPlanIdAndIdIn(Long planId, Collection<Long> ids);

  @Query(value = "SELECT DISTINCT planId as planId, testerId as testerId FROM FuncCase WHERE planId in ?1")
  List<PlanMember> findMemberByPlanIdIn(Collection<Long> planIds);

  @Query(value = "SELECT count(ud.id) as caseNum, ud.planId as planId FROM FuncCase ud WHERE ud.planId IN (?1) GROUP BY ud.planId")
  List<PlanCaseNum> findPlanCaseNumsGroupByPlanId(Collection<Long> planIds);

  @Query(value = "SELECT count(ud.id) as caseNum, ud.planId as planId FROM FuncCase ud WHERE ud.planId IN (?1) AND ud.testResult <> 'CANCELED' GROUP BY ud.planId")
  List<PlanCaseNum> findValidPlanCaseNumsGroupByPlanId(Collection<Long> planIds);

  @Query(value = "SELECT count(ud.id) as caseNum, ud.planId as planId FROM FuncCase ud WHERE ud.planId IN (?1) AND ud.testResult = 'PASSED' GROUP BY ud.planId")
  List<PlanCaseNum> findPlanPassedCaseNumsGroupByPlanId(Collection<Long> planIds);

  @Query(value = "SELECT name FROM func_case WHERE name in ?1 AND plan_id = ?2", nativeQuery = true)
  List<String> findNamesByNameInAndPlanId(Collection<String> caseNames, long planId);

  @Query("SELECT t.id as id, t.name as name FROM FuncCase t WHERE t.id IN ?1")
  List<IdAndName> findNameMapByIdIn(Collection<Long> ids);

  @Query(value = "SELECT * FROM func_case t WHERE t.id IN ?1", nativeQuery = true)
  List<FuncCase> findAllByIdIn(Collection<Long> caseIds);

  List<FuncCase> findAllByProjectId(Long projectId);

  boolean existsByPlanIdAndName(Long planId, String name);

  long countByPlanId(Long planId);

  long countByPlanIdAndNameAndIdNot(Long planId, String name, Long id);

  @Query(value =
      "SELECT count(id) FROM func_case WHERE plan_id = ?1 AND test_result NOT IN ('PASSED','CANCELED')"
          + " AND deleted = 0 AND plan_deleted =0", nativeQuery = true)
  long countNotPassedByPlanId(Long id);

  @Modifying
  @Query(value = "UPDATE func_case s SET s.deleted = false, s.deleted_by = -1, s.deleted_date = null WHERE s.id in ?1", nativeQuery = true)
  void updateToUndeletedStatusByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "UPDATE func_case s SET s.deleted=?2, s.deleted_by =?3, s.deleted_date = ?4 WHERE s.id in ?1", nativeQuery = true)
  void updateDeleteStatus(Collection<Long> ids, Boolean deleted, Long deletedBy,
      LocalDateTime deletedDate);

  @Modifying
  @Query("UPDATE FuncCase s SET s.planAuth=?2 WHERE s.planId=?1")
  void updatePlanAuthByPlanId(Long planId, Boolean enabled);

  @Modifying
  @Query("UPDATE FuncCase s SET s.projectId=?2 WHERE s.id IN ?1")
  void updateProjectByIdIn(Collection<Long> caseIds, Long projectId);

  @Modifying
  @Query("UPDATE FuncCase s SET s.planId=?2 WHERE s.id IN ?1")
  void updatePlanByIdIn(Collection<Long> caseIds, Long planId);

  @Modifying
  @Query("UPDATE FuncCase s SET s.projectId=?2 WHERE s.planId = ?1")
  void updateProjectByPlanId(Long planId, Long targetDirId);

  @Modifying
  @Query("UPDATE FuncCase s SET s.moduleId=-1 WHERE s.moduleId IN ?1")
  void updateModuleNull(Collection<Long> moduleIds);

  @Modifying
  @Query("UPDATE FuncCase s SET s.evalWorkloadMethod=?2 WHERE s.planId = ?1")
  void updateEvalWorkloadMethodByPlanId(Long planId, EvalWorkloadMethod evalWorkloadMethod);

  @Modifying
  @Query("UPDATE FuncCase s SET s.testNum=0, s.testFailNum=0, s.testResult='PENDING', s.testScore = null, s.testResultHandleDate=null WHERE s.id IN ?1")
  void updateTestResultToInitByIds(Collection<Long> ids);

  @Modifying
  @Query("UPDATE FuncCase s SET s.testNum=0, s.testFailNum=0, s.testResult='PENDING', s.testScore = null, s.testResultHandleDate=null WHERE s.planId IN ?1")
  void updateTestResultToInitByPlanIds(Collection<Long> planIds);

  @Modifying
  @Query("UPDATE FuncCase s SET s.reviewerId=null, s.reviewDate=null, s.reviewStatus='PENDING', s.reviewRemark=null, s.reviewNum=0, s.reviewFailNum=0 WHERE s.id IN ?1")
  void updateReviewResultToInitByIds(Collection<Long> ids);

  @Modifying
  @Query("UPDATE FuncCase s SET s.reviewerId=null, s.reviewDate=null, s.reviewStatus='PENDING', s.reviewRemark=null WHERE s.id IN ?1")
  void updateReviewResultToRestartByIds(Collection<Long> ids);

  @Modifying
  @Query("UPDATE FuncCase s SET s.reviewerId=null, s.reviewDate=null, s.reviewStatus='PENDING', s.reviewRemark=null, s.reviewNum=0, s.reviewFailNum=0 WHERE s.planId IN ?1")
  void updateReviewResultToInitByPlanIds(Collection<Long> planIds);

  @Modifying
  //@Query("UPDATE FuncCase s SET s.reviewStatus=null, s.reviewRemark=null, s.reviewNum=0 WHERE s.planId = ?1")
  @Query("UPDATE FuncCase s SET s.review=false, s.reviewStatus=null WHERE s.planId = ?1")
  void updateReviewResultToDisabledByPlanId(Long planId);

  @Modifying
  //@Query("UPDATE FuncCase s SET s.reviewStatus='PENDING', s.reviewRemark=null, s.reviewNum=0 WHERE s.planId = ?1")
  @Query("UPDATE FuncCase s SET s.review=true, s.reviewStatus='PENDING', s.reviewRemark=null WHERE s.planId = ?1")
  void updateReviewResultToStartedByPlanId(Long planId);

  @Modifying
  @Query("UPDATE FuncCase s SET s.planDeleted=?2 WHERE s.planId IN ?1")
  void updatePlanDeleteStatusByPlan(Collection<Long> planIds, Boolean deleted);

  @Transactional
  @Modifying
  @Query(value = "UPDATE func_case SET overdue=1 WHERE id IN ?1", nativeQuery = true)
  void updateOverdue(Collection<Long> ids);

  @Transactional
  @Modifying
  @Query(value = "UPDATE func_case SET unplanned=?2 WHERE id IN ?1", nativeQuery = true)
  void updateUnplannedByIdIn(Collection<Long> ids, Boolean unplanned);

  @Modifying
  @Query(value = "UPDATE func_case SET software_version = ?3 WHERE project_id = ?1 AND software_version = ?2", nativeQuery = true)
  void updateVersionByProjectIdAndSoftwareVersion(Long projectId, String fromVersion,
      String toVersion);

  @Transactional
  @Modifying
  @Query(value = "UPDATE func_case SET version = version + 1 WHERE id IN ?1", nativeQuery = true)
  void incrVersionByIdIn(Collection<Long> caseIds);

  @Modifying
  @Query(value = "DELETE FROM func_case WHERE id in ?1", nativeQuery = true)
  void deleteAllByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "DELETE FROM func_case WHERE plan_id = ?1 AND name IN ?2", nativeQuery = true)
  void deleteByPlanIdAndNameIn(Long planId, Collection<String> names);


}
