package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static org.apache.commons.lang.StringUtils.isEmpty;

import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioFollowQuery;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.scenario.follow.ScenarioFollow;
import cloud.xcan.angus.core.tester.domain.scenario.follow.ScenarioFollowRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ScenarioFollowQueryImpl implements ScenarioFollowQuery {

  @Resource
  private ScenarioFollowRepo scenarioFollowRepo;

  @Resource
  private ScenarioRepo scenarioRepo;

  @Override
  public Page<ScenarioFollow> search(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<ScenarioFollow>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<ScenarioFollow> process() {
        Page<ScenarioFollow> page = isEmpty(name)
            ? scenarioFollowRepo.search(projectId, getUserId(), pageable)
            : scenarioFollowRepo.searchByMatch(projectId, getUserId(), name, pageable);
        setScenarioInfo(page.getContent());
        return page;
      }
    }.execute();
  }

  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Long process() {
        return isNull(projectId) ? scenarioFollowRepo.countByCreatedBy(getUserId())
            : scenarioFollowRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

  public void setScenarioInfo(List<ScenarioFollow> follows) {
    if (isNotEmpty(follows)) {
      Map<Long, Scenario> scenarioMap = scenarioRepo
          .findAll0ByIdIn(follows.stream().map(ScenarioFollow::getScenarioId).collect(
              Collectors.toSet())).stream().collect(Collectors.toMap(Scenario::getId, x -> x));
      for (ScenarioFollow follow : follows) {
        if (scenarioMap.containsKey(follow.getScenarioId())) {
          follow.setScenarioName(scenarioMap.get(follow.getScenarioId()).getName());
          follow.setPlugin(scenarioMap.get(follow.getScenarioId()).getPlugin());
        }
      }
    }
  }
}




