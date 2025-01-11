package cloud.xcan.sdf.core.angustester.application.query.scenario.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioMonitorQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioMonitorSearch;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ScenarioMonitorSearchImpl implements ScenarioMonitorSearch {

  @Resource
  private ScenarioMonitorSearchRepo scenarioMonitorSearchRepo;

  @Resource
  private ScenarioMonitorQuery scenarioMonitorQuery;

  @Override
  public Page<ScenarioMonitor> search(Set<SearchCriteria> criterias, PageRequest pageable,
      Class<ScenarioMonitor> clz, String... matches) {
    return new BizTemplate<Page<ScenarioMonitor>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<ScenarioMonitor> process() {
        Page<ScenarioMonitor> page = scenarioMonitorSearchRepo.find(
            criterias, pageable, clz, matches);
        if (page.hasContent()) {
          scenarioMonitorQuery.assembleScenarioMonitorCount(page);
        }
        return page;
      }
    }.execute();
  }
}
