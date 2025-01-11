package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApis;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApisSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MockApisSearchRepoMysql extends SimpleSearchRepository<MockApis>
    implements MockApisSearchRepo {

}
