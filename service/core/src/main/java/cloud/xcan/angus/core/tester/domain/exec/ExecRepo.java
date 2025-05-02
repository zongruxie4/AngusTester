package cloud.xcan.angus.core.tester.domain.exec;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.core.jpa.entity.projection.IdAndName;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ExecRepo extends BaseRepository<Exec, Long> {

  /**
   * Important: Different test types of apis correspond to different scripts and executions, but
   * multiple test types in a scenario correspond to one script and multiple executions.
   */
  List<Exec> findByScriptId(Long scriptId);

  @Query(value =
      "SELECT * FROM exec WHERE (start_at_date IS NULL OR start_at_date <= NOW()) AND status IN ('CREATED', 'PENDING') "
          + "ORDER BY priority DESC, scheduling_num ASC, created_date ASC LIMIT ?1", nativeQuery = true)
  List<Exec> findSchedulingTask(int count);

  @Query(value = "SELECT * FROM exec WHERE assemble_and_send_event IS NULL AND status IN ('COMPLETED', 'FAILED', 'TIMEOUT') LIMIT ?1", nativeQuery = true)
  List<Exec> findAssembleAndSendEventTask(int count);

  /**
   * Note: Test results are not updated to 1 when they do not need to be updated
   */
  @Query(value = "SELECT * FROM exec WHERE update_test_result = 1 AND sync_test_result = 0 AND status IN ('COMPLETED', 'FAILED') LIMIT ?1", nativeQuery = true)
  List<Exec> findUpdateResultTask(int count);

  @Query(value = "SELECT * FROM exec WHERE actual_start_date <= ?1 AND status = 'RUNNING' LIMIT ?2", nativeQuery = true)
  List<Exec> findRunningByDuration(LocalDateTime plusMinutes, int count);

  @Query(value = "SELECT id, name FROM exec WHERE id IN ?1", nativeQuery = true)
  List<IdAndName> findInfoByIdIn(Collection<Long> execIds);

  @Query(value = "SELECT tenant_id FROM exec WHERE id = ?1 AND trial = 1", nativeQuery = true)
  Long findActualTenantIdByIdAndTrial(Long execId);

  long countByTenantId(Long optTenantId);

  long countByTenantIdAndStatus(Long optTenantId, ExecStatus status);

  long countByTenantIdAndTrial(Long optTenantId, boolean trial);

  @Transactional
  @Modifying
  @Query(value = "UPDATE exec SET status = ?2, end_date = ?3, meter_status = ?4, meter_message = ?5 WHERE id = ?1", nativeQuery = true)
  void updateFinishById(Long execId, String status, LocalDateTime now, String meterStatus,
      String meterMessage);

  @Transactional
  @Modifying
  @Query(value = "UPDATE exec SET status = ?2, scheduling_num = scheduling_num + 1, last_scheduling_date = ?3, last_scheduling_result = ?4 , last_modified_by = ?5, last_modified_date = ?6 WHERE id = ?1", nativeQuery = true)
  void updateSchedulingFailed(Long execId, String status, LocalDateTime now, String result,
      Long lastModifiedBy, LocalDateTime lastModifiedDate);

  @Transactional
  @Modifying
  @Query(value = "UPDATE exec SET status = ?2, last_modified_by = ?3, last_modified_date = ?4 WHERE id = ?1", nativeQuery = true)
  void updateStatusById(Long id, String status, Long lastModifiedBy,
      LocalDateTime lastModifiedDate);

  @Transactional
  @Modifying
  @Query(value = "UPDATE exec SET status = 'STOPPED', end_date = ?2, last_modified_by = ?3, last_modified_date = ?2 WHERE id = ?1", nativeQuery = true)
  void updateStoppedStatusById(Long id, LocalDateTime now, Long lastModifiedBy);

  @Transactional
  @Modifying
  @Query(value = "UPDATE exec SET status = 'PENDING' WHERE id = ?1", nativeQuery = true)
  void updatePendingStatusById(Long id);

  @Transactional
  @Modifying
  @Query(value = "UPDATE exec SET sync_test_result = 1, sync_test_result_failure = ?2 WHERE id = ?1", nativeQuery = true)
  void updateSyncTestResult(Long execId, String syncTestResultFailure);

  @Transactional
  @Modifying
  @Query(value = "UPDATE exec SET assemble_and_send_event = ?2 WHERE id = ?1", nativeQuery = true)
  void updateAssembleAndSendEvent(Long id, Boolean flag);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM exec WHERE id in ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);


}
