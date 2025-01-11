package cloud.xcan.sdf.core.angustester.domain.mock.apis;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MockApisSearchRepo extends CustomBaseRepository<MockApis> {


}
