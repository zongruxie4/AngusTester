package cloud.xcan.angus.core.tester.domain.func.review;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncReviewRepo extends NameJoinRepository<FuncReview, Long>,
    BaseRepository<FuncReview, Long> {

  long countByProjectIdAndName(Long projectId, String name);

  boolean existsByProjectIdAndName(Long projectId, String name);

  List<FuncReview> findByPlanIdIn(Collection<Long> planIds);

  @Modifying
  @Query(value = "DELETE FROM func_review WHERE plan_id IN ?1", nativeQuery = true)
  void deleteByPlanIdIn(Collection<Long> planIds);

}
