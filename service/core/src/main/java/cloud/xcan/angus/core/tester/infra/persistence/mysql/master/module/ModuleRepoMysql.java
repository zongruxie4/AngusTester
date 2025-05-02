package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.module;

import cloud.xcan.angus.core.tester.domain.module.ModuleRepo;
import org.springframework.stereotype.Repository;

@Repository("moduleRepo")
public interface ModuleRepoMysql extends ModuleRepo {

}
