package cloud.xcan.angus.core.tester.domain.scenario.monitor;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScenarioMonitorHistoryInfoRepo extends
    BaseRepository<ScenarioMonitorHistoryInfo, Long> {

  List<ScenarioMonitorHistoryInfo> findByMonitorIdIn(Collection<Long> monitorIds);

}
