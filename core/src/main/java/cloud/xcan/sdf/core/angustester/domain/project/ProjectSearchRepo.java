package cloud.xcan.sdf.core.angustester.domain.project;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ProjectSearchRepo extends CustomBaseRepository<Project> {


}
