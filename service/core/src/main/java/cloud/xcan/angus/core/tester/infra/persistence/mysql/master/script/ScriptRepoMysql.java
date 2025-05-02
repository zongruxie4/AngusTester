package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.script;

import cloud.xcan.angus.core.tester.domain.script.ScriptRepo;
import org.springframework.stereotype.Repository;

@Repository("scriptRepo")
public interface ScriptRepoMysql extends ScriptRepo {


}
