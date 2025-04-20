package cloud.xcan.angus.core.tester.infra.persistence.postgres.mock;

import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("mockServiceInfoRepo")
public interface MockServiceInfoRepoPostgres extends MockServiceInfoRepo {

}
