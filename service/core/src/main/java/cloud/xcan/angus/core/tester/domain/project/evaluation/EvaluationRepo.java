package cloud.xcan.angus.core.tester.domain.project.evaluation;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EvaluationRepo extends BaseRepository<TestEvaluation, Long>,
    NameJoinRepository<TestEvaluation, Long> {

  List<TestEvaluation> findByProjectId(Long projectId);

  List<TestEvaluation> findByProjectIdAndScope(Long projectId, EvaluationScope scope);

  List<TestEvaluation> findByIdIn(Collection<Long> ids);

  @Override
  @Query(value = "SELECT id FROM test_evaluation WHERE id IN ?1", nativeQuery = true)
  List<Long> findIdByIdIn(Collection<Long> ids);
}

