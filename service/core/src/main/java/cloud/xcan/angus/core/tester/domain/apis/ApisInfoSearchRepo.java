package cloud.xcan.angus.core.tester.domain.apis;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisInfoSearchRepo extends CustomBaseRepository<ApisBasicInfo> {

}
