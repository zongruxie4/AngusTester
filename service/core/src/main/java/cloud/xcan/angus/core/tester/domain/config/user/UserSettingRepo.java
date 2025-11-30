package cloud.xcan.angus.core.tester.domain.config.user;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import org.springframework.data.repository.NoRepositoryBean;

@DoInFuture("Add cache")
@NoRepositoryBean
public interface UserSettingRepo extends BaseRepository<UserSetting, Long> {

  boolean existsByTenantId(Long tenantId);
}
