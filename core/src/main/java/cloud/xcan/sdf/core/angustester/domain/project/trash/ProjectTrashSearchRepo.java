package cloud.xcan.sdf.core.angustester.domain.project.trash;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ProjectTrashSearchRepo extends CustomBaseRepository<ProjectTrash> {


}
