package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class MockApisSearchRepoMysql extends SimpleSearchRepository<MockApis>
    implements MockApisSearchRepo {

}
