package cloud.xcan.angus.core.tester.domain.mock.service;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MockServiceSearchRepo extends CustomBaseRepository<MockServiceInfo> {

}
