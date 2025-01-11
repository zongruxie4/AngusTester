package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogInfo;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MockApisLogSearchRepoMysql extends SimpleSearchRepository<MockApisLogInfo>
    implements MockApisLogSearchRepo {

}
