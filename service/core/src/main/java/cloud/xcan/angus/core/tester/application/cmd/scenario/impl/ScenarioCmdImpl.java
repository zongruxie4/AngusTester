package cloud.xcan.angus.core.tester.application.cmd.scenario.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_SCRIPT_FILES;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioConverter.importExampleToDomain;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioConverter.toScenarioTrash;
import static cloud.xcan.angus.core.tester.application.converter.ScriptConverter.toAngusScenarioAddScript;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.readExampleScriptContent;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorPerfCmd;
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorStabilityCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioAuthCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioTrashCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.converter.ScenarioConverter;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetTargetRepo;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableTargetRepo;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuthRepo;
import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavouriteRepo;
import cloud.xcan.angus.core.tester.domain.scenario.follow.ScenarioFollowRepo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.parser.AngusParser;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ScenarioCmdImpl extends CommCmd<Scenario, Long> implements ScenarioCmd {

  @Resource
  private ScenarioRepo scenarioRepo;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;

  @Resource
  private ScenarioAuthCmd scenarioAuthCmd;

  @Resource
  private ScenarioAuthRepo scenarioAuthRepo;

  @Resource
  private ScenarioFavouriteRepo scenarioFavoriteRepo;

  @Resource
  private ScenarioFollowRepo scenarioFollowRepo;

  @Resource
  private ScriptQuery scriptQuery;

  @Resource
  private ScriptCmd scriptCmd;

  @Resource
  private IndicatorPerfCmd indicatorPerfCmd;

  @Resource
  private IndicatorStabilityCmd indicatorStabilityCmd;

  @Resource
  private VariableTargetRepo variableTargetRepo;

  @Resource
  private DatasetTargetRepo datasetTargetRepo;

  @Resource
  private ScenarioTrashCmd trashScenarioCmd;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Scenario scenario) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the project member
        projectMemberQuery.checkMember(getUserId(), scenario.getProjectId());

        // Check the scenario quota
        scenarioQuery.checkQuota(1);

        // Check the repeated name
        scenarioQuery.checkNameExists(scenario.getProjectId(), scenario.getName());

        // Check the target is purchased and valid TODO
      }

      @SneakyThrows
      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(scenario.getId())){
          scenario.setId(uidGenerator.getUID());
        }

        IdKey<Long, Object> scriptIdKey = null;
        if (nonNull(scenario.getAngusScript())) {
          PrincipalContext.addExtension("scenario", scenario);
          scriptIdKey = scriptCmd.addByScenario(toAngusScenarioAddScript(scenario.getId(),
                  scenario.getProjectId(), scenario.getAuth(), scenario.getAngusScript(),
                  AngusParser.YAML_MAPPER.writeValueAsString(scenario.getAngusScript())),
              scenario.getAngusScript());
        }

        // Save scenario
        if (isNull(scenario.getScriptType()) && nonNull(scenario.getAngusScript())) {
          scenario.setScriptType(scenario.getAngusScript().getType());
        }

        scenario.setScriptId(nonNull(scriptIdKey) ? scriptIdKey.getId() : scenario.getScriptId());
        scenario.setAuth(nullSafe(scenario.getAuth(), false));
        IdKey<Long, Object> idKey = insert(scenario, "name");

        // Init creator auth
        scenarioAuthCmd.addCreatorAuth(Set.of(getUserId()), scenario.getId());

        // Add delete scenario activity
        activityCmd.add(toActivity(SCENARIO, scenario, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Scenario scenario) {
    new BizTemplate<Void>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check and find scenario
        scenarioDb = scenarioQuery.checkAndFind(scenario.getId());

        // Check the scenario permission
        scenarioAuthQuery.checkModifyAuth(getUserId(), scenario.getId());

        // Check the repeated name
        if (StringUtils.isNotBlank(scenario.getName())) {
          scenarioQuery.checkUpdateNameExists(scenarioDb.getId(), scenario.getName(),
              scenarioDb.getProjectId());
        }

        // Check the target is purchased and valid TODO
      }

      @Override
      protected Void process() {
        if (nonNull(scenario.getAngusScript())) {
          ScriptInfo scriptInfo = scenarioQuery.checkAndFindScenarioScriptInfo(scenario.getId());
          scriptCmd.angusReplace(scriptInfo.getId(), scenario.getAngusScript(), true);
          scenarioDb.setScriptType(scenario.getAngusScript().getType());
        }

        // Update scenario
        updateOrNotFound0(CoreUtils.copyPropertiesIgnoreNull(scenario, scenarioDb));

        // Add updated scenario activity
        Activity activity = toActivity(SCENARIO, scenarioDb, ActivityType.UPDATED);
        activityCmd.add(activity);

        // Add modification event
        scenarioQuery.assembleAndSendModifyNoticeEvent(scenarioDb, activity);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Scenario scenario) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Scenario scenarioDb;
      //ScenarioDir scenarioDirDb;

      @Override
      protected void checkParams() {
        // Check and find scenario
        if (nonNull(scenario.getId())) {
          scenarioDb = scenarioRepo.findById(scenario.getId()).orElse(null);

          // Check the scenario permission
          scenarioAuthQuery.checkModifyAuth(getUserId(), scenario.getId());

          // Check the repeated name
          scenarioQuery.checkUpdateNameExists(scenarioDb.getId(), scenario.getName(),
              scenarioDb.getProjectId());

          // Check and find project
          // scenarioDirDb = scenarioDirQuery.checkAndFind(scenarioDb.getProjectId());
        }

        // Check the target is purchased and valid TODO
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(scenarioDb)) {
          return add(scenario);
        }

        ScenarioConverter.setReplaceRetentionInfo(scenario, scenarioDb);
        scenarioRepo.save(scenario);

        // Replace the script of scenario
        ScriptInfo scriptInfo = scenarioQuery.checkAndFindScenarioScriptInfo(scenario.getId());
        scriptCmd.angusReplace(scriptInfo.getId(), scenario.getAngusScript(), true);

        // Add updated scenario activity
        Activity activity = toActivity(SCENARIO, scenario, ActivityType.UPDATED);
        activityCmd.add(activity);

        // Add modification event
        scenarioQuery.assembleAndSendModifyNoticeEvent(scenarioDb, activity);

        return new IdKey<Long, Object>().setId(scenarioDb.getId()).setKey(scenarioDb.getName());
      }
    }.execute();
  }


  @Transactional(rollbackFor = Exception.class)
  @Override
  public void move(Long scenarioId, Long targetProjectId) {
    /*new BizTemplate<Void>() {
      Project projectDb;
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check the scenario exists
        scenarioDb = scenarioQuery.checkAndFind0(scenarioId);

        // Check the dir exists
        projectDb = projectQuery.checkAndFind(targetProjectId);

        // Check the to have permission to modify the scenario
        scenarioAuthQuery.checkModifyAuth(getUserId(), scenarioId);

        // Check the to have permission to add dir
        projectAuthQuery.checkAddAuth(getUserId(), targetProjectId);

        // Check the number of scenes in the dir
        scenarioQuery.checkDirScenarioQuota(targetProjectId, 1);

        // NOOP: Allow repeated names after move
      }

      @Override
      protected Void process() {
        // Grant the scenario creator permission to the target project creator
        Set<Long> creatorIds = getCreatorIds(null, targetProjectId);
        scenarioAuthCmd.moveCreatorAuth(creatorIds, scenarioId);

        // Modify scenario dirId
        scenarioRepo.updateProjectById(scenarioId, targetProjectId);

        // Modify script target dirId
        scriptTargetCmd.move0(List.of(scenarioId), ScriptSource.SCENARIO, targetProjectId);

        // NOOP: Init scenario creator to view parent dir permissions by WEB-UI

        // Add move scenario activity
        activityCmd.add(ActivityConverter.toActivity(CombinedTargetType.SCENARIO, scenarioDb,
            ActivityType.MOVED_TO, projectDb.getName()));
        return null;
      }
    }.execute();*/
  }

  @Override
  public IdKey<Long, Object> clone(Long id) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Scenario scenarioDb = null;

      @Override
      protected void checkParams() {
        scenarioDb = scenarioQuery.detail(id);
        scenarioAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Clone scenario
        Scenario scenario = ScenarioConverter.toCloneScenario(this.scenarioDb);
        scenarioQuery.setSafeCloneName(scenario);

        // Clone and save scenario script
        IdKey<Long, Object> scriptIdKey = scriptCmd.cloneByScenario(id, scenario.getId());

        // Save scenario
        scenario.setScriptId(scriptIdKey.getId());
        IdKey<Long, Object> idKey = add(scenario);

        // Add clone scenario activity
        activityCmd.add(toActivity(SCENARIO, scenarioDb, ActivityType.CLONE, scenarioDb.getName()));
        return idKey;
      }
    }.execute();
  }

  /**
   * Note: When API calls that are not user-action, tenant and user information must be injected
   * into the PrincipalContext.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = new ArrayList<>();
        for (String scriptFile : SAMPLE_SCRIPT_FILES) {
          String content = readExampleScriptContent(this.getClass(), scriptFile);
          AngusScript angusScript = scriptQuery.checkAndParse(content, true);
          Scenario scenario = importExampleToDomain(projectId, angusScript);
          idKeys.add(add(scenario));
        }
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Optional<Scenario> scenarioDb;

      @Override
      protected void checkParams() {
        // Check the scenario existed and authed
        scenarioDb = scenarioRepo.find0ById(id);
        if (scenarioDb.isEmpty()) {
          return;
        }
        scenarioAuthQuery.checkDeleteAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        if (scenarioDb.isEmpty()) {
          return null;
        }

        // Delete scenario
        scenarioRepo.updateDeleteStatus(id, true, getUserId(), LocalDateTime.now());

        // Add scenario to ApisTrash
        trashScenarioCmd.add0(singletonList(toScenarioTrash(scenarioDb.get())));

        // Add delete scenario activity
        Activity activity = toActivity(SCENARIO, scenarioDb.get(), ActivityType.DELETED);
        activityCmd.add(activity);

        // Add modification event
        scenarioQuery.assembleAndSendModifyNoticeEvent(scenarioDb.get(), activity);
        return null;
      }
    }.execute();
  }

  /**
   * Physically delete, External calling biz must ensure data authed and secured!
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0(List<Long> ids) {
    // Delete scenario
    scenarioRepo.deleteAllByIdIn(ids);
    // Delete scenario script
    scriptCmd.deleteBySource(ScriptSource.SCENARIO, ids);
    // Delete scenario auth
    scenarioAuthRepo.deleteByScenarioIdIn(ids);
    // Delete scenario favorite
    scenarioFavoriteRepo.deleteByScenarioIdIn(ids);
    // Delete scenario follow
    scenarioFollowRepo.deleteByScenarioIdIn(ids);
    // Delete scenario indicator
    indicatorPerfCmd.deleteAllByTarget(ids, SCENARIO);
    indicatorStabilityCmd.deleteAllByTarget(ids, SCENARIO);
    // Delete scenario variable association
    variableTargetRepo.deleteByTarget(ids, API.getValue());
    // Delete scenario dataset association
    datasetTargetRepo.deleteByTarget(ids, API.getValue());
    // Do not delete associated test tasks
    // scenarioTestCmd.delete0(ids);
  }

  @Override
  protected BaseRepository<Scenario, Long> getRepository() {
    return this.scenarioRepo;
  }
}
