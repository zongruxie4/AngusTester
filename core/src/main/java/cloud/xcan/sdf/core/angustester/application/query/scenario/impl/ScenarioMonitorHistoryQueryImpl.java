package cloud.xcan.sdf.core.angustester.application.query.scenario.impl;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioMonitorHistoryQuery;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistory;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistoryInfo;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistoryInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistoryRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
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
