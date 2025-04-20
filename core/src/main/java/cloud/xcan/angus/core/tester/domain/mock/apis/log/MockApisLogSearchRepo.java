package cloud.xcan.angus.core.tester.domain.mock.apis.log;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MockApisLogSearchRepo extends CustomBaseRepository<MockApisLogInfo> {

}
