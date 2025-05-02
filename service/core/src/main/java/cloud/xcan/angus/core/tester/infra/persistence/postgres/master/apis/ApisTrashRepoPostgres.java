package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.apis;

import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrashRepo;
import org.springframework.stereotype.Repository;

@Repository("apisTrashRepo")
public interface ApisTrashRepoPostgres extends ApisTrashRepo {


}
