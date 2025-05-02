package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.apis;

import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuthRepo;
import org.springframework.stereotype.Repository;

@Repository("apisAuthRepo")
public interface ApisAuthRepoMysql extends ApisAuthRepo {


}
