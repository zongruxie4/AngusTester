package cloud.xcan.angus.core.tester.infra.persistence.postgres.module;

import cloud.xcan.angus.core.tester.domain.module.ModuleRepo;
import org.springframework.stereotype.Repository;

@Repository("moduleRepo")
public interface ModuleRepoPostgres extends ModuleRepo {

}
