package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioMonitorHistoryQuery;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistory;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistoryInfo;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistoryInfoRepo;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistoryRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of ScenarioMonitorHistoryQuery for scenario monitoring history management and query operations.
 * </p>
 * <p>
 * Provides methods for retrieving scenario monitoring history details, listing history information, and finding history by monitor IDs.
 * </p>
 */
@Biz
public class ScenarioMonitorHistoryQueryImpl implements ScenarioMonitorHistoryQuery {

  @Resource
  private ScenarioMonitorHistoryRepo scenarioMonitorHistoryRepo;
  @Resource
  private ScenarioMonitorHistoryInfoRepo scenarioMonitorHistoryInfoRepo;

  /**
   * <p>
   * Get detailed information of a scenario monitoring history record.
   * </p>
   * @param id Monitoring history ID
   * @return Scenario monitoring history entity
   */
  @Override
  public ScenarioMonitorHistory detail(Long id) {
    return new BizTemplate<ScenarioMonitorHistory>() {

      @Override
      protected ScenarioMonitorHistory process() {
        return checkAndFind(id);
      }
    }.execute();
  }

  /**
   * <p>
   * List scenario monitoring history information with pagination and search criteria.
   * </p>
   * @param spec Search specification for filtering
   * @param pageable Pagination information
   * @return Page of scenario monitoring history information
   */
  @Override
  public Page<ScenarioMonitorHistoryInfo> list(
      GenericSpecification<ScenarioMonitorHistoryInfo> spec, PageRequest pageable) {
    return new BizTemplate<Page<ScenarioMonitorHistoryInfo>>() {

      @Override
      protected Page<ScenarioMonitorHistoryInfo> process() {
        return scenarioMonitorHistoryInfoRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * <p>
   * Find monitoring history information by a collection of monitor IDs.
   * </p>
   * <p>
   * Used for batch retrieval of monitoring history information to avoid N+1 query problems.
   * </p>
   * @param monitorIds Collection of monitor IDs
   * @return List of monitoring history information
   */
  @Override
  public List<ScenarioMonitorHistoryInfo> findInfoById(Collection<Long> monitorIds) {
    return scenarioMonitorHistoryInfoRepo.findByMonitorIdIn(monitorIds);
  }

  /**
   * <p>
   * Check and find a scenario monitoring history record by ID.
   * </p>
   * <p>
   * Throws ResourceNotFound exception if the record does not exist.
   * </p>
   * @param id Monitoring history ID
   * @return Scenario monitoring history entity
   */
  @Override
  public ScenarioMonitorHistory checkAndFind(Long id) {
    return scenarioMonitorHistoryRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ScenarioMonitorHistory"));
  }
}
