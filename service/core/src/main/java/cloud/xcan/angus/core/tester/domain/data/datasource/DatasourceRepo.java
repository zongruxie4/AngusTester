package cloud.xcan.angus.core.tester.domain.data.datasource;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DatasourceRepo extends BaseRepository<Datasource, Long> {

  long countByName(String name);

  long countByNameAndIdNot(String name, Long id);

  long countAllByTenantId(Long tenantId);

}
