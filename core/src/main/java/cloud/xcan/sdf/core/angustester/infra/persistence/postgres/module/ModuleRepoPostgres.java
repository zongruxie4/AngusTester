package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.module;

import cloud.xcan.sdf.core.angustester.domain.module.ModuleRepo;
import org.springframework.stereotype.Repository;

@Repository("moduleRepo")
public interface ModuleRepoPostgres extends ModuleRepo {

}
