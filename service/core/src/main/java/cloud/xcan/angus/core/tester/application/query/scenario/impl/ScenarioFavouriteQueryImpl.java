package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioFavouriteQuery;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavourite;
import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavouriteRepo;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Implementation of ScenarioFavouriteQuery for scenario favorite management and query operations.
 * </p>
 * <p>
 * Provides methods for listing user's favorite scenarios, counting favorites, and setting scenario
 * information.
 * </p>
 */
@Service
public class ScenarioFavouriteQueryImpl implements ScenarioFavouriteQuery {

  @Resource
  private ScenarioFavouriteRepo scenarioFavouriteRepo;
  @Resource
  private ScenarioRepo scenarioRepo;

  /**
   * <p>
   * List user's favorite scenarios with optional name filtering and pagination.
   * </p>
   * <p>
   * Sets scenario name and plugin information for each favorite item.
   * </p>
   *
   * @param projectId Project ID for filtering
   * @param name      Optional scenario name filter
   * @param pageable  Pagination information
   * @return Page of favorite scenarios
   */
  @Override
  public Page<ScenarioFavourite> list(Long projectId, String name, PageRequest pageable) {
    return new BizTemplate<Page<ScenarioFavourite>>() {

      @Override
      protected Page<ScenarioFavourite> process() {
        Page<ScenarioFavourite> page = isEmpty(name)
            ? scenarioFavouriteRepo.search(projectId, getUserId(), pageable)
            : scenarioFavouriteRepo.searchByMatch(projectId, getUserId(), name, pageable);
        setScenarioInfo(page.getContent());
        return page;
      }
    }.execute();
  }

  /**
   * <p>
   * Count the number of favorite scenarios for the current user.
   * </p>
   * <p>
   * If projectId is provided, counts favorites within that project only. Otherwise, counts all
   * favorites for the user.
   * </p>
   *
   * @param projectId Optional project ID for filtering
   * @return Number of favorite scenarios
   */
  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return isNull(projectId) ? scenarioFavouriteRepo.countByCreatedBy(getUserId())
            : scenarioFavouriteRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

  /**
   * <p>
   * Set scenario information (name and plugin) for a list of favorite scenarios.
   * </p>
   * <p>
   * Batch retrieves scenario information to avoid N+1 query problems.
   * </p>
   *
   * @param favourites List of favorite scenarios to update
   */
  public void setScenarioInfo(List<ScenarioFavourite> favourites) {
    if (isNotEmpty(favourites)) {
      Map<Long, Scenario> scenarioMap = scenarioRepo
          .findAll0ByIdIn(favourites.stream().map(ScenarioFavourite::getScenarioId).collect(
              Collectors.toSet())).stream().collect(Collectors.toMap(Scenario::getId, x -> x));
      for (ScenarioFavourite favourite : favourites) {
        if (scenarioMap.containsKey(favourite.getScenarioId())) {
          favourite.setScenarioName(scenarioMap.get(favourite.getScenarioId()).getName());
          favourite.setPlugin(scenarioMap.get(favourite.getScenarioId()).getPlugin());
        }
      }
    }
  }
}




