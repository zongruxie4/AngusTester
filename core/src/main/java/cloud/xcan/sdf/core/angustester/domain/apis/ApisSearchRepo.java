package cloud.xcan.sdf.core.angustester.domain.apis;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisSearchRepo extends CustomBaseRepository<Apis> {

}
