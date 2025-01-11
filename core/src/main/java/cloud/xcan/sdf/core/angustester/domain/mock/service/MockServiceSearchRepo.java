package cloud.xcan.sdf.core.angustester.domain.mock.service;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MockServiceSearchRepo extends CustomBaseRepository<MockServiceInfo> {

}
