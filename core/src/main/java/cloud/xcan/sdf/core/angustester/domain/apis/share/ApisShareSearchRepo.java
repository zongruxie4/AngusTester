package cloud.xcan.sdf.core.angustester.domain.apis.share;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisShareSearchRepo extends CustomBaseRepository<ApisShare> {

}
