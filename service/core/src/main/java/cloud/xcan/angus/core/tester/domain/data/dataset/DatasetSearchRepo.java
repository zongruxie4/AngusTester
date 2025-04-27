package cloud.xcan.angus.core.tester.domain.data.dataset;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DatasetSearchRepo extends CustomBaseRepository<Dataset> {

}
