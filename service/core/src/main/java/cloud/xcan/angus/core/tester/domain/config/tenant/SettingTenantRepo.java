package cloud.xcan.angus.core.tester.domain.config.tenant;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SettingTenantRepo extends BaseRepository<SettingTenant, Long> {

  Optional<SettingTenant> findByTenantId(Long tenantId);

  boolean existsByTenantId(Long tenantId);

}
