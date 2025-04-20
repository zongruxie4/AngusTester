package cloud.xcan.angus.core.tester.infra.persistence.postgres.script;

import cloud.xcan.angus.core.tester.domain.script.ScriptInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("scriptInfoRepo")
public interface ScriptInfoRepoPostgres extends ScriptInfoRepo {


}
