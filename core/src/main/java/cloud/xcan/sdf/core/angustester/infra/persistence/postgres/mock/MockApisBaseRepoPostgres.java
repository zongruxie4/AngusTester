package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.mock;

import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApisBaseRepo;
import org.springframework.stereotype.Repository;

@Repository("mockApisBaseRepo")
public interface MockApisBaseRepoPostgres extends MockApisBaseRepo {

}
