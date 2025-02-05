package cloud.xcan.sdf.core.angustester.domain.data.datasource;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DatasourceRepo extends BaseRepository<Datasource, Long> {

  long countByName(String name);

  long countByNameAndIdNot(String name, Long id);

  long countAllByTenantId(Long tenantId);

}
