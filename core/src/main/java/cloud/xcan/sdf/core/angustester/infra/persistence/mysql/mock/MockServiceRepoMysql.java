package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.mock;

import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceRepo;
import org.springframework.stereotype.Repository;

@Repository("mockServiceRepo")
public interface MockServiceRepoMysql extends MockServiceRepo {


}
