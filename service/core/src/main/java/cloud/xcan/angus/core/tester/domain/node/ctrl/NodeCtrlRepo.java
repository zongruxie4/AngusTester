package cloud.xcan.angus.core.tester.domain.node.ctrl;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NodeCtrlRepo extends BaseRepository<NodeCtrl, Long> {

}
