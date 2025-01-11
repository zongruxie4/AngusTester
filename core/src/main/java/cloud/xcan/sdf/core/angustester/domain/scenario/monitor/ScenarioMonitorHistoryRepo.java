package cloud.xcan.sdf.core.angustester.domain.scenario.monitor;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ScenarioMonitorHistoryRepo extends BaseRepository<ScenarioMonitorHistory, Long> {

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM scenario_monitor_history WHERE monitor_id IN ?1", nativeQuery = true)
  void deleteByMonitorIdIn(Collection<Long> monitorIds);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM scenario_monitor_history WHERE monitor_id = ?1 AND id NOT IN ( "
      + "    SELECT id FROM ("
      + "        SELECT id FROM scenario_monitor_history WHERE monitor_id = ?1"
      + "        ORDER BY created_date DESC LIMIT ?2"
      + "    ) AS t"
      + ")", nativeQuery = true)
  void deleteHistory(Long monitorId, int maxNum);
}
