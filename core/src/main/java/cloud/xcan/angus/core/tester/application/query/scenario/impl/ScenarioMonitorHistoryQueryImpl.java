package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioMonitorHistoryQuery;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistory;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistoryInfo;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistoryInfoRepo;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistoryRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ScenarioMonitorHistoryQueryImpl implements ScenarioMonitorHistoryQuery {

  @Resource
  private ScenarioMonitorHistoryRepo scenarioMonitorHistoryRepo;

  @Resource
  private ScenarioMonitorHistoryInfoRepo scenarioMonitorHistoryInfoRepo;

  @Override
  public ScenarioMonitorHistory detail(Long id) {
    return new BizTemplate<ScenarioMonitorHistory>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected ScenarioMonitorHistory process() {
        return checkAndFind(id);
      }
    }.execute();
  }

  @Override
  public Page<ScenarioMonitorHistoryInfo> find(GenericSpecification<ScenarioMonitorHistoryInfo> spec,
      PageRequest pageable) {
    return new BizTemplate<Page<ScenarioMonitorHistoryInfo>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<ScenarioMonitorHistoryInfo> process() {
        return scenarioMonitorHistoryInfoRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public List<ScenarioMonitorHistoryInfo> findInfoById(Collection<Long> monitorIds) {
    return scenarioMonitorHistoryInfoRepo.findByMonitorIdIn(monitorIds);
  }

  @Override
  public ScenarioMonitorHistory checkAndFind(Long id) {
    return scenarioMonitorHistoryRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ScenarioMonitorHistory"));
  }
}
