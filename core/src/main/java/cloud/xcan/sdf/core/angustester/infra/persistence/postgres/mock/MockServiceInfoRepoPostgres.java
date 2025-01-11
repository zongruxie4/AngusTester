package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.mock;

import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("mockServiceInfoRepo")
public interface MockServiceInfoRepoPostgres extends MockServiceInfoRepo {

}
