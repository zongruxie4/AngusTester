package cloud.xcan.angus.core.tester.domain.project;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ProjectSearchRepo extends CustomBaseRepository<Project> {


}
