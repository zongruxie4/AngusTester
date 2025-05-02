package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.apis;

import cloud.xcan.angus.core.tester.domain.apis.ApisBasicAndConfigInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("apisBasicAndConfigInfoRepo")
public interface ApisBasicAndConfigInfoRepoPostgres extends ApisBasicAndConfigInfoRepo {

}
