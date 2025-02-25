package cloud.xcan.sdf.core.angustester.domain.apis.design;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisDesignInfoSearchRepo extends CustomBaseRepository<ApisDesignInfo> {

}
