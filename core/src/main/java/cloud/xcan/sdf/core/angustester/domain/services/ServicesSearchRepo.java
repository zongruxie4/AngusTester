package cloud.xcan.sdf.core.angustester.domain.services;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ServicesSearchRepo extends CustomBaseRepository<Services> {

}
