package cloud.xcan.sdf.core.angustester.domain.mock.apis.log;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author xiaolong.liu
 */
@NoRepositoryBean
public interface MockApisLogInfoRepo extends BaseRepository<MockApisLogInfo, Long>,
    NameJoinRepository<MockApisLogInfo, Long> {

}
