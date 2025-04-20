package cloud.xcan.angus.core.tester.infra.persistence.mysql.mock;

import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceRepo;
import org.springframework.stereotype.Repository;

@Repository("mockServiceRepo")
public interface MockServiceRepoMysql extends MockServiceRepo {


}
