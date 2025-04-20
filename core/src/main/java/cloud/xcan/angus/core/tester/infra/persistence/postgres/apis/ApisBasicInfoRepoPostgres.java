package cloud.xcan.angus.core.tester.infra.persistence.postgres.apis;

import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("apisBasicInfoRepo")
public interface ApisBasicInfoRepoPostgres extends ApisBasicInfoRepo {

}
