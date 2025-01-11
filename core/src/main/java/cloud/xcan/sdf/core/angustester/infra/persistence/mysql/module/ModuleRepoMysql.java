package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.module;

import cloud.xcan.sdf.core.angustester.domain.module.ModuleRepo;
import org.springframework.stereotype.Repository;

@Repository("moduleRepo")
public interface ModuleRepoMysql extends ModuleRepo {

}
