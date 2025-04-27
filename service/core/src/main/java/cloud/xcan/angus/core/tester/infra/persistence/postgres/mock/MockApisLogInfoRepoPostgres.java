package cloud.xcan.angus.core.tester.infra.persistence.postgres.mock;

import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("mockApisLogInfoRepo")
public interface MockApisLogInfoRepoPostgres extends MockApisLogInfoRepo {

}
