package cloud.xcan.sdf.core.angustester.application.cmd.script.impl;

import static cloud.xcan.angus.core.utils.AngusUtils.overrideExecServerParameter;
import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.angus.model.script.configuration.ScriptType.TEST_FUNCTIONALITY;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.API_CASE;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SCRIPT;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.SAMPLE_SCRIPT_FILES;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.ApisToAngusModelConverter.getScriptTaskArguments;
import static cloud.xcan.sdf.core.angustester.application.converter.ApisToAngusModelConverter.toHttp;
import static cloud.xcan.sdf.core.angustester.application.converter.ScriptConverter.importDtoToDomain;
import static cloud.xcan.sdf.core.angustester.application.converter.ScriptConverter.setReplaceInfo;
import static cloud.xcan.sdf.core.angustester.application.converter.ScriptConverter.toAngusAddScript;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.DELETED;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.sdf.spec.experimental.BizConstant.ANGUS_SCRIPT_LENGTH;
import static cloud.xcan.sdf.spec.experimental.StandardCharsets.UTF_8;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.sdf.spec.utils.StreamUtils.copyToString;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.model.element.http.Http;
import cloud.xcan.angus.model.element.type.TestTargetType;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.Threads;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import cloud.xcan.angus.model.script.pipeline.Task;
import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.script.ScriptAuthCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.script.ScriptCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.script.ScriptTagCmd;
import cloud.xcan.sdf.core.angustester.application.converter.ApisToAngusModelConverter;
import cloud.xcan.sdf.core.angustester.application.converter.DatasetConverter;
import cloud.xcan.sdf.core.angustester.application.converter.ScriptConverter;
import cloud.xcan.sdf.core.angustester.application.query.data.DatasetTargetQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableTargetQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCase;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioRepo;
import cloud.xcan.sdf.core.angustester.domain.script.Script;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfo;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptRepo;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.model.script.ScriptSource;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.spec.experimental.IdKey;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.servers.Server;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.transaction.annotation.Transactional;

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
   * Note: Scenarios and scripts are one-to-one, apis (different test types) and scripts are
   * one-to-many.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Script script, boolean saveActivity) {
    return new BizTemplate<IdKey<Long, Object>>() {
      AngusScript angusScript;

      @Override
      protected void checkParams() {
        // Check the project member
        projectMemberQuery.checkMember(script.getProjectId(), getUserId());

        // Check the script num quota
        scriptQuery.checkQuota(1);

        // Check the source exists
        scriptQuery.checkSourceResourceExist(script);

        // Check the script length
        scriptQuery.checkScriptLength(script.getContent());

        // Check and parse script
        angusScript = scriptQuery.checkAndParse(script.getContent(), true);

        // Check the resource script exists,
        // Apis and scenarios There can be only one script for each test type
        script.setType(nullSafe(script.getType(), angusScript.getType()));
        scriptQuery.checkSourceAddScriptExist(script);

        // TODO Check plugin is purchased and valid

        // TODO Check test quota is valid
      }

      @Override
      protected IdKey<Long, Object> process() {
        // NOOP: Overwrite script type
        judgeScriptType(script, angusScript);
        script.setPlugin(angusScript.getPlugin());

        IdKey<Long, Object> idKey = insert(script, "name");

        // Init creator auth
        scriptAuthCmd.addCreatorAuth(Set.of(getUserId()), idKey.getId());

        // Add scripts tags
        scriptTagCmd.add(script.getId(), angusScript.getTags());

        // Save activity
        activityCmd.add(toActivity(SCRIPT, script, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Script script, AngusScript angusScript, boolean validateScript) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the project member
        projectMemberQuery.checkMember(script.getProjectId(), getUserId());

        // Check the script num quota
        scriptQuery.checkQuota(1);

        // Check the source exists
        scriptQuery.checkSourceResourceExist(script);

        // Check the resource script exists,
        // Apis and scenarios There can be only one script for each test type
        script.setType(nullSafe(script.getType(), angusScript.getType()));
        scriptQuery.checkSourceAddScriptExist(script);

        // Check and serialize script
        if (isEmpty(script.getContent())) {
          script.setContent(scriptQuery.checkAndSerialize(angusScript, validateScript));
        }

        // TODO Check plugin is purchased and valid

        // TODO Check test quota is valid
      }

      @Override
      protected IdKey<Long, Object> process() {
        // NOOP: Overwrite script type
        judgeScriptType(script, angusScript);
        script.setPlugin(angusScript.getPlugin());

        // Fix: Oh my god, internal reflection will generate duplicate ID.
        script.setId(uidGenerator.getUID());
        IdKey<Long, Object> idKey = insert(script, "name");

        // Init creator auth
        scriptAuthCmd.addCreatorAuth(Set.of(getUserId()), idKey.getId());

        // Add scripts tags
        scriptTagCmd.add(script.getId(), angusScript.getTags());

        // Save activity
        activityCmd.add(toActivity(SCRIPT, script, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  @Override
  public IdKey<Long, Object> addByScenario(Script script, AngusScript angusScript) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the resource script exists,
        // Apis and scenarios There can be only one script for each test type
        script.setType(nullSafe(script.getType(), angusScript.getType()));
        scriptQuery.checkSourceAddScriptExist(script);

        // Check and serialize script
        if (isEmpty(script.getContent())) {
          script.setContent(scriptQuery.checkAndSerialize(angusScript, true));
        }

        // TODO Check plugin is purchased and valid

        // TODO Check test quota is valid
      }

      @Override
      protected IdKey<Long, Object> process() {
        // NOOP: Overwrite script type
        judgeScriptType(script, angusScript);
        script.setPlugin(angusScript.getPlugin());

        // Fix: Oh my god, internal reflection will generate duplicate ID.
        script.setId(uidGenerator.getUID());
        IdKey<Long, Object> idKey = insert(script, "name");

        // Init creator auth
        scriptAuthCmd.addCreatorAuth(Set.of(getUserId()), idKey.getId());

        // Add scripts tags
        scriptTagCmd.add(script.getId(), angusScript.getTags());

        // Save activity
        activityCmd.add(toActivity(SCRIPT, script, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> addByAngus(Long projectId, AngusScript angusScript,
      boolean validateScript) {
    return new BizTemplate<IdKey<Long, Object>>() {
      String content;

      @Override
      protected void checkParams() {
        ProtocolAssert.assertNotNull(projectId, "Parameter projectId is null");

        // Check and serialize script
        content = scriptQuery.checkAndSerialize(angusScript, true);
      }

      @Override
      protected IdKey<Long, Object> process() {
        return add(toAngusAddScript(projectId, angusScript, content), angusScript, validateScript);
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Script script) {
    new BizTemplate<Void>() {
      Script scriptDb;
      AngusScript angusScript;

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

        // Check the script length
        scriptQuery.checkScriptLength(script.getContent());

        // Check and parse script
        angusScript = scriptQuery.checkAndParse(script.getContent(), true);

        // TODO Check plugin is purchased and valid

        // TODO Check test quota is valid
      }

      @Override
      protected Void process() {
        // NOOP: Overwrite script type, name and description -> Allowing inconsistencies.

        if (nonNull(angusScript)) {
          script.setPlugin(angusScript.getPlugin());
        }

        scriptRepo.save(copyPropertiesIgnoreNull(script, scriptDb));

        // Save activity
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

  @Override
  public void update0(Script scriptDb, AngusScript angusScript, boolean replaceTag,
      boolean validateScript) {
    // Check and serialize script
    scriptDb.setContent(scriptQuery.checkAndSerialize(angusScript, validateScript));

    // NOOP: Overwrite script type, name and description -> Allowing inconsistencies.

    if (nonNull(angusScript)) {
      scriptDb.setPlugin(angusScript.getPlugin());
    }

    judgeScriptType(scriptDb, angusScript);
    scriptRepo.save(scriptDb);

    // Replace scripts tags
    if (replaceTag) {
      scriptTagCmd.replace(scriptDb.getId(), angusScript.getTags());
    }

    // Save activity
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

        ProtocolAssert.assertTrue(scriptDb.getSource().isUnique(),
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
  public List<IdKey<Long, Object>> sampleImport() {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @SneakyThrows
      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = new ArrayList<>();
        for (String file : SAMPLE_SCRIPT_FILES) {
          URL resourceUrl = this.getClass().getResource("/samples/script/"
              + getDefaultLanguage().getValue() + "/" + file);
          String content = copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
          AngusScript angusScript = scriptQuery.checkAndParse(content, true);
          Script script = importDtoToDomain(angusScript.getInfo().getName(),
              angusScript.getInfo().getDescription(), content);
          idKeys.add(imports(script));
        }
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> imports(Script script) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        ProtocolAssert.assertTrue(isNotBlank(script.getContent()) || nonNull(script.getFile()),
            "Importing content and files must specify one of them");
      }

      @SneakyThrows
      @Override
      protected IdKey<Long, Object> process() {
        if (isBlank(script.getContent())) {
          String content = copyToString(script.getFile().getInputStream(), UTF_8);
          ProtocolAssert.assertTrue(content.length() <= ANGUS_SCRIPT_LENGTH,
              "Script length exceeds the limit of " + ANGUS_SCRIPT_LENGTH);
          script.setContent(content);
        }

        IdKey<Long, Object> idKey = add(script, false);

        // Save activity
        activityCmd.add(toActivity(SCRIPT, script, ActivityType.IMPORT));
        return idKey;
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
        activityCmd.batchAdd(toActivities(SCRIPT, scriptDb, DELETED, activityParams(scriptDb)));
        return null;
      }
    }.execute();
  }

  @Override
  public void deleteBySource(ScriptSource source, Collection<Long> targetIds) {
    Set<Long> scriptIds = scriptQuery.findIdsBySource(source, targetIds);
    if (ObjectUtils.isNotEmpty(scriptIds)) {
      scriptRepo.deleteByIdIn(scriptIds);
      scriptTagCmd.deleteByScriptIdIn(scriptIds);
      scriptAuthCmd.deleteByScriptIdIn(scriptIds);
    }
  }

  @Override
  public void deleteBySource(ScriptSource source, Collection<Long> targetIds,
      Collection<ScriptType> testTypes) {
    Set<Long> scriptIds = scriptQuery.findIdsBySourceAndTypeIn(source, targetIds, testTypes);
    if (ObjectUtils.isNotEmpty(scriptIds)) {
      scriptRepo.deleteByIdIn(scriptIds);
      scriptTagCmd.deleteByScriptIdIn(scriptIds);
      scriptAuthCmd.deleteByScriptIdIn(scriptIds);
    }
  }

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
    Script scriptDb = scriptQuery.findBySourceAndScriptType(ScriptSource.API, apisDb.getId(),
        TEST_FUNCTIONALITY);
    List<Long> caseIds = casesDb.stream().map(ApisCase::getId).collect(Collectors.toList());
    Map<Long, Long> caseApiMap = casesDb.stream()
        .collect(Collectors.toMap(ApisCase::getId, ApisCase::getApisId));
    Map<Long, List<Variable>> caseVariableMap = variableTargetQuery.findVariables(caseIds,
        API_CASE.getValue(), caseApiMap);
    Map<Long, List<Dataset>> caseDatasetMap = datasetTargetQuery.findDatasets(caseIds,
        API_CASE.getValue(), caseApiMap);
    if (isNull(scriptDb)) {
      String name = String.format("%s[%s]", apisDb.getName(), TestType.FUNCTIONAL.getMessage());
      Script script = assembleFuncTestScript(name, casesDb, false, ScriptSource.API, apisDb.getId(),
          apisDb.getProjectId(), apisDb.getServiceId(), caseVariableMap, caseDatasetMap);
      return add(script, script.getAngusScript(), false).getId();
    } else {
      // Update existed script
      AngusScript angusScript = scriptQuery.checkAndParse(scriptDb.getContent(), false);
      List<cloud.xcan.angus.model.element.variable.Variable>
          angusVariables = getAngusConfigurationVariables(caseVariableMap);
      angusScript.getConfiguration().setVariables(angusVariables);

      if (ObjectUtils.isEmpty(angusScript.getTask().getPipelines())) {
        angusScript.getTask().setPipelines(casesDb.stream().map(ApisToAngusModelConverter::toHttp)
            .collect(Collectors.toList()));
      } else {
        List<TestTargetType> pipelines = new ArrayList<>();
        List<String> existedInScript = new ArrayList<>();
        // Preserve and update existing targets
        for (TestTargetType pipeline : angusScript.getTask().getPipelines()) {
          ApisCase existCases = casesDb.stream()
              .filter(x -> Objects.equals(x.getName(), pipeline.getName())).findFirst()
              .orElse(null);
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
    List<Long> caseIds = casesDb.stream().map(ApisCase::getId).collect(Collectors.toList());
    Map<Long, Long> caseApiMap = casesDb.stream()
        .collect(Collectors.toMap(ApisCase::getId, ApisCase::getApisId));
    Map<Long, List<Variable>> caseVariableMap = variableTargetQuery.findVariables(caseIds,
        API_CASE.getValue(), caseApiMap);
    Map<Long, List<Dataset>> caseDatasetMap = datasetTargetQuery.findDatasets(caseIds,
        API_CASE.getValue(), caseApiMap);
    Map<String, Server> serverMap = ObjectUtils.isEmpty(servers) ? Collections.emptyMap()
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

  public static Script assembleFuncTestScript(String name, List<ApisCase> cases, boolean authFlag,
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
        .setAuthFlag(authFlag)
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
    if (ObjectUtils.isEmpty(angusScript.getTask().getPipelines())) {
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
  public void deleteCaseFromScript(Long apisId, Collection<Long> caseIds) {
    Script scriptDb = scriptQuery.findBySourceAndScriptType(ScriptSource.API, apisId,
        TEST_FUNCTIONALITY);
    if (isNull(scriptDb)) {
      log.warn("Apis[{}] functionality test script does not exist", apisId);
      return;
    }
    AngusScript angusScript = scriptQuery.checkAndParse(scriptDb.getContent(), true);
    if (ObjectUtils.isEmpty(angusScript.getTask().getPipelines())) {
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
    angusScript.getTask().setPipelines(pipelines);
    update0(scriptDb, scriptDb.getAngusScript(), false, false);
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
    if (ObjectUtils.isEmpty(angusScript.getTask().getPipelines())) {
      log.warn("Apis[{}] functionality case[{}] does not exist in script pipelines", apisId,
          caseIds);
      return;
    }

    for (TestTargetType pipeline : angusScript.getTask().getPipelines()) {
      Http http = ((Http) pipeline);
      if (caseIds.contains(http.getCaseId())) {
        http.setEnabled(enabled);
      }
    }
    update0(scriptDb, scriptDb.getAngusScript(), false, false);
  }

  public static List<cloud.xcan.angus.model.element.variable.Variable> getAngusConfigurationVariables(
      Map<Long, List<Variable>> caseVariableMap) {
    List<Variable> variables = caseVariableMap.values().stream().flatMap(Collection::stream)
        .collect(Collectors.toList());
    return ObjectUtils.isEmpty(variables) ? null : variables.stream()
        .map(ApisToAngusModelConverter::toAngusVariable).collect(Collectors.toList());
  }

  public static @Nullable List<cloud.xcan.angus.model.element.dataset.Dataset> getAngusDataset(
      Map<Long, List<Dataset>> caseDatasetMap, ApisCase case0) {
    List<Dataset> datasets = caseDatasetMap.get(case0.getId());
    return ObjectUtils.isEmpty(datasets) ? null
        : datasets.stream().map(DatasetConverter::toAngusDataset)
            .collect(Collectors.toList());
  }

  @Override
  protected BaseRepository<Script, Long> getRepository() {
    return this.scriptRepo;
  }
}
