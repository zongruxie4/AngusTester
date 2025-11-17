package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.convert;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static cloud.xcan.angus.spec.utils.WorkingTimeCalculator.isLastMonth;
import static cloud.xcan.angus.spec.utils.WorkingTimeCalculator.isLastWeek;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptCount;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ScriptConverter {

  public static Script toAngusAddScript(Long projectId, AngusScript script, String content) {
    return new Script()
        .setProjectId(projectId)
        .setName(script.getInfo().getName())
        .setType(script.getType())
        .setSource(script.getType().isMockData()
            ? ScriptSource.GENERATE_DATA : ScriptSource.USER_DEFINED)
        .setAuth(true)
        .setContent(content)
        .setDescription(script.getInfo().getDescription());
  }

  public static Script toAngusScenarioAddScript(Long scenarioId, Long projectId, Boolean auth,
      AngusScript script, String content) {
    return new Script()
        .setProjectId(projectId)
        .setName(script.getInfo().getName())
        .setDescription(script.getInfo().getDescription())
        .setType(script.getType())
        .setSource(ScriptSource.SCENARIO)
        .setSourceId(scenarioId)
        .setAuth(auth)
        .setContent(content);
  }

  public static void setReplaceInfo(Script scriptDb, Script script, AngusScript angusScript) {
    scriptDb.setName(script.getName())
        .setType(nullSafe(script.getType(), scriptDb.getType()))
        .setDescription(script.getDescription())
        .setPlugin(angusScript.getPlugin())
        .setContent(script.getContent());
  }

  public static void setReplaceInfo(Script scriptDb, AngusScript angusScript, String content) {
    scriptDb.setName(isNull(angusScript.getInfo())
            ? scriptDb.getName() : stringSafe(angusScript.getInfo().getName()))
        .setType(angusScript.getType())
        .setDescription(isNull(angusScript.getInfo())
            ? scriptDb.getDescription() : stringSafe(angusScript.getInfo().getDescription()))
        .setPlugin(angusScript.getPlugin())
        .setContent(content);
  }

  public static Script toClonedScript(Script scriptDb) {
    Script script = new Script();
    script.setProjectId(scriptDb.getProjectId());
    script.setServiceId(null);
    String saltName = randomAlphanumeric(3);
    script.setName(scriptDb.getName() + "-Copy." + saltName);
    script.setType(scriptDb.getType());
    script.setSource(ScriptSource.USER_DEFINED);
    script.setSourceId(null);
    script.setPlugin(scriptDb.getPlugin());
    script.setAuth(scriptDb.getAuth());
    script.setDescription(scriptDb.getDescription());
    script.setContent(scriptDb.getContent());
    return script;
  }

  public static Script toClonedScenarioScript(Script scriptDb, Long newScenarioId) {
    Script script = new Script();
    script.setProjectId(scriptDb.getProjectId());
    script.setServiceId(null);
    String saltName = randomAlphanumeric(3);
    script.setName(scriptDb.getName() + "-Copy." + saltName);
    script.setType(scriptDb.getType());
    script.setSource(ScriptSource.SCENARIO);
    script.setSourceId(newScenarioId);
    script.setPlugin(scriptDb.getPlugin());
    script.setAuth(scriptDb.getAuth());
    script.setDescription(scriptDb.getDescription());
    script.setContent(scriptDb.getContent());
    return script;
  }

  public static Script importDtoToDomain(Long id, Long projectId, String name,
      String description, String content) {
    return new Script()
        .setId(id)
        .setProjectId(projectId)
        .setName(stringSafe(name, "Script" + System.currentTimeMillis()))
        //.setType(dto.getType())
        .setSource(ScriptSource.IMPORTED)
        .setAuth(true)
        .setDescription(description)
        .setContent(content);
  }

  /**
   * <pre>
   * type                 source      num
   * MOCK_DATA	          CREATED	    37
   * TEST_FUNCTIONALITY	  CREATED	    9
   * TEST_FUNCTIONALITY	  IMPORTED	  1
   * TEST_FUNCTIONALITY	  SCENARIO	  11
   * TEST_PERFORMANCE	    APIS	      2
   * TEST_PERFORMANCE	    CREATED	    25
   * TEST_PERFORMANCE	    IMPORTED	  1
   * TEST_PERFORMANCE	    SCENARIO	  78
   * TEST_STABILITY	      APIS	      2
   * TEST_STABILITY	      SCENARIO	  4
   * </pre>
   */
  public static ScriptCount objectArrToScriptCount(List<Object[]> groupByResult) {
    if (isEmpty(groupByResult)) {
      return new ScriptCount();
    }
    ScriptCount statistics = new ScriptCount();
    // Statistics by script type
    Map<String, Integer> typeMap = groupByResult.stream().collect(Collectors.toMap(
        x -> convert(x[0], String.class), x -> convert(x[2], Integer.class), Integer::sum));
    statistics.setPerfScriptNum(nullSafe(typeMap.get(ScriptType.TEST_PERFORMANCE.name()), 0))
        .setFunctionalScriptNum(nullSafe(typeMap.get(ScriptType.TEST_FUNCTIONALITY.name()), 0))
        .setStabilityScriptNum(nullSafe(typeMap.get(ScriptType.TEST_STABILITY.name()), 0))
        .setCustomizationScriptNum(nullSafe(typeMap.get(ScriptType.TEST_CUSTOMIZATION.name()), 0))
        .setMockDataScriptNum(nullSafe(typeMap.get(ScriptType.MOCK_DATA.name()), 0))
        .setMockApisScriptNum(nullSafe(typeMap.get(ScriptType.MOCK_APIS.name()), 0));

    statistics.setTotalScriptNum(statistics.getPerfScriptNum()
        + statistics.getFunctionalScriptNum() + statistics.getStabilityScriptNum()
        + statistics.getCustomizationScriptNum() + statistics.getMockDataScriptNum());

    // Statistics by source
    Map<String, Integer> sourceMap = groupByResult.stream().collect(Collectors.toMap(
        x -> convert(x[1], String.class), x -> convert(x[2], Integer.class), Integer::sum));
    statistics.setCreatedSourceNum(nullSafe(sourceMap.get(ScriptSource.USER_DEFINED.name()), 0))
        .setImportedSourceNum(nullSafe(sourceMap.get(ScriptSource.IMPORTED.name()), 0))
        .setApisSourceNum(nullSafe(sourceMap.get(ScriptSource.API.name()), 0))
        .setScenarioSourceNum(nullSafe(sourceMap.get(ScriptSource.SCENARIO.name()), 0));
    return statistics;
  }

  public static void countCreationScript(ScriptResourcesCreationCount result,
      List<ScriptInfo> scripts) {
    result.setAllScript(scripts.size())
        .setScriptByLastWeek(scripts.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setScriptByLastMonth(
            scripts.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());

    Map<ScriptType, List<ScriptInfo>> typeMap = scripts.stream()
        .collect(Collectors.groupingBy(ScriptInfo::getType));
    for (ScriptType value : ScriptType.values()) {
      result.getScriptByScriptType().put(value, typeMap.getOrDefault(value, emptyList()).size());
    }

    Map<String, List<ScriptInfo>> pluginMap = scripts.stream()
        .collect(Collectors.groupingBy(ScriptInfo::getPlugin));
    for (Entry<String, List<ScriptInfo>> entry : pluginMap.entrySet()) {
      result.getScriptByPluginName().put(entry.getKey(), entry.getValue().size());
    }
  }
}
