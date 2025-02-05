package cloud.xcan.sdf.core.angustester.domain.data.dataset;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DatasetSearchRepo extends CustomBaseRepository<Dataset> {

}
