package cloud.xcan.angus.core.tester.domain.mock.apis;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MockApisBaseRepo extends BaseRepository<MockApisBase, Long>,
    NameJoinRepository<MockApisBase, Long> {


}
