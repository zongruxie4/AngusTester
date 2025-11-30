package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.config;

import cloud.xcan.angus.core.tester.domain.config.tenant.TenantSettingRepo;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantSettingRepoPostgres extends TenantSettingRepo {

}
