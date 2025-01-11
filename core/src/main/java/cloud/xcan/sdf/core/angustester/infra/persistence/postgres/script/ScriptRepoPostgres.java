package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.script;

import cloud.xcan.sdf.core.angustester.domain.script.ScriptRepo;
import org.springframework.stereotype.Repository;

@Repository("scriptRepo")
public interface ScriptRepoPostgres extends ScriptRepo {

}
