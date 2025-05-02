package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioMonitorQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioMonitorSearch;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ScenarioMonitorSearchImpl implements ScenarioMonitorSearch {

  @Resource
  private ScenarioMonitorSearchRepo scenarioMonitorSearchRepo;

  @Resource
  private ScenarioMonitorQuery scenarioMonitorQuery;

  @Override
  public Page<ScenarioMonitor> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<ScenarioMonitor> clz, String... matches) {
    return new BizTemplate<Page<ScenarioMonitor>>() {

      @Override
      protected Page<ScenarioMonitor> process() {
        Page<ScenarioMonitor> page = scenarioMonitorSearchRepo.find(
            criteria, pageable, clz, matches);
        if (page.hasContent()) {
          scenarioMonitorQuery.assembleScenarioMonitorCount(page);
        }
        return page;
      }
    }.execute();
  }
}
