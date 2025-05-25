package cloud.xcan.angus.core.tester.domain.exec.debug;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ExecDebugRepo extends BaseRepository<ExecDebug, Long> {

  long countByTenantId(Long optTenantId);

  long countByTenantIdAndStatus(Long optTenantId, ExecStatus status);

  ExecDebug findBySourceAndScriptId(ExecDebugSource source, Long scriptId);

  ExecDebug findBySourceAndScenarioId(ExecDebugSource source, Long scenarioId);

  ExecDebug findBySourceAndMonitorId(ExecDebugSource source, Long monitorId);

  @Query(value = "SELECT tenant_id FROM exec_debug WHERE id = ?1 ", nativeQuery = true)
  Long findActualTenantIdByIdAndTrial(Long id);

  @Query(value = "SELECT id FROM exec_debug WHERE script_id = ?2 AND source = ?1", nativeQuery = true)
  List<Long> findIdBySourceAndScriptId(String source, Long scriptId);

  @Transactional
  @Modifying
  @Query(value = "UPDATE exec_debug SET status = ?2, end_date = ?3, meter_status = ?4, meter_message = ?5 WHERE id = ?1", nativeQuery = true)
  void updateFinishById(Long execId, String status, LocalDateTime now, String meterStatus,
      String meterMessage);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM exec_debug WHERE script_id IS NOT NULL AND (status = 'COMPLETED' OR created_date <= ?1)", nativeQuery = true)
  void deleteTempFinishAndTimeout(LocalDateTime timeout);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM exec_debug WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(List<Long> ids);
}
