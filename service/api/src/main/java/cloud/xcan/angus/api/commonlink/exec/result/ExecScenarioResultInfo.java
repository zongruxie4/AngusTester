package cloud.xcan.angus.api.commonlink.exec.result;

import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.model.scenario.ScenarioTestCount;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecScenarioResultInfo {

  private Progress progress;

  private ScenarioTestCount testScenarios;

  private TestResultCount testResultCount;

  private List<ExecResultSummary> testResultInfos;

  private LinkedHashMap<ScriptType, Integer> sceByScriptType = new LinkedHashMap<>(); // For AngusTester Report

  private LinkedHashMap<String, Integer> sceByPluginName = new LinkedHashMap<>(); // For AngusTester Report

}
