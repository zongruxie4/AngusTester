package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.project;

import cloud.xcan.angus.core.tester.domain.project.module.ModuleRepo;
import org.springframework.stereotype.Repository;

@Repository("moduleRepo")
public interface ModuleRepoMysql extends ModuleRepo {

}
