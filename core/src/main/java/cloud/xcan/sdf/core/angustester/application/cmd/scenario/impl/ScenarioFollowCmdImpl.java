package cloud.xcan.sdf.core.angustester.application.cmd.scenario.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SCE_FOLLOW_REPEATED;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.scenario.ScenarioFollowCmd;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.follow.ScenarioFollow;
import cloud.xcan.sdf.core.angustester.domain.scenario.follow.ScenarioFollowRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ScenarioFollowCmdImpl extends CommCmd<ScenarioFollow, Long> implements
    ScenarioFollowCmd {

  @Resource
  private ScenarioFollowRepo scenarioFollowRepo;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(ScenarioFollow follow) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Scenario scenarioDb = null;

      @Override
      protected void checkParams() {
        // Check the scenario exists
        scenarioDb = scenarioQuery.checkAndFind0(follow.getScenarioId());
        // Check the permission to view scenario
        scenarioAuthQuery.checkViewAuth(getUserId(), follow.getScenarioId());
        //  Check is not repeated
        if (scenarioFollowRepo.countByScenarioIdAndCreatedBy(follow.getScenarioId(),
            getUserId()) > 0) {
          throw ResourceExisted.of(SCE_FOLLOW_REPEATED);
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        follow.setProjectId(scenarioDb.getProjectId());
        IdKey<Long, Object> idKey = insert(follow);

        // Add follow scenario activity
        activityCmd.add(toActivity(SCENARIO, scenarioDb, ActivityType.FOLLOW));
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
        if (scenarioFollowRepo.deleteByScenarioIdAndCreatedBy(id, getUserId()) > 0) {
          // Add cancel follow api activity
          activityCmd.add(toActivity(SCENARIO, scenarioDb, ActivityType.FOLLOW_CANCEL));
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
          scenarioFollowRepo.deleteByCreatedBy(getUserId());
        } else {
          scenarioFollowRepo.deleteByProjectIdAndCreatedBy(projectId, getUserId());
        }
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<ScenarioFollow, Long> getRepository() {
    return this.scenarioFollowRepo;
  }
}




