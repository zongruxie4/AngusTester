package cloud.xcan.angus.core.tester.infra.persistence.postgres.apis;

import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import org.springframework.stereotype.Repository;

@Repository("apisRepo")
public interface ApisRepoPostgres extends ApisRepo {


}
