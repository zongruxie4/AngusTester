package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.mock;

import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuthRepo;
import org.springframework.stereotype.Repository;

@Repository("mockServiceAuthRepo")
public interface MockServiceAuthRepoMysql extends MockServiceAuthRepo {


}
