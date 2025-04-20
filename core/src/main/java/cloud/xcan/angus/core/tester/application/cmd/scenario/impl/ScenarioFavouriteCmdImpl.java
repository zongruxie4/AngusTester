package cloud.xcan.angus.core.tester.application.cmd.scenario.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCE_FAVOURITE_REPEATED;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioFavouriteCmd;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavourite;
import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavouriteRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ScenarioFavouriteCmdImpl extends CommCmd<ScenarioFavourite, Long> implements
    ScenarioFavouriteCmd {

  @Resource
  private ScenarioFavouriteRepo scenarioFavoriteRepo;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Override
  public IdKey<Long, Object> add(ScenarioFavourite favorite) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check the scenario existed
        scenarioDb = scenarioQuery.checkAndFind0(favorite.getScenarioId());

        // Check the permission to view scenario
        scenarioAuthQuery.checkViewAuth(getUserId(), favorite.getScenarioId());

        // Check the is not repeated
        if (scenarioFavoriteRepo.countByScenarioIdAndCreatedBy(favorite.getScenarioId(),
            getUserId()) > 0) {
          throw ResourceExisted.of(SCE_FAVOURITE_REPEATED);
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        favorite.setProjectId(scenarioDb.getProjectId());
        IdKey<Long, Object> idKey = insert(favorite);

        // Add favourite scenario activity
        activityCmd.add(toActivity(SCENARIO, scenarioDb, ActivityType.FAVOURITE));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancel(Long id) {
    new BizTemplate<Void>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check the scenario existed
        scenarioDb = scenarioQuery.checkAndFind0(id);
      }

      @Override
      protected Void process() {
        if (scenarioFavoriteRepo.deleteByScenarioIdAndCreatedBy(id, getUserId()) > 0) {
          //Add cancel favourite api activity
          activityCmd.add(toActivity(SCENARIO, scenarioDb, ActivityType.FAVOURITE_CANCEL));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancelAll(Long projectId) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        if (isNull(projectId)) {
          scenarioFavoriteRepo.deleteByCreatedBy(getUserId());
        } else {
          scenarioFavoriteRepo.deleteByProjectIdAndCreatedBy(projectId, getUserId());
        }
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<ScenarioFavourite, Long> getRepository() {
    return this.scenarioFavoriteRepo;
  }

}




