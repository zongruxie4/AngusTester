package cloud.xcan.angus.core.tester.infra.persistence.mysql.mock;

import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisBaseRepo;
import org.springframework.stereotype.Repository;

@Repository("mockApisBaseRepo")
public interface MockApisBaseRepoMysql extends MockApisBaseRepo {

}
