package cloud.xcan.angus.core.tester.domain.module;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ModuleSearchRepo extends CustomBaseRepository<Module> {

}
