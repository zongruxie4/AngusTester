package cloud.xcan.sdf.core.angustester.domain.scenario.monitor;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import cloud.xcan.sdf.spec.annotations.DoInFuture;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ScenarioMonitorRepo extends BaseRepository<ScenarioMonitor, Long>,
    NameJoinRepository<ScenarioMonitor, Long> {

  long countByProjectIdAndName(Long projectId, String name);

  @DoInFuture("Do not use index when there are two `OR` condition")
  @Query(value = "SELECT * FROM scenario_monitor WHERE (status = 'PENDING' AND created_at = 'NOW') "
      + " OR (status = 'PENDING' AND created_at = 'AT_SOME_DATE' AND next_exec_date < NOW()) "
      + " OR (created_at = 'PERIODICALLY' AND next_exec_date < NOW()) "
      + " LIMIT ?1", nativeQuery = true)
  List<ScenarioMonitor> findRunByNow(Long count);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM scenario_monitor WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
