package cloud.xcan.angus.core.tester.domain.data.datasource;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DatasourceSearchRepo extends CustomBaseRepository<Datasource> {

}
