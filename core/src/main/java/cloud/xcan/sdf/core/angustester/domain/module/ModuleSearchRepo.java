package cloud.xcan.sdf.core.angustester.domain.module;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ModuleSearchRepo extends CustomBaseRepository<Module> {

}
