package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioFollowQuery;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.scenario.follow.ScenarioFollow;
import cloud.xcan.angus.core.tester.domain.scenario.follow.ScenarioFollowRepo;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of ScenarioFollowQuery for scenario follow management and query operations.
 * </p>
 * <p>
 * Provides methods for listing user's followed scenarios, counting follows, and setting scenario information.
 * </p>
 */
@Biz
public class ScenarioFollowQueryImpl implements ScenarioFollowQuery {

  @Resource
  private ScenarioFollowRepo scenarioFollowRepo;
  @Resource
  private ScenarioRepo scenarioRepo;

  /**
   * <p>
   * List user's followed scenarios with optional name filtering and pagination.
   * </p>
   * <p>
   * Sets scenario name and plugin information for each followed item.
   * </p>
   * @param projectId Project ID for filtering
   * @param name Optional scenario name filter
   * @param pageable Pagination information
   * @return Page of followed scenarios
   */
  @Override
  public Page<ScenarioFollow> list(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<ScenarioFollow>>() {

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

  /**
   * <p>
   * Count the number of followed scenarios for the current user.
   * </p>
   * <p>
   * If projectId is provided, counts follows within that project only.
   * Otherwise, counts all follows for the user.
   * </p>
   * @param projectId Optional project ID for filtering
   * @return Number of followed scenarios
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return isNull(projectId) ? scenarioFollowRepo.countByCreatedBy(getUserId())
            : scenarioFollowRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

  /**
   * <p>
   * Set scenario information (name and plugin) for a list of followed scenarios.
   * </p>
   * <p>
   * Batch retrieves scenario information to avoid N+1 query problems.
   * </p>
   * @param follows List of followed scenarios to update
   */
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




