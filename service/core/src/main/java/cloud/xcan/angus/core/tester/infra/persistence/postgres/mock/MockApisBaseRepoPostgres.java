package cloud.xcan.angus.core.tester.infra.persistence.postgres.mock;

import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisBaseRepo;
import org.springframework.stereotype.Repository;

@Repository("mockApisBaseRepo")
public interface MockApisBaseRepoPostgres extends MockApisBaseRepo {

}
