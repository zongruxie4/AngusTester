package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogInfo;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class MockApisLogSearchRepoMysql extends SimpleSearchRepository<MockApisLogInfo>
    implements MockApisLogSearchRepo {

}
