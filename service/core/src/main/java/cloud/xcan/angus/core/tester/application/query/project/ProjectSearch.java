package cloud.xcan.angus.core.tester.application.query.project;

import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectSearch {

  Page<Project> search(Set<SearchCriteria> criteria, Pageable pageable, Class<Project> clz,
      String... matches);

}




