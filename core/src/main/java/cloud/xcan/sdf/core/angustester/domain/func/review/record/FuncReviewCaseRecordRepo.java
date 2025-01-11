package cloud.xcan.sdf.core.angustester.domain.func.review.record;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncReviewCaseRecordRepo extends BaseRepository<FuncReviewCaseRecord, Long> {

  List<FuncReviewCaseRecord> findByCaseId(Long caseId);

  @Modifying
  @Query(value = "DELETE FROM func_review_case_record WHERE review_case_id IN ?1", nativeQuery = true)
  void deleteByReviewCaseIdIn(Collection<Long> review);

}
