package cloud.xcan.sdf.core.angustester.application.converter;


import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.spring.SpringContextHolder.getCachedUidGenerator;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.convert;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.sdf.spec.utils.WorkingTimeCalculator.isLastMonth;
import static cloud.xcan.sdf.spec.utils.WorkingTimeCalculator.isLastWeek;
import static java.util.Collections.emptyList;

import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.model.scenario.ScenarioInfo;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.scenario.summary.ScenarioDetailSummary;
import cloud.xcan.sdf.core.angustester.domain.scenario.trash.ScenarioTrash;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ScenarioConverter {

  public static ScenarioTrash toScenarioTrash(Scenario scenarioDb) {
    return new ScenarioTrash()
        .setProjectId(scenarioDb.getProjectId())
        .setTargetId(scenarioDb.getId())
        .setTargetName(scenarioDb.getName())
        .setCreatedBy(scenarioDb.getCreatedBy())
        .setDeletedBy(getUserId())
        .setDeletedDate(LocalDateTime.now());
  }

  public static Scenario objectArrToScenario(Object[] objects) {
    Scenario scenario = new Scenario()
        .setId(convert(objects[0], Long.class))
        .setName(convert(objects[1], String.class))
        .setDescription(convert(objects[2], String.class))
        .setProjectId(convert(objects[3], Long.class))
        .setAuthFlag(convert(objects[4], Boolean.class))
        .setPlugin(convert(objects[5], String.class))
        .setScriptType(convert(objects[6], ScriptType.class))
        .setScriptId(convert(objects[7], Long.class));
    //.setDescription(convert(objects[5], String.class));
    scenario.setCreatedBy(convert(objects[8], Long.class));
    scenario.setCreatedDate(convert(objects[9], LocalDateTime.class));
    scenario.setLastModifiedBy(convert(objects[10], Long.class));
    scenario.setLastModifiedDate(convert(objects[11], LocalDateTime.class));
    return scenario;
  }

  public static void setReplaceRetentionInfo(Scenario scenario, Scenario scenarioDb) {
    scenario.setProjectId(scenarioDb.getProjectId())
        .setPlugin(scenarioDb.getPlugin())
        .setAuthFlag(scenarioDb.getAuthFlag())
        .setTestFuncFlag(scenarioDb.getTestFuncFlag())
        .setTestPerfFlag(scenarioDb.getTestPerfFlag())
        .setTestStabilityFlag(scenarioDb.getTestStabilityFlag())
        .setDeletedFlag(scenarioDb.getDeletedFlag())
        .setDeletedBy(scenarioDb.getDeletedBy())
        .setDeletedDate(scenarioDb.getDeletedDate())
        .setExtSearchMerge(scenarioDb.getExtSearchMerge());
    scenario.setScriptType(nullSafe(scenario.getAngusScript().getType(),
        scenarioDb.getScriptType()));
    scenario.setAuthFlag(nullSafe(scenarioDb.getAuthFlag(), false));
    scenario.setScriptId(scenarioDb.getScriptId());
  }

  public static Scenario toCloneScenario(Scenario scenarioDb) {
    return new Scenario()
        .setId(getCachedUidGenerator().getUID())
        .setProjectId(scenarioDb.getProjectId())
        .setName(scenarioDb.getName())
        .setAuthFlag(scenarioDb.getAuthFlag())
        .setPlugin(scenarioDb.getPlugin())
        .setScriptType(scenarioDb.getScriptType())
        .setDescription(scenarioDb.getDescription())
        .setTestFuncFlag(scenarioDb.getTestFuncFlag())
        .setTestPerfFlag(scenarioDb.getTestPerfFlag())
        .setTestStabilityFlag(scenarioDb.getTestStabilityFlag())
        .setDeletedFlag(false);
  }

  public static Scenario importExampleToDomain(Long projectId, AngusScript angusScript) {
    return new Scenario()
        .setName(angusScript.getInfo().getName())
        .setProjectId(projectId)
        .setAuthFlag(false)
        .setPlugin(angusScript.getPlugin())
        .setDescription(angusScript.getInfo().getDescription())
        .setAngusScript(angusScript)
        .setDeletedFlag(false)
        .setTestFuncFlag(false)
        .setTestPerfFlag(false)
        .setTestStabilityFlag(false);
  }

  public static ScenarioInfo toScenarioInfo(Scenario scenario) {
    return new ScenarioInfo()
        .setId(scenario.getId())
        .setName(scenario.getName())
        .setPlugin(scenario.getPlugin())
        .setScriptType(scenario.getScriptType())
        .setScriptId(scenario.getScriptId());
  }

  public static ScenarioDetailSummary toScenarioDetailSummary(Scenario scenario) {
    return new ScenarioDetailSummary()
        .setId(scenario.getId())
        .setAuthFlag(scenario.getAuthFlag())
        .setName(scenario.getName())
        .setProjectId(scenario.getProjectId())
        .setPlugin(scenario.getPlugin())
        .setDescription(scenario.getDescription())
        .setScriptType(scenario.getScriptType())
        .setScriptId(scenario.getScriptId())
        .setScriptName(scenario.getScriptName())
        .setScript(scenario.getAngusScript())
        .setTestFuncFlag(scenario.getTestFuncFlag())
        .setTestFuncPassedFlag(scenario.getTestFuncPassedFlag())
        .setTestFuncFailureMessage(scenario.getTestFuncFailureMessage())
        .setTestPerfFlag(scenario.getTestPerfFlag())
        .setTestPerfPassedFlag(scenario.getTestPerfPassedFlag())
        .setTestPerfFailureMessage(scenario.getTestPerfFailureMessage())
        .setTestStabilityFlag(scenario.getTestStabilityFlag())
        .setTestStabilityPassedFlag(scenario.getTestStabilityPassedFlag())
        .setTestStabilityFailureMessage(scenario.getTestStabilityFailureMessage())
        .setFavouriteFlag(scenario.getFavouriteFlag())
        .setFollowFlag(scenario.getFollowFlag())
        .setCreatedBy(scenario.getCreatedBy())
        .setCreatedDate(scenario.getCreatedDate())
        .setLastModifiedBy(scenario.getLastModifiedBy())
        .setLastModifiedDate(scenario.getLastModifiedDate());
  }

  public static void countCreationScenario(ScenarioResourcesCreationCount result,
      List<Scenario> scenarios) {
    result.setAllSce(scenarios.size())
        .setSceByLastWeek(scenarios.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setSceByLastMonth(scenarios.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());

    Map<ScriptType, List<Scenario>> typeMap = scenarios.stream()
        .collect(Collectors.groupingBy(Scenario::getScriptType));
    for (ScriptType value : ScriptType.values()) {
      result.getSceByScriptType().put(value, typeMap.getOrDefault(value, emptyList()).size());
    }

    Map<String, List<Scenario>> pluginMap = scenarios.stream()
        .collect(Collectors.groupingBy(Scenario::getPlugin));
    for (Entry<String, List<Scenario>> entry : pluginMap.entrySet()) {
      result.getSceByPluginName().put(entry.getKey(), entry.getValue().size());
    }
  }

}
