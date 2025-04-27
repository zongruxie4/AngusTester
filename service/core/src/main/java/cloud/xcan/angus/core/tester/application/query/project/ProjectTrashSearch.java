package cloud.xcan.angus.core.tester.application.query.project;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectTrashSearch {

  Page<ProjectTrash> search(Set<SearchCriteria> criteria, Pageable pageable, Class<ProjectTrash> clz,
      String... matches);

}




