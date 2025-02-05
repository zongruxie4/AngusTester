package cloud.xcan.sdf.core.angustester.domain.data.datasource;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DatasourceSearchRepo extends CustomBaseRepository<Datasource> {

}
