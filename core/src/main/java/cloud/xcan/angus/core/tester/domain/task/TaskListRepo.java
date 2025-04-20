package cloud.xcan.angus.core.tester.domain.task;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.task.count.TaskCount;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskListRepo extends CustomBaseRepository<Task> {

  StringBuilder getSqlTemplate0(SearchMode mode, Class<Task> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches);

  String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params);

  TaskCount count(Set<SearchCriteria> criteria);
}
