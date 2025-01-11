package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.script;

import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("scriptInfoRepo")
public interface ScriptInfoRepoPostgres extends ScriptInfoRepo {


}
