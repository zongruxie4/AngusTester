package cloud.xcan.angus.core.tester.domain.mock.apis.log;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author XiaoLong Liu
 */
@NoRepositoryBean
public interface MockApisLogInfoRepo extends BaseRepository<MockApisLogInfo, Long>,
    NameJoinRepository<MockApisLogInfo, Long> {

}
