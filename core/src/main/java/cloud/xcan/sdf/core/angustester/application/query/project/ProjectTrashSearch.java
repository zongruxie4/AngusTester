package cloud.xcan.sdf.core.angustester.application.query.project;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrash;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectTrashSearch {

  Page<ProjectTrash> search(Set<SearchCriteria> criterias, Pageable pageable, Class<ProjectTrash> clz,
      String... matches);

}




