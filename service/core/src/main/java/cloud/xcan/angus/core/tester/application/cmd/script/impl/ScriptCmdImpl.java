package cloud.xcan.angus.core.tester.application.cmd.script.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API_CASE;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SCRIPT;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_SCRIPT_FILES;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ApisToAngusModelConverter.getScriptTaskArguments;
import static cloud.xcan.angus.core.tester.application.converter.ApisToAngusModelConverter.toHttp;
import static cloud.xcan.angus.core.tester.application.converter.ScriptConverter.importDtoToDomain;
import static cloud.xcan.angus.core.tester.application.converter.ScriptConverter.setReplaceInfo;
import static cloud.xcan.angus.core.tester.application.converter.ScriptConverter.toAngusAddScript;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCRIPT_CONTENT_PARSE_ERROR;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.DELETED;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.readExampleScriptContent;
import static cloud.xcan.angus.core.utils.AngusUtils.overrideExecServerParameter;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.angus.model.script.configuration.ScriptType.TEST_FUNCTIONALITY;
import static cloud.xcan.angus.spec.experimental.BizConstant.ANGUS_SCRIPT_LENGTH;
import static cloud.xcan.angus.spec.experimental.StandardCharsets.UTF_8;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isBlank;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.StreamUtils.copyToString;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptAuthCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptTagCmd;
import cloud.xcan.angus.core.tester.application.converter.ApisToAngusModelConverter;
import cloud.xcan.angus.core.tester.application.converter.DatasetConverter;
import cloud.xcan.angus.core.tester.application.converter.ScriptConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisCaseQuery;
import cloud.xcan.angus.core.tester.application.query.data.DatasetTargetQuery;
import cloud.xcan.angus.core.tester.application.query.data.VariableTargetQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptAuthQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.script.ScriptRepo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils.BIDKey;
import cloud.xcan.angus.model.element.http.Http;
import cloud.xcan.angus.model.element.type.TestTargetType;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.Threads;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import cloud.xcan.angus.model.script.pipeline.Task;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of script command operations for comprehensive script management.
 *
 * <p>This class provides extensive functionality for managing test scripts, including
 * creation, modification, cloning, import/export, and synchronization with API cases.</p>
 *
 * <p>It handles the complete lifecycle of scripts from creation to deletion, including
 * authorization management, tag management, and activity logging for audit purposes.</p>
 *
 * <p>The implementation supports various script sources (API, Scenario, Service) and
 * integrates with the Angus testing framework for script validation and execution.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Script CRUD operations with comprehensive validation</li>
 *   <li>Script synchronization with API cases and services</li>
 *   <li>Import/export functionality with example script templates</li>
 *   <li>Creator authorization and tag management</li>
 *   <li>Activity logging for audit trails</li>
 * </ul></p>
 */
@Slf4j
@Biz
public class ScriptCmdImpl extends CommCmd<Script, Long> implements ScriptCmd {

