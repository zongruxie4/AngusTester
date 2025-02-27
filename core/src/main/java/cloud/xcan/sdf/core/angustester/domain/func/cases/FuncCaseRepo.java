package cloud.xcan.sdf.core.angustester.domain.func.cases;

import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.PlanCaseNum;
import cloud.xcan.sdf.core.angustester.domain.func.plan.PlanMember;
import cloud.xcan.sdf.core.jpa.entity.projection.IdAndName;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncCaseRepo extends BaseRepository<FuncCase, Long> {

  @Query(value = "SELECT id FROM func_case WHERE project_id in ?1", nativeQuery = true)
  List<Long> findAll0IdByProjectIdIn(Collection<Long> projectIds);

  @Query(value = "SELECT id FROM func_case WHERE plan_id in ?1", nativeQuery = true)
  List<Long> findAll0IdByPlanIdIn(Collection<Long> planIds);

  @Query(value = "SELECT id FROM func_case WHERE plan_id = ?1 AND id IN ?2", nativeQuery = true)
  Set<Long> findAll0IdByPlanIdAndIdIn(Long planId, Collection<Long> ids);

  @Query(value = "SELECT DISTINCT module FROM func_case WHERE project_id = ?1 AND deleted_flag = 0 AND plan_deleted_flag = 0", nativeQuery = true)
  List<String> findModuleByPlanId(Long projectId);

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

  boolean existsByPlanIdAndName(Long planId, String name);

  long countByPlanId(Long planId);

  long countByPlanIdAndNameAndIdNot(Long planId, String name, Long id);

  @Query(value = "SELECT count(id) FROM func_case WHERE plan_id = ?1 AND test_result NOT IN ('PASSED','CANCELED')"
      + " AND deleted_flag = 0 AND plan_deleted_flag =0", nativeQuery = true)
  long countNotPassedByPlanId(Long id);

  @Modifying
  @Query("UPDATE FuncCase s SET s.deletedFlag = 0, s.deletedBy = -1, s.deletedDate = null WHERE s.id in ?1")
  void updateToUndeletedStatusByIdIn(Collection<Long> ids);

  @Modifying
  @Query("UPDATE FuncCase s SET s.planAuthFlag=?2 WHERE s.planId=?1")
  void updatePlanAuthFlagByPlanId(Long planId, Boolean enabledFlag);

  @Modifying
  @Query("UPDATE FuncCase s SET s.deletedFlag=?2, s.deletedBy =?3, s.deletedDate = ?4 WHERE s.id in ?1")
  void updateDeleteStatus(Collection<Long> ids, Boolean deletedFlag, Long deletedBy,
      LocalDateTime deletedDate);

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
  @Query("UPDATE FuncCase s SET s.testNum=0, s.testFailNum=0, s.testResult='PENDING', s.testResultHandleDate=null WHERE s.id IN ?1")
  void updateTestResultToInitByIds(Collection<Long> ids);

  @Modifying
  @Query("UPDATE FuncCase s SET s.testNum=0, s.testFailNum=0, s.testResult='PENDING', s.testResultHandleDate=null WHERE s.planId IN ?1")
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
  @Query("UPDATE FuncCase s SET s.reviewFlag=false, s.reviewStatus=null WHERE s.planId = ?1")
  void updateReviewResultToDisabledByPlanId(Long planId);

  @Modifying
  //@Query("UPDATE FuncCase s SET s.reviewStatus='PENDING', s.reviewRemark=null, s.reviewNum=0 WHERE s.planId = ?1")
  @Query("UPDATE FuncCase s SET s.reviewFlag=true, s.reviewStatus='PENDING', s.reviewRemark=null WHERE s.planId = ?1")
  void updateReviewResultToStartedByPlanId(Long planId);

  @Modifying
  @Query("UPDATE FuncCase s SET s.planDeletedFlag=?2 WHERE s.planId IN ?1")
  void updatePlanDeleteStatusByPlan(Collection<Long> planIds, Boolean deletedFlag);

  @Transactional
  @Modifying
  @Query(value = "UPDATE func_case SET overdue_flag=1 WHERE id IN ?1", nativeQuery = true)
  void updateOverdueFlag(Collection<Long> ids);

  @Transactional
  @Modifying
  @Query(value = "UPDATE func_case SET unplanned_flag=?2 WHERE id IN ?1", nativeQuery = true)
  void updateUnplannedFlagByIdIn(Collection<Long> ids, Boolean unplannedFlag);

  @Modifying
  @Query(value = "UPDATE func_case SET software_version = ?3 WHERE project_id = ?1 AND software_version = ?2", nativeQuery = true)
  void updateVersionByProjectIdAndSoftwareVersion(Long projectId, String fromVersion, String toVersion);

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
