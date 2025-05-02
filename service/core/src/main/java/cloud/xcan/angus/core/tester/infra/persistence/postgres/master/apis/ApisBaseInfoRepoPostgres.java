package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.apis;

import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("apisBaseInfoRepo")
public interface ApisBaseInfoRepoPostgres extends ApisBaseInfoRepo {

}
