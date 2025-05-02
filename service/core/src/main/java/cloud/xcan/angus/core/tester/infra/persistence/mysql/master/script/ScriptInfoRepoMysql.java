package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.script;

import cloud.xcan.angus.core.tester.domain.script.ScriptInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("scriptInfoRepo")
public interface ScriptInfoRepoMysql extends ScriptInfoRepo {

}
