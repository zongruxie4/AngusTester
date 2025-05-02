package cloud.xcan.angus.core.tester.domain.exec;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.model.script.ScriptSource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ExecInfoRepo extends BaseRepository<ExecInfo, Long> {

  List<ExecInfo> findAllByScriptIdIn(Collection<Long> scriptIds);

  @Query(value = "SELECT e.* FROM exec e, exec_node n WHERE e.id = n.exec_id AND n.node_id = ?1", nativeQuery = true)
  List<ExecInfo> findAllByNodeId(Long nodeId);

  @Query(value = "SELECT * FROM exec WHERE actual_start_date <= ?1 AND status = 'RUNNING' LIMIT ?2", nativeQuery = true)
  List<ExecInfo> findRunningByDuration(LocalDateTime plusMinutes, int count);

  List<ExecInfo> findByScriptSourceAndScriptSourceIdIn(ScriptSource resourceType,
      Collection<Long> resourceIds);

  @Transactional
  @Modifying
  @Query(value = "UPDATE exec SET status = ?2, end_date = ?3, meter_status = ?4, meter_message = ?5  WHERE id = ?1", nativeQuery = true)
  void updateFinishById(Long execId, String status, LocalDateTime now, String meterStatus,
      String meterMessage);

}
