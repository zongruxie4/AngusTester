package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.mock;

import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("mockApisLogInfoRepo")
public interface MockApisLogInfoRepoMysql extends MockApisLogInfoRepo {

}
