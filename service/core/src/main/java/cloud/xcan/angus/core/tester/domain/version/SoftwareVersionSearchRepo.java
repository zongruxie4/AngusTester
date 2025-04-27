package cloud.xcan.angus.core.tester.domain.version;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SoftwareVersionSearchRepo extends CustomBaseRepository<SoftwareVersion> {

}
