package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.mock;

import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("mockApisLogInfoRepo")
public interface MockApisLogInfoRepoPostgres extends MockApisLogInfoRepo {

}
