package cloud.xcan.sdf.core.angustester.domain.mock.apis.log;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MockApisLogSearchRepo extends CustomBaseRepository<MockApisLogInfo> {

}
