package cloud.xcan.sdf.core.angustester.domain.activity;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ActivitySearchRepo extends CustomBaseRepository<Activity> {

}
