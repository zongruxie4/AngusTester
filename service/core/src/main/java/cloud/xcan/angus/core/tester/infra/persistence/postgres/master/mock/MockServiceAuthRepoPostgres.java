package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.mock;

import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuthRepo;
import org.springframework.stereotype.Repository;

@Repository("mockServiceAuthRepo")
public interface MockServiceAuthRepoPostgres extends MockServiceAuthRepo {


}
