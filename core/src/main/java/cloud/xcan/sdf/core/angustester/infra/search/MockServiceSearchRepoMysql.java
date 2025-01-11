package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceInfo;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MockServiceSearchRepoMysql extends SimpleSearchRepository<MockServiceInfo>
    implements MockServiceSearchRepo {

}
