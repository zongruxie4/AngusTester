package cloud.xcan.sdf.core.angustester.domain.mock.apis;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MockApisBaseRepo extends BaseRepository<MockApisBase, Long>,
    NameJoinRepository<MockApisBase, Long> {


}
