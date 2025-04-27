package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class MockServiceSearchRepoMysql extends SimpleSearchRepository<MockServiceInfo>
    implements MockServiceSearchRepo {

}