  @Resource
  private ScriptRepo scriptRepo;
  @Resource
  private ScriptQuery scriptQuery;
  @Resource
  private ScriptAuthCmd scriptAuthCmd;
  @Resource
  private ScriptAuthQuery scriptAuthQuery;
  @Resource
  private ScriptTagCmd scriptTagCmd;
  @Resource
  private ApisCaseQuery apisCaseQuery;
  @Resource
  private ScenarioRepo scenarioRepo;
  @Resource
  private ScenarioQuery scenarioQuery;
  @Resource
  private VariableTargetQuery variableTargetQuery;
  @Resource
  private DatasetTargetQuery datasetTargetQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds a new script with comprehensive validation and setup.
   *
   * <p>This method performs extensive validation including project membership,
   * script quota checks, source resource validation, and script content parsing.</p>
   *
   * <p>It automatically sets up creator authorization and script tags,
   * and logs the creation activity for audit purposes.</p>
   *
   * <p>Note: Scenarios and scripts have a one-to-one relationship, while APIs
   * (different test types) and scripts have a one-to-many relationship.</p>
   *
   * @param script the script to add
   * @param saveActivity whether to save the creation activity
   * @return the ID key of the created script
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Script script, boolean saveActivity) {
    return new BizTemplate<IdKey<Long, Object>>() {
      AngusScript angusScript;

      @Override
      protected void checkParams() {
        // Verify project membership for the current user
        projectMemberQuery.checkMember(getUserId(), script.getProjectId());

        // Check script quota availability
        scriptQuery.checkQuota(1);

        // Verify the source resource exists
        scriptQuery.checkSourceResourceExist(script);

        // Validate script content length
        scriptQuery.checkScriptLength(script.getContent());

        // Parse and validate script content
        angusScript = scriptQuery.checkAndParse(script.getContent(), true);

        // Check for duplicate scripts in the same source and test type
        script.setType(nullSafe(script.getType(), angusScript.getType()));
        scriptQuery.checkSourceAddScriptExist(script);

        // TODO: Check plugin is purchased and valid

        // TODO: Check test quota is valid
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Determine and set script type based on content analysis
        judgeScriptType(script, angusScript);
        script.setPlugin(angusScript.getPlugin());

        script.setId(BIDUtils.getId(BIDKey.scriptId));
        IdKey<Long, Object> idKey = insert(script, "name");

        // Initialize creator authorization
        scriptAuthCmd.addCreatorAuth(Set.of(getUserId()), idKey.getId());

        // Add script tags
        scriptTagCmd.add(script.getId(), angusScript.getTags());

        // Log creation activity
        if (saveActivity) {
          activityCmd.add(toActivity(SCRIPT, script, ActivityType.CREATED));
        }
        return idKey;
      }
    }.execute();
  }

  /**
   * Adds a new script with pre-validated AngusScript object.
   *
   * <p>This method is used when the AngusScript object is already available and validated.
   * It provides better performance by avoiding redundant parsing operations.</p>
   *
   * <p>The method handles script serialization if content is empty and ensures
   * proper type determination and plugin assignment.</p>
   *
   * @param script the script to add
   * @param angusScript the pre-validated AngusScript object
   * @param validateScript whether to perform additional script validation
   * @return the ID key of the created script
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Script script, AngusScript angusScript, boolean validateScript) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Verify project membership for the current user
        projectMemberQuery.checkMember(getUserId(), script.getProjectId());

        // Check script quota availability
        scriptQuery.checkQuota(1);

        // Verify the source resource exists
        scriptQuery.checkSourceResourceExist(script);

        // Check for duplicate scripts in the same source and test type
        script.setType(nullSafe(script.getType(), angusScript.getType()));
        scriptQuery.checkSourceAddScriptExist(script);

        // Serialize script content if empty
        if (isEmpty(script.getContent())) {
          script.setContent(scriptQuery.checkAndSerialize(angusScript, validateScript));
        }

        // TODO: Check plugin is purchased and valid

        // TODO: Check test quota is valid
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Determine and set script type based on content analysis
        judgeScriptType(script, angusScript);
        script.setPlugin(angusScript.getPlugin());

        // Generate new ID to avoid reflection conflicts
        script.setId(BIDUtils.getId(BIDKey.scriptId));
        IdKey<Long, Object> idKey = insert(script, "name");

        // Initialize creator authorization
        scriptAuthCmd.addCreatorAuth(Set.of(getUserId()), idKey.getId());

        // Add script tags
        scriptTagCmd.add(script.getId(), angusScript.getTags());

        // Log creation activity
        activityCmd.add(toActivity(SCRIPT, script, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Adds a script by scenario with minimal validation.
   *
   * <p>This method is specifically designed for scenario-based script creation
   * with reduced validation requirements. It's typically used during scenario
   * setup and initialization.</p>
   *
   * @param script the script to add
   * @param angusScript the pre-validated AngusScript object
   * @return the ID key of the created script
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  public IdKey<Long, Object> addByScenario(Script script, AngusScript angusScript) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check for duplicate scripts in the same source and test type
        script.setType(nullSafe(script.getType(), angusScript.getType()));
        scriptQuery.checkSourceAddScriptExist(script);

        // Serialize script content if empty
        if (isEmpty(script.getContent())) {
          script.setContent(scriptQuery.checkAndSerialize(angusScript, true));
        }

        // TODO: Check plugin is purchased and valid

        // TODO: Check test quota is valid
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Determine and set script type based on content analysis
        judgeScriptType(script, angusScript);
        script.setPlugin(angusScript.getPlugin());

        // Generate new ID to avoid reflection conflicts
        script.setId(BIDUtils.getId(BIDKey.scriptId));
        IdKey<Long, Object> idKey = insert(script, "name");

        // Initialize creator authorization
        scriptAuthCmd.addCreatorAuth(Set.of(getUserId()), idKey.getId());

        // Add script tags
        scriptTagCmd.add(script.getId(), angusScript.getTags());

        // Log creation activity
        activityCmd.add(toActivity(SCRIPT, script, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Adds a script from AngusScript object with project context.
   *
   * <p>This method creates a script from an AngusScript object, providing
   * a convenient way to add scripts with minimal setup requirements.</p>
   *
   * @param projectId the project ID to associate the script with
   * @param angusScript the AngusScript object to convert
   * @param validateScript whether to perform script validation
   * @return the ID key of the created script
   * @throws IllegalArgumentException if projectId is null or validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> addByAngus(Long projectId, AngusScript angusScript,
      boolean validateScript) {
    return new BizTemplate<IdKey<Long, Object>>() {
      String content;

      @Override
      protected void checkParams() {
        assertNotNull(projectId, "Parameter projectId is null");

        // Serialize and validate script content
        content = scriptQuery.checkAndSerialize(angusScript, true);
      }

      @Override
      protected IdKey<Long, Object> process() {
        return add(toAngusAddScript(projectId, angusScript, content), angusScript, validateScript);
      }
    }.execute();
  }

  /**
   * Updates an existing script with comprehensive validation.
   *
   * <p>This method performs extensive validation including script existence,
   * modification permissions, source resource validation, and content parsing.</p>
   *
   * <p>It preserves existing script properties while allowing updates to
   * type, name, and description fields for flexibility.</p>
   *
   * @param script the script with updated properties
   * @throws IllegalArgumentException if validation fails or script not found
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Script script) {
    new BizTemplate<Void>() {
      Script scriptDb;
      AngusScript angusScript;

      @Override
      protected void checkParams() {
        // Verify script exists and retrieve it
        scriptDb = scriptQuery.checkAndFind(script.getId());

        // Verify user has modification permissions
        scriptAuthQuery.checkModifyAuth(getUserId(), scriptDb.getId());

        // Verify the source resource exists
        scriptQuery.checkSourceResourceExist(script);

        // Check for duplicate scripts in the same source and test type
        scriptQuery.checkSourceUpdateScriptExist(script);

        // Validate script content length
        scriptQuery.checkScriptLength(script.getContent());

        // Parse and validate script content
        angusScript = scriptQuery.checkAndParse(script.getContent(), true);

        // TODO: Check plugin is purchased and valid

        // TODO: Check test quota is valid
      }

      @Override
      protected Void process() {
        // Update script properties while preserving existing data
        if (nonNull(angusScript)) {
          script.setPlugin(angusScript.getPlugin());
        }

        scriptRepo.save(copyPropertiesIgnoreNull(script, scriptDb));

        // Log update activity
        activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Script script, AngusScript angusScript, boolean validateScript) {
    new BizTemplate<Void>() {
      Script scriptDb;

      @Override
      protected void checkParams() {
        // Check and find script
        scriptDb = scriptQuery.checkAndFind(script.getId());

        // Check the script permission
        scriptAuthQuery.checkModifyAuth(getUserId(), scriptDb.getId());

        // Check the source exists
        scriptQuery.checkSourceResourceExist(script);

        // Check the resource script exists,
        // Apis and scenarios There can be only one script for each test type
        scriptQuery.checkSourceUpdateScriptExist(script);

        // Check and serialize script
        if (isEmpty(script.getContent())) {
          script.setContent(scriptQuery.checkAndSerialize(angusScript, true));
        }

        // TODO Check plugin is purchased and valid

        // TODO Check test quota is valid
      }

      @Override
      protected Void process() {
        // NOOP: Overwrite script type, name and description -> Allowing inconsistencies.

        if (nonNull(angusScript)) {
          script.setPlugin(angusScript.getPlugin());
        }

        judgeScriptType(scriptDb, angusScript);
        scriptRepo.save(copyPropertiesIgnoreNull(script, scriptDb));

        // Replace scripts tags
        scriptTagCmd.replace(script.getId(), angusScript.getTags());

        // Save activity
        activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Updates a script with pre-validated AngusScript object and optional tag replacement.
   *
   * <p>This method is used for internal script updates where the AngusScript object
   * is already validated. It provides better performance by avoiding redundant parsing.</p>
   *
   * <p>The method allows selective tag replacement and activity logging based on
   * the provided parameters.</p>
   *
   * @param scriptDb the script to update
   * @param angusScript the pre-validated AngusScript object
   * @param replaceTag whether to replace existing tags
   * @param validateScript whether to perform script validation
   */
  @Override
  public void update0(Script scriptDb, AngusScript angusScript, boolean replaceTag,
      boolean validateScript) {
    // Serialize script content with validation
    scriptDb.setContent(scriptQuery.checkAndSerialize(angusScript, validateScript));

    // Update script properties while preserving existing data
    if (nonNull(angusScript)) {
      scriptDb.setPlugin(angusScript.getPlugin());
      judgeScriptType(scriptDb, angusScript);
    }

    scriptRepo.save(scriptDb);

    // Replace script tags if requested
    if (replaceTag && nonNull(angusScript)) {
      scriptTagCmd.replace(scriptDb.getId(), angusScript.getTags());
    }

    // Log update activity
    activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.UPDATED));
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update0(Script scriptDb, AngusScript angusScript) {
    // Check and serialize script
    scriptDb.setContent(scriptQuery.checkAndSerialize(angusScript, false));

    // NOOP: Overwrite script type, name and description -> Allowing inconsistencies.

    if (nonNull(angusScript)) {
      scriptDb.setPlugin(angusScript.getPlugin());
    }

    judgeScriptType(scriptDb, angusScript);
    scriptRepo.save(scriptDb);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Script script) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Script scriptDb;
      AngusScript angusScript;

      @Override
      protected void checkParams() {
        if (nonNull(script.getId())) {
          // Check and find script
          scriptDb = scriptQuery.checkAndFind(script.getId());

          // Check the script permission
          scriptAuthQuery.checkModifyAuth(getUserId(), scriptDb.getId());

          // Check the source exists
          script.setSource(scriptDb.getSource());
          script.setSourceId(scriptDb.getSourceId());
          scriptQuery.checkSourceResourceExist(script);

          // Check the resource script exists,
          // Apis and scenarios There can be only one script for each test type
          scriptQuery.checkSourceUpdateScriptExist(script);

          // Check and parse script
          angusScript = scriptQuery.checkAndParse(script.getContent(), true);

          // TODO Check plugin is purchased and valid

          // TODO Check test quota is valid
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(script.getId())) {
          return add(script, angusScript, false);
        }

        // NOOP: Overwrite script type, name and description -> Allowing inconsistencies.

        setReplaceInfo(scriptDb, script, angusScript);
        judgeScriptType(scriptDb, angusScript);
        scriptRepo.save(scriptDb);

        // Replace scripts tags
        scriptTagCmd.replace(script.getId(), angusScript.getTags());

        // Save activity
        activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.UPDATED));
        return new IdKey<Long, Object>().setId(scriptDb.getId()).setKey(scriptDb.getName());
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void angusReplace(Long id, AngusScript angusScript, boolean validateScript) {
    new BizTemplate<Void>() {
      String content;
      Script scriptDb;

      @SneakyThrows
      @Override
      protected void checkParams() {
        // Check and find script
        scriptDb = scriptQuery.checkAndFind(id);
        // Check the script permission
        scriptAuthQuery.checkModifyAuth(getUserId(), scriptDb.getId());
        // Check and serialize script
        content = scriptQuery.checkAndSerialize(angusScript, validateScript);

        // TODO Check plugin is purchased and valid

        // TODO Check test quota is valid
      }

      @Override
      protected Void process() {
        setReplaceInfo(scriptDb, angusScript, content);
        scriptRepo.save(scriptDb);

        // Replace scripts tags
        scriptTagCmd.replace(id, angusScript.getTags());

        // Save activity
        activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> clone(Long id) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Script scriptDb;

      @Override
      protected void checkParams() {
        // Check and find script
        scriptDb = scriptQuery.checkAndFind(id);
        // Check the script permission
        scriptAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected IdKey<Long, Object> process() {
        Script script = ScriptConverter.toClonedScript(scriptDb);
        script.setId(BIDUtils.getId(BIDKey.scriptId));
        IdKey<Long, Object> idKey = insert(script, "name");

        // Init creator auth
        scriptAuthCmd.addCreatorAuth(Set.of(getUserId()), idKey.getId());

        // Clone scripts tags
        scriptTagCmd.clone(id, idKey.getId());

        // Save activity
        activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.CLONE));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> cloneByScenario(Long id, Long newId) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Script scriptDb;

      @Override
      protected void checkParams() {
        scriptDb = scenarioQuery.checkAndFindScenarioScript(id);

        assertTrue(scriptDb.getSource().isUnique(),
            "The associated resource script does not allow cloning");
      }

      @Override
      protected IdKey<Long, Object> process() {
        Script script = ScriptConverter.toClonedScenarioScript(scriptDb, newId);
        IdKey<Long, Object> idKey = insert(script, "name");

        // Clone scripts tags
        scriptTagCmd.clone(id, idKey.getId());

        // Save activity
        activityCmd.add(toActivity(SCRIPT, script, ActivityType.CLONE));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> imports(Script script) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        assertTrue(isNotBlank(script.getContent()) || nonNull(script.getFile()),
            "Importing content and files must specify one of them");
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isBlank(script.getContent())) {
          try {
            String content = copyToString(script.getFile().getInputStream(), UTF_8);
            assertTrue(content.length() <= ANGUS_SCRIPT_LENGTH,
                "Script length exceeds the limit of " + ANGUS_SCRIPT_LENGTH);
            script.setContent(content);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        IdKey<Long, Object> idKey = add(script, false);

        // Save activity
        activityCmd.add(toActivity(SCRIPT, script, ActivityType.IMPORT));
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
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = new ArrayList<>();
        for (String scriptFile : SAMPLE_SCRIPT_FILES) {
          String content = readExampleScriptContent(this.getClass(), scriptFile);
          AngusScript angusScript = scriptQuery.checkAndParse(content, true);
          Script script = importDtoToDomain(projectId,
              angusScript.getInfo().getName(), angusScript.getInfo().getDescription(), content);
          idKeys.add(imports(script));
        }
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<ScriptInfo> scriptDb;

      @Override
      protected void checkParams() {
        // Check the script permission
        scriptAuthQuery.batchCheckPermission(ids, ScriptPermission.DELETE);
        scriptDb = scriptQuery.checkAndFindInfos(
            new ArrayList<>(ids)/*Fix:: Clear by checkAndFindInfos()*/);
      }

      @Override
      protected Void process() {
        scriptRepo.deleteByIdIn(ids);

        scriptTagCmd.deleteByScriptIdIn(ids);

        scriptAuthCmd.deleteByScriptIdIn(ids);

        scenarioRepo.deleteByScriptIdIn(ids);

        // Save activity
        activityCmd.addAll(toActivities(SCRIPT, scriptDb, DELETED, activityParams(scriptDb)));
        return null;
      }
    }.execute();
  }

  @Override
  public void deleteBySource(ScriptSource source, Collection<Long> targetIds) {
    Set<Long> scriptIds = scriptQuery.findIdsBySource(source, targetIds);
    if (isNotEmpty(scriptIds)) {
      scriptRepo.deleteByIdIn(scriptIds);
      scriptTagCmd.deleteByScriptIdIn(scriptIds);
      scriptAuthCmd.deleteByScriptIdIn(scriptIds);
    }
  }

  @Override
  public void deleteBySource(ScriptSource source, Collection<Long> targetIds,
      Collection<ScriptType> testTypes) {
    Set<Long> scriptIds = scriptQuery.findIdsBySourceAndTypeIn(source, targetIds, testTypes);
    if (isNotEmpty(scriptIds)) {
      scriptRepo.deleteByIdIn(scriptIds);
      scriptTagCmd.deleteByScriptIdIn(scriptIds);
      scriptAuthCmd.deleteByScriptIdIn(scriptIds);
    }
  }

  /**
   * Determines and sets the script type based on priority rules.
   *
   * <p>This method implements a priority-based type determination system where
   * the script's explicit type takes precedence over the parsed AngusScript type.</p>
   *
   * <p>If the script has no explicit type, it uses the type from the parsed
   * AngusScript. Otherwise, it updates the AngusScript with the script's type.</p>
   *
   * @param script the script entity to determine type for
   * @param angusScript the parsed AngusScript object
   */
  private void judgeScriptType(Script script, AngusScript angusScript) {
    if (isNull(script.getType())) {
      script.setType(angusScript.getType());
    } else {
      // Script has higher priority than AngusScript
      angusScript.setType(script.getType());
    }
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public long syncApisCaseToScript(Apis apisDb, List<ApisCase> casesDb) {
    // Replace authentication references
    for (ApisCase caseDb : casesDb) {
      Map<String, String> allRefModels = apisCaseQuery.findCaseAllRef(caseDb);
      caseDb.setResolvedRefModels(allRefModels);

      if (nonNull(caseDb.getAuthentication()) && caseDb.isAuthSchemaRef()
          && caseDb.includeSchemaRef(caseDb.getAuthentication().get$ref())) {
        apisCaseQuery.setAndGetRefAuthentication(caseDb);
      }
    }

    Script scriptDb = scriptQuery.findBySourceAndScriptType(ScriptSource.API, apisDb.getId(),
        TEST_FUNCTIONALITY);
    List<Long> caseIds = casesDb.stream().map(ApisCase::getId).toList();
    Map<Long, Long> caseApiMap = casesDb.stream()
        .collect(Collectors.toMap(ApisCase::getId, ApisCase::getApisId));
    Map<Long, List<Variable>> caseVariableMap = variableTargetQuery.findVariables(caseIds,
        API_CASE.getValue(), caseApiMap);
    Map<Long, List<Dataset>> caseDatasetMap = datasetTargetQuery.findDatasets(caseIds,
        API_CASE.getValue(), caseApiMap);
    // Generate script
    if (isNull(scriptDb)) {
      String name = String.format("%s[%s]", apisDb.getName(), TestType.FUNCTIONAL.getMessage());
      Script script = assembleFuncTestScript(name, casesDb, false, ScriptSource.API, apisDb.getId(),
          apisDb.getProjectId(), apisDb.getServiceId(), caseVariableMap, caseDatasetMap);
      return add(script, script.getAngusScript(), false).getId();
    } else {
      // Update existed script
      AngusScript angusScript = scriptQuery.checkAndParse(scriptDb.getContent(), true);
      assertNotNull(angusScript, SCRIPT_CONTENT_PARSE_ERROR);
      List<cloud.xcan.angus.model.element.variable.Variable>
          angusVariables = getAngusConfigurationVariables(caseVariableMap);
      angusScript.getConfiguration().setVariables(angusVariables);

      if (isEmpty(angusScript.getTask().getPipelines())) {
        angusScript.getTask().setPipelines(casesDb.stream()
            .map(ApisToAngusModelConverter::toHttp).toList());
      } else {
        List<TestTargetType> pipelines = new ArrayList<>();
        List<String> existedInScript = new ArrayList<>();
        // Preserve and update existing targets
        for (TestTargetType pipeline : angusScript.getTask().getPipelines()) {
          ApisCase existCases = casesDb.stream()
              .filter(x -> Objects.equals(x.getName(), pipeline.getName()))
              .findFirst().orElse(null);
          if (nonNull(existCases)) {
            existedInScript.add(existCases.getName());
            Http case0 = toHttp(existCases);
            case0.setDatasets(getAngusDataset(caseDatasetMap, existCases));
            pipelines.add(case0); // Update existing script case
          } else {
            pipelines.add(pipeline); // Preserve script case
          }
        }
        // Add new script case
        for (ApisCase aCase : casesDb) {
          if (!existedInScript.contains(aCase.getName())) {
            Http case0 = toHttp(aCase);
            case0.setDatasets(getAngusDataset(caseDatasetMap, aCase));
            pipelines.add(case0);
          }
        }
        angusScript.getTask().setPipelines(pipelines);
      }
      update0(scriptDb, angusScript, true, false);
    }
    return scriptDb.getId();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public long syncServiceCaseToScript(Services serviceDb, ScriptSource source,
      List<ApisCase> casesDb, List<Server> servers) {
    List<Long> caseIds = casesDb.stream().map(ApisCase::getId).toList();
    Map<Long, Long> caseApiMap = casesDb.stream()
        .collect(Collectors.toMap(ApisCase::getId, ApisCase::getApisId));
    Map<Long, List<Variable>> caseVariableMap = variableTargetQuery.findVariables(caseIds,
        API_CASE.getValue(), caseApiMap);
    Map<Long, List<Dataset>> caseDatasetMap = datasetTargetQuery.findDatasets(caseIds,
        API_CASE.getValue(), caseApiMap);
    Map<String, Server> serverMap = isEmpty(servers) ? Collections.emptyMap()
        : servers.stream().collect(Collectors.toMap(Server::getUrl, x -> x));
    Script scriptDb = scriptQuery.findBySourceAndScriptType(source, serviceDb.getId(),
        TEST_FUNCTIONALITY);
    if (isNull(scriptDb)) {
      String name = String.format("%s[%s]", serviceDb.getName(), source.getMessage());
      Script script = assembleFuncTestScript(name, casesDb, false, source, serviceDb.getId(),
          serviceDb.getProjectId(), serviceDb.getId(), caseVariableMap, caseDatasetMap);
      for (TestTargetType http : script.getAngusScript().getTask().getPipelines()) {
        // Override exec server configuration parameter in http
        overrideExecServerParameter(serverMap, (Http) http);
      }
      // Override exec server configuration parameter in variables
      overrideExecServerParameter(serverMap,
          script.getAngusScript().getConfiguration().getVariables());
      return add(script, script.getAngusScript(), false).getId();
    } else {
      // Replace existed case in script
      AngusScript angusScript = scriptQuery.checkAndParse(scriptDb.getContent(), false);
      angusScript.getConfiguration().setVariables(getAngusConfigurationVariables(caseVariableMap));
      // Override exec server configuration parameter in variables
      overrideExecServerParameter(serverMap, angusScript.getConfiguration().getVariables());
      List<Http> https = new ArrayList<>();
      for (ApisCase case0 : casesDb) {
        Http http = toHttp(case0);
        http.setDatasets(getAngusDataset(caseDatasetMap, case0));
        // Override exec server configuration parameter in http
        overrideExecServerParameter(serverMap, http);
        https.add(http);
      }
      angusScript.getTask().setPipelines(https);
      update0(scriptDb, angusScript, true, false);
    }
    return scriptDb.getId();
  }

  public static Script assembleFuncTestScript(String name, List<ApisCase> cases, boolean auth,
      ScriptSource source, Long sourceId, Long projectId, Long serviceId,
      Map<Long, List<Variable>> caseVariableMap, Map<Long, List<Dataset>> caseDatasetMap) {
    Arguments arguments = getScriptTaskArguments(false);

    List<Http> https = new ArrayList<>();
    for (ApisCase case0 : cases) {
      Http http = ApisToAngusModelConverter.toHttp(case0);
      http.setDatasets(getAngusDataset(caseDatasetMap, case0));
      https.add(http);
    }
    return new Script()
        .setProjectId(projectId)
        .setServiceId(serviceId)
        .setName(name)
        .setType(TEST_FUNCTIONALITY)
        .setSource(source).setSourceId(sourceId)
        .setAuth(auth)
        .setPlugin(PLUGIN_HTTP_NAME)
        .setAngusScript(AngusScript.newBuilder()
            .type(TEST_FUNCTIONALITY)
            .plugin(PLUGIN_HTTP_NAME)
            .configuration(Configuration.newBuilder()
                .iterations(1L)
                .thread(Threads.newBuilder()
                    .threads(1)
                    .build())
                .variables(getAngusConfigurationVariables(caseVariableMap))
                .build())
            .task(Task.newBuilder()
                .arguments(arguments)
                .pipelines(https)
                .build())
            .build());
  }

  @Override
  public void renameCaseToScript(Long apisId, Long caseId, String newName) {
    Script scriptDb = scriptQuery.findBySourceAndScriptType(ScriptSource.API, apisId,
        TEST_FUNCTIONALITY);
    if (isNull(scriptDb)) {
      log.warn("Apis[{}] functionality test script does not exist", apisId);
      return;
    }
    AngusScript angusScript = scriptQuery.checkAndParse(scriptDb.getContent(), true);
    if (isEmpty(angusScript.getTask().getPipelines())) {
      log.warn("Apis[{}] functionality case[{}] does not exist in script pipelines", apisId,
          caseId);
      return;
    }
    for (TestTargetType pipeline : angusScript.getTask().getPipelines()) {
      if (caseId.equals(((Http) pipeline).getCaseId())) {
        ((Http) pipeline).setName(newName);
        break;
      }
    }
    update0(scriptDb, scriptDb.getAngusScript(), false, false);
  }

  @Override
  public void deleteCaseInScript(Long apisId, Collection<Long> caseIds) {
    Script scriptDb = scriptQuery.findBySourceAndScriptType(ScriptSource.API, apisId,
        TEST_FUNCTIONALITY);
    if (isNull(scriptDb)) {
      log.warn("Apis[{}] functionality test script does not exist", apisId);
      return;
    }

    // Delete script when its content is empty
    if (isNull(scriptDb.getContent())) {
      delete(Set.of(scriptDb.getId()));
      return;
    }

    AngusScript angusScript = scriptQuery.checkAndParse(scriptDb.getContent(), true);
    if (isNull(angusScript) || isEmpty(angusScript.getTask().getPipelines())) {
      log.warn("Apis[{}] functionality case[{}] does not exist in script pipelines", apisId,
          caseIds);
      return;
    }

    List<TestTargetType> pipelines = new ArrayList<>();
    for (TestTargetType pipeline : angusScript.getTask().getPipelines()) {
      if (!caseIds.contains(((Http) pipeline).getCaseId())) {
        pipelines.add(pipeline);
      }
    }
    if (pipelines.isEmpty()) {
      // Delete script when its content is empty
      delete(Set.of(scriptDb.getId()));
    } else {
      angusScript.getTask().setPipelines(pipelines);
      update0(scriptDb, scriptDb.getAngusScript(), false, false);
    }
  }

  @Override
  public void enableCaseToScript(Long apisId, Collection<Long> caseIds, boolean enabled) {
    Script scriptDb = scriptQuery.findBySourceAndScriptType(ScriptSource.API, apisId,
        TEST_FUNCTIONALITY);
    if (isNull(scriptDb)) {
      log.warn("Apis[{}] functionality test script does not exist", apisId);
      return;
    }
    AngusScript angusScript = scriptQuery.checkAndParse(scriptDb.getContent(), true);
    if (isEmpty(angusScript.getTask().getPipelines())) {
      log.warn("Apis[{}] functionality case[{}] does not exist in script pipelines",
          apisId, caseIds);
      return;
    }

    for (TestTargetType pipeline : angusScript.getTask().getPipelines()) {
      Http http = ((Http) pipeline);
      if (caseIds.contains(http.getCaseId())) {
        http.setEnabled(enabled);
      }
    }
    update0(scriptDb, angusScript, false, false);
  }

  /**
   * Converts domain variables to Angus configuration variables.
   *
   * <p>This method flattens and converts a map of case variables to the format
   * required by the Angus testing framework configuration.</p>
   *
   * <p>It processes all variables from all cases and converts them to the
   * appropriate Angus model format for script configuration.</p>
   *
   * @param caseVariableMap map of case IDs to their associated variables
   * @return list of Angus configuration variables, or null if no variables exist
   */
  public static List<cloud.xcan.angus.model.element.variable.Variable> getAngusConfigurationVariables(
      Map<Long, List<Variable>> caseVariableMap) {
    List<Variable> variables = caseVariableMap.values().stream().flatMap(Collection::stream)
        .toList();
    return isEmpty(variables) ? null : variables.stream()
        .map(ApisToAngusModelConverter::toAngusVariable).toList();
  }

  /**
   * Converts domain datasets to Angus datasets for a specific case.
   *
   * <p>This method retrieves and converts datasets associated with a specific
   * API case to the format required by the Angus testing framework.</p>
   *
   * <p>It returns null if no datasets are found for the case, allowing
   * the calling code to handle the absence of datasets gracefully.</p>
   *
   * @param caseDatasetMap map of case IDs to their associated datasets
   * @param case0 the API case to get datasets for
   * @return list of Angus datasets for the case, or null if no datasets exist
   */
  @Nullable
  public static List<cloud.xcan.angus.model.element.dataset.Dataset> getAngusDataset(
      Map<Long, List<Dataset>> caseDatasetMap, ApisCase case0) {
    List<Dataset> datasets = caseDatasetMap.get(case0.getId());
    return isEmpty(datasets) ? null
        : datasets.stream().map(DatasetConverter::toAngusDataset)
            .toList();
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the script repository
   */
  @Override
  protected BaseRepository<Script, Long> getRepository() {
    return this.scriptRepo;
  }
}
