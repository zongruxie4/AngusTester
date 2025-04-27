package cloud.xcan.angus.core.tester.domain.mock.apis;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MockApisSearchRepo extends CustomBaseRepository<MockApis> {


}
