package cloud.xcan.sdf.core.angustester.domain.scenario.monitor;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScenarioMonitorHistoryInfoRepo extends
    BaseRepository<ScenarioMonitorHistoryInfo, Long> {

  List<ScenarioMonitorHistoryInfo> findByMonitorIdIn(Collection<Long> monitorIds);

}
