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

import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.config.IndicatorPerfCmd;
import cloud.xcan.angus.core.tester.application.cmd.config.IndicatorStabilityCmd;
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
import cloud.xcan.angus.core.tester.infra.util.BIDUtils;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils.BIDKey;
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

/**
 * Command implementation for scenario management operations.
 * <p>
 * Provides comprehensive CRUD operations for scenarios including creation, modification, deletion,
 * cloning, moving, and import/export functionality.
 * <p>
 * Implements business logic validation, permission checks, activity logging, and transaction
 * management for all scenario operations.
 * <p>
 * Supports script management, authorization setup, indicator tracking, and comprehensive activity
 * tracking.
 */
@Service
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

  /**
   * Adds a new scenario to the system.
   * <p>
   * Performs comprehensive validation including project membership, quota limits, name uniqueness,
   * and script validation.
   * <p>
   * Creates associated script if provided and initializes creator authorization.
   * <p>
   * Logs creation activity and establishes proper scenario setup.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Scenario scenario) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Ensure user is a member of the project
        projectMemberQuery.checkMember(getUserId(), scenario.getProjectId());

        // Check if scenario creation exceeds quota limits
        scenarioQuery.checkQuota(1);

        // Validate scenario name is unique within the project
        scenarioQuery.checkNameExists(scenario.getProjectId(), scenario.getName());

        // TODO: Check if target is purchased and valid
      }

      @SneakyThrows
      @Override
      protected IdKey<Long, Object> process() {
        // Generate scenario ID if not provided
        if (isNull(scenario.getId())) {
          scenario.setId(BIDUtils.getId(BIDKey.scenarioId));
        }

        // Create associated script if provided
        IdKey<Long, Object> scriptIdKey = null;
        if (nonNull(scenario.getAngusScript())) {
          PrincipalContext.addExtension("scenario", scenario);
          scriptIdKey = scriptCmd.addByScenario(toAngusScenarioAddScript(scenario.getId(),
                  scenario.getProjectId(), scenario.getAuth(), scenario.getAngusScript(),
                  AngusParser.YAML_MAPPER.writeValueAsString(scenario.getAngusScript())),
              scenario.getAngusScript());
        }

        // Set script type from Angus script if not specified
        if (isNull(scenario.getScriptType()) && nonNull(scenario.getAngusScript())) {
          scenario.setScriptType(scenario.getAngusScript().getType());
        }
        // Set platform type from Angus script if not specified
        if (isNull(scenario.getPlatform()) && nonNull(scenario.getAngusScript())) {
          scenario.setPlatform(scenario.getAngusScript().getPlatform());
        }

        // Update scenario with script ID and authorization settings
        scenario.setScriptId(nonNull(scriptIdKey) ? scriptIdKey.getId() : scenario.getScriptId());
        scenario.setAuth(nullSafe(scenario.getAuth(), false));
        IdKey<Long, Object> idKey = insert(scenario, "name");

        // Initialize creator authorization for the scenario
        scenarioAuthCmd.addCreatorAuth(Set.of(getUserId()), scenario.getId());

        // Log scenario creation activity for audit
        activityCmd.add(toActivity(SCENARIO, scenario, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Updates an existing scenario in the system.
   * <p>
   * Validates scenario existence, user permissions, name uniqueness, and script changes before
   * updating scenario details.
   * <p>
   * Updates associated script if provided and logs modification activity.
   * <p>
   * Sends modification notification events for real-time updates.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Scenario scenario) {
    new BizTemplate<Void>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Ensure the scenario exists in database
        scenarioDb = scenarioQuery.checkAndFind(scenario.getId());

        // Check user permission to modify the scenario
        scenarioAuthQuery.checkModifyAuth(getUserId(), scenario.getId());

        // Validate scenario name is unique if changed
        if (StringUtils.isNotBlank(scenario.getName())) {
          scenarioQuery.checkUpdateNameExists(scenarioDb.getId(), scenario.getName(),
              scenarioDb.getProjectId());
        }

        // TODO: Check if target is purchased and valid
      }

      @Override
      protected Void process() {
        // Update associated script if provided
        if (nonNull(scenario.getAngusScript())) {
          ScriptInfo scriptInfo = scenarioQuery.checkAndFindScenarioScriptInfo(scenario.getId());
          scriptCmd.angusReplace(scriptInfo.getId(), scenario.getAngusScript(), true);
          scenarioDb.setScriptType(scenario.getAngusScript().getType());
          scenarioDb.setPlatform(scenario.getAngusScript().getPlatform());
        }

        // Update scenario information in database
        updateOrNotFound0(CoreUtils.copyPropertiesIgnoreNull(scenario, scenarioDb));

        // Log scenario update activity for audit
        Activity activity = toActivity(SCENARIO, scenarioDb, ActivityType.UPDATED);
        activityCmd.add(activity);

        // Send modification notification event
        scenarioQuery.assembleAndSendModifyNoticeEvent(scenarioDb, activity);
        return null;
      }
    }.execute();
  }

  /**
   * Replaces (adds or updates) a scenario in the system.
   * <p>
   * Validates scenario existence, user permissions, name uniqueness, and script changes before
   * replacing scenario details.
   * <p>
   * Creates a new scenario if ID is null, otherwise updates existing scenario.
   * <p>
   * Updates associated script and sends modification notification events.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Scenario scenario) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Scenario scenarioDb;
      //ScenarioDir scenarioDirDb;

      @Override
      protected void checkParams() {
        // Check if scenario exists in database
        if (nonNull(scenario.getId())) {
          scenarioDb = scenarioRepo.findById(scenario.getId()).orElse(null);

          // Check user permission to modify the scenario
          scenarioAuthQuery.checkModifyAuth(getUserId(), scenario.getId());

          // Validate scenario name is unique if changed
          scenarioQuery.checkUpdateNameExists(scenarioDb.getId(), scenario.getName(),
              scenarioDb.getProjectId());

          // TODO: Check if target is purchased and valid
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Create new scenario if ID is null
        if (isNull(scenarioDb)) {
          return add(scenario);
        }

        // Update scenario with retention information
        ScenarioConverter.setReplaceRetentionInfo(scenario, scenarioDb);
        scenarioRepo.save(scenario);

        // Update associated script
        ScriptInfo scriptInfo = scenarioQuery.checkAndFindScenarioScriptInfo(scenario.getId());
        scriptCmd.angusReplace(scriptInfo.getId(), scenario.getAngusScript(), true);

        // Log scenario update activity for audit
        Activity activity = toActivity(SCENARIO, scenario, ActivityType.UPDATED);
        activityCmd.add(activity);

        // Send modification notification event
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

  /**
   * Clones a scenario and its associated script.
   * <p>
   * Validates scenario existence and user permissions before creating a deep copy.
   * <p>
   * Creates a new scenario with a unique name and clones the associated script.
   * <p>
   * Logs clone activity and establishes proper scenario setup.
   */
  @Override
  public IdKey<Long, Object> clone(Long id) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Scenario scenarioDb = null;

      @Override
      protected void checkParams() {
        // Get scenario details and ensure it exists
        scenarioDb = scenarioQuery.detail(id);

        // Check user permission to view the scenario
        scenarioAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Create a deep copy of the scenario with a new name
        Scenario scenario = ScenarioConverter.toCloneScenario(this.scenarioDb);
        scenario.setId(BIDUtils.getId(BIDKey.scriptId));
        scenarioQuery.setSafeCloneName(scenario);

        // Clone and save the associated script
        IdKey<Long, Object> scriptIdKey = scriptCmd.cloneByScenario(id, scenario.getId());

        // Save the cloned scenario with the new script ID
        scenario.setScriptId(scriptIdKey.getId());
        IdKey<Long, Object> idKey = add(scenario);

        // Log scenario clone activity for audit
        activityCmd.add(toActivity(SCENARIO, scenarioDb, ActivityType.CLONE, scenarioDb.getName()));
        return idKey;
      }
    }.execute();
  }

  /**
   * Imports example scenarios for a project from template files.
   * <p>
   * Parses example script files from resources and creates scenarios for each template.
   * <p>
   * Validates script content and creates scenarios with proper project association.
   * <p>
   * Returns list of created scenario IDs for further processing.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = new ArrayList<>();

        // Process each example script file
        for (String scriptFile : SAMPLE_SCRIPT_FILES) {
          // Read script content from resource file
          String content = readExampleScriptContent(this.getClass(), scriptFile);

          // Parse and validate the script content
          AngusScript angusScript = scriptQuery.checkAndParse(content, true);

          // Convert to domain object and create scenario
          Scenario scenario = importExampleToDomain(projectId, angusScript);
          idKeys.add(add(scenario));
        }
        return idKeys;
      }
    }.execute();
  }

  /**
   * Deletes a scenario from the system (soft delete).
   * <p>
   * Validates scenario existence and user permissions before marking the scenario as deleted.
   * <p>
   * Moves scenario to trash for potential recovery and logs deletion activity.
   * <p>
   * Sends modification notification events for real-time updates.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Optional<Scenario> scenarioDb;

      @Override
      protected void checkParams() {
        // Check if scenario exists in database
        scenarioDb = scenarioRepo.find0ById(id);
        if (scenarioDb.isEmpty()) {
          return;
        }

        // Check user permission to delete the scenario
        scenarioAuthQuery.checkDeleteAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        if (scenarioDb.isEmpty()) {
          return null;
        }

        // Mark scenario as deleted (soft delete)
        scenarioRepo.updateDeleteStatus(id, true, getUserId(), LocalDateTime.now());

        // Move scenario to trash for potential recovery
        trashScenarioCmd.add0(singletonList(toScenarioTrash(scenarioDb.get())));

        // Log scenario deletion activity for audit
        Activity activity = toActivity(SCENARIO, scenarioDb.get(), ActivityType.DELETED);
        activityCmd.add(activity);

        // Send modification notification event
        scenarioQuery.assembleAndSendModifyNoticeEvent(scenarioDb.get(), activity);
        return null;
      }
    }.execute();
  }

  /**
   * Permanently deletes scenarios and all related data from the system.
   * <p>
   * Removes scenarios, scripts, authorizations, favorites, follows, indicators, and
   * variable/dataset associations.
   * <p>
   * This operation is irreversible and should be used with extreme caution.
   * <p>
   * External use must ensure data security and proper authorization.
   */
  @Override
  public void delete0(List<Long> ids) {
    // Permanently delete all scenarios
    scenarioRepo.deleteAllByIdIn(ids);

    // Permanently delete all associated scripts
    scriptCmd.deleteBySource(ScriptSource.SCENARIO, ids);

    // Permanently delete all scenario authorizations
    scenarioAuthRepo.deleteByScenarioIdIn(ids);

    // Permanently delete all scenario favorites
    scenarioFavoriteRepo.deleteByScenarioIdIn(ids);

    // Permanently delete all scenario follows
    scenarioFollowRepo.deleteByScenarioIdIn(ids);

    // Permanently delete all performance indicators
    indicatorPerfCmd.deleteAllByTarget(ids, SCENARIO);

    // Permanently delete all stability indicators
    indicatorStabilityCmd.deleteAllByTarget(ids, SCENARIO);

    // Permanently delete all variable associations
    variableTargetRepo.deleteByTarget(ids, API.getValue());

    // Permanently delete all dataset associations
    datasetTargetRepo.deleteByTarget(ids, API.getValue());

    // Note: Associated test tasks are not deleted to preserve test history
  }

  /**
   * Gets the repository for scenario entities.
   * <p>
   * Used by the base command class for generic operations.
   * <p>
   * Provides access to the underlying scenario data store.
   */
  @Override
  protected BaseRepository<Scenario, Long> getRepository() {
    return this.scenarioRepo;
  }
}
