package cloud.xcan.angus.core.tester.domain.apis.design;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisDesignInfoSearchRepo extends CustomBaseRepository<ApisDesignInfo> {

}
