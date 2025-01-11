package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.mock;

import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServiceAuthRepo;
import org.springframework.stereotype.Repository;

@Repository("mockServiceAuthRepo")
public interface MockServiceAuthRepoMysql extends MockServiceAuthRepo {


}
