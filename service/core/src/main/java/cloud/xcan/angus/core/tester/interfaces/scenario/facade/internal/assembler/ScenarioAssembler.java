package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioInfoFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.ScenarioUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.ScenarioDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.ScenarioListVo;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.info.Info;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class ScenarioAssembler {

  public static Scenario addDtoToDomain(ScenarioAddDto dto) {
    Scenario scenario = new Scenario()
        .setName(dto.getName())
        .setProjectId(dto.getProjectId())
        .setModuleId(dto.getModuleId())
        .setAuth(nullSafe(dto.getAuth(), false))
        .setPlugin(dto.getPlugin())
        .setDescription(dto.getDescription())
        .setAngusScript(nullSafe(dto.getScript(), new AngusScript()))
        .setDeleted(false)
        .setTestFunc(nullSafe(dto.getTestFunc(), true))
        .setTestPerf(nullSafe(dto.getTestPerf(), true))
        .setTestStability(nullSafe(dto.getTestStability(), true));
    scenario.getAngusScript().setInfo(Info.newBuilder()
        .name(dto.getName()).description(dto.getDescription()).build());
    return scenario;
  }

  public static Scenario updateDtoToDomain(ScenarioUpdateDto dto) {
    return new Scenario().setId(dto.getId())
        .setName(dto.getName())
        //.setProjectId(dto.getProjectId())
        .setModuleId(dto.getModuleId())
        .setAuth(dto.getAuth())
        //.setPluginName(dto.getPluginName())
        .setDescription(dto.getDescription())
        .setAngusScript(dto.getScript())
        .setTestFunc(dto.getTestFunc())
        .setTestPerf(dto.getTestPerf())
        .setTestStability(dto.getTestStability());
    //.setDirDeleted(false)
    //.setDeleted(false);
  }

  public static Scenario replaceDtoToDomain(ScenarioReplaceDto dto) {
    Scenario scenario = new Scenario()
        .setId(dto.getId())
        .setName(dto.getName())
        .setModuleId(dto.getModuleId())
        .setAuth(isNull(dto.getId()) ? dto.getAuth() : null)
        //.setPluginName(dto.getPluginName())
        .setDescription(dto.getDescription())
        .setAngusScript(dto.getScript());
    if (isNull(dto.getId())) {
      scenario.setProjectId(dto.getProjectId())
          .setPlugin(dto.getPlugin())
          .setAngusScript(nullSafe(dto.getScript(), new AngusScript()))
          .setDeleted(false)
          .setTestFunc(nullSafe(dto.getTestFunc(), true))
          .setTestPerf(nullSafe(dto.getTestPerf(), true))
          .setTestStability(nullSafe(dto.getTestStability(), true));
    }
    return scenario;
  }

  public static ScenarioDetailVo toDetailVo(Scenario scenario) {
    return new ScenarioDetailVo()
        .setId(scenario.getId())
        .setAuth(scenario.getAuth())
        .setName(scenario.getName())
        .setProjectId(scenario.getProjectId())
        .setModuleId(scenario.getModuleId())
        .setPlugin(scenario.getPlugin())
        .setDescription(scenario.getDescription())
        .setScriptType(scenario.getScriptType())
        .setScriptId(scenario.getScriptId())
        .setScriptName(scenario.getScriptName())
        .setScript(scenario.getAngusScript())
        .setTestFunc(scenario.getTestFunc())
        .setTestFuncPassed(scenario.getTestFuncPassed())
        .setTestFuncFailureMessage(scenario.getTestFuncFailureMessage())
        .setTestPerf(scenario.getTestPerf())
        .setTestPerfPassed(scenario.getTestPerfPassed())
        .setTestPerfFailureMessage(scenario.getTestPerfFailureMessage())
        .setTestStability(scenario.getTestStability())
        .setTestStabilityPassed(scenario.getTestStabilityPassed())
        .setTestStabilityFailureMessage(scenario.getTestStabilityFailureMessage())
        .setFavourite(scenario.getFavourite())
        .setFollow(scenario.getFollow())
        .setCreatedBy(scenario.getCreatedBy())
        .setCreatedDate(scenario.getCreatedDate())
        .setModifiedBy(scenario.getModifiedBy())
        .setModifiedDate(scenario.getModifiedDate());
  }

  public static ScenarioListVo toListVo(Scenario scenario) {
    return new ScenarioListVo()
        .setId(scenario.getId())
        .setAuth(scenario.getAuth())
        .setName(scenario.getName())
        .setDescription(scenario.getDescription())
        .setProjectId(scenario.getProjectId())
        .setModuleId(scenario.getModuleId())
        .setPlugin(scenario.getPlugin())
        .setScriptType(scenario.getScriptType())
        .setScriptId(scenario.getScriptId())
        .setLastExecId(scenario.getLastExecId())
        .setLastExecName(scenario.getLastExecName())
        .setLastExecStatus(scenario.getLastExecStatus())
        .setLastExecDate(scenario.getLastExecDate())
        .setLastExecFailureMessage(scenario.getLastExecFailureMessage())
        .setTestFunc(scenario.getTestFunc())
        .setTestFuncPassed(scenario.getTestFuncPassed())
        .setTestFuncFailureMessage(scenario.getTestFuncFailureMessage())
        .setTestPerf(scenario.getTestPerf())
        .setTestPerfPassed(scenario.getTestPerfPassed())
        .setTestPerfFailureMessage(scenario.getTestPerfFailureMessage())
        .setTestStability(scenario.getTestStability())
        .setTestStabilityPassed(scenario.getTestStabilityPassed())
        .setTestStabilityFailureMessage(scenario.getTestStabilityFailureMessage())
        .setFavourite(scenario.getFavourite())
        .setFollow(scenario.getFollow())
        .setCreatedBy(scenario.getCreatedBy())
        .setCreatedDate(scenario.getCreatedDate())
        .setCreator(scenario.getCreator())
        .setAvatar(scenario.getAvatar())
        .setModifiedBy(scenario.getModifiedBy())
        .setModifiedDate(scenario.getModifiedDate());
  }

  public static GenericSpecification<Scenario> getSpecification(ScenarioInfoFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("id")
        .rangeSearchFields("id", "platform", "scriptType", "createdDate")
        .matchSearchFields("name", "description", "extSearchMerge")
        .orderByFields("id", "name", "createdDate", "createdBy", "modifiedBy")
        .build();
    return new GenericSpecification<>(filters);
  }

}




