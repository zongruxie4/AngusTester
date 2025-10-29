package cloud.xcan.angus.core.tester.domain.test.review.cases;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.domain.test.review.ReviewCaseNum;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncReviewCaseRepo extends BaseRepository<FuncReviewCase, Long> {

  @Query(value = "SELECT case_id FROM func_review_case WHERE review_id = ?1", nativeQuery = true)
  Set<Long> findCaseIdByReviewId(Long id);

  @Query(value = "SELECT count(ud.id) as caseNum, ud.reviewId as reviewId FROM FuncReviewCase ud WHERE ud.reviewId IN (?1) GROUP BY ud.reviewId")
  List<ReviewCaseNum> findReviewCaseNumsGroupByReviewId(Set<Long> reviewIds);

  @Query(value = "SELECT count(ud.id) as caseNum, ud.reviewId as reviewId FROM FuncReviewCase ud WHERE ud.reviewId IN (?1) AND ud.reviewStatus = 'PASSED' GROUP BY ud.reviewId")
  List<ReviewCaseNum> findReviewPassedCaseNumsGroupByReviewId(Collection<Long> reviewIds);

  @Query(value = "SELECT case_id FROM func_review_case WHERE review_id = ?1 AND review_status = 'PENDING' ", nativeQuery = true)
  Set<Long> findPendingCaseIdByReviewId(Long reviewId);

  @Query(value = "SELECT case_id FROM func_review_case WHERE case_id IN ?1 AND review_id <> ?2 AND review_status = 'PENDING' ", nativeQuery = true)
  Set<Long> findPendingCaseIdByCaseIdInAndReviewIdNot(List<Long> caseIds, Long reviewId);

  @Query(value = "SELECT case_id FROM func_review_case WHERE plan_id = ?1 AND review_id <> ?2 AND review_status = 'PENDING' ", nativeQuery = true)
  Set<Long> findPendingCaseIdByPlanIdAndReviewIdNot(Long planId, Long reviewId);

  @Query(value = "SELECT case_id FROM func_review_case WHERE plan_id = ?1 AND review_status = 'PENDING' ", nativeQuery = true)
  Set<Long> findPendingCaseIdByPlanId(Long planId);

  @Query(value = "SELECT case_id FROM func_review_case WHERE plan_id = ?1 AND review_id = ?2 ", nativeQuery = true)
  Set<Long> findCaseIdByPlanIdAndReviewId(Long planId, Long reviewId);

  @Modifying
  @Query("UPDATE FuncReviewCase s SET s.reviewerId=null, s.reviewDate=null, s.reviewStatus='PENDING', s.reviewRemark=null WHERE s.reviewId = ?1 AND s.caseId IN ?2")
  void updateReviewResultToInitByReviewIdAndCaseIds(Long reviewId, Collection<Long> caseIds);

  @Modifying
  @Query(value = "UPDATE func_review_case s SET s.case_name=?1 WHERE s.case_id = ?2", nativeQuery = true)
  void updateNameByCaseId(String caseName, Long caseId);

  @Modifying
  @Query(value = "DELETE FROM func_review_case WHERE plan_id IN ?1", nativeQuery = true)
  void deleteByPlanIdIn(Collection<Long> planIds);

  @Modifying
  @Query(value = "DELETE FROM func_review_case WHERE case_id IN ?1", nativeQuery = true)
  void deleteByCaseIdIn(Collection<Long> caseIds);

  @Modifying
  @Query(value = "DELETE FROM func_review_case WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
