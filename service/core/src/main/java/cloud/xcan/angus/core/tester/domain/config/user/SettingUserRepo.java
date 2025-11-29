package cloud.xcan.angus.core.tester.domain.config.user;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

@DoInFuture("Add cache")
@NoRepositoryBean
public interface SettingUserRepo extends BaseRepository<SettingUser, Long> {

  boolean existsByTenantId(Long tenantId);
}
