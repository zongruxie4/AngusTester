package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.project;

import cloud.xcan.angus.core.tester.domain.project.module.ModuleRepo;
import org.springframework.stereotype.Repository;

@Repository("moduleRepo")
public interface ModuleRepoPostgres extends ModuleRepo {

}
