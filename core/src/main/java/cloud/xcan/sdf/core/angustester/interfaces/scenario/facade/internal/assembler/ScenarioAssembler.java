package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.info.Info;
import cloud.xcan.sdf.api.angustester.scenario.vo.ScenarioDetailVo;
import cloud.xcan.sdf.api.angustester.scenario.vo.ScenarioListVo;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioInfoFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioInfoSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.ScenarioUpdateDto;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

public class ScenarioAssembler {

  public static Scenario addDtoToDomain(ScenarioAddDto dto) {
    Scenario scenario = new Scenario()
        .setName(dto.getName())
        .setProjectId(dto.getProjectId())
        .setAuthFlag(nullSafe(dto.getAuthFlag(), false))
        .setPlugin(dto.getPlugin())
        .setDescription(dto.getDescription())
        .setAngusScript(nullSafe(dto.getScript(), new AngusScript()))
        .setDeletedFlag(false)
        .setTestFuncFlag(nullSafe(dto.getTestFuncFlag(), true))
        .setTestPerfFlag(nullSafe(dto.getTestPerfFlag(), true))
        .setTestStabilityFlag(nullSafe(dto.getTestStabilityFlag(), true));
    scenario.getAngusScript().setInfo(Info.newBuilder()
        .name(dto.getName()).description(dto.getDescription()).build());
    return scenario;
  }

  public static Scenario updateDtoToDomain(ScenarioUpdateDto dto) {
    return new Scenario().setId(dto.getId())
        .setName(dto.getName())
        //.setDirId(dto.getDirId())
        .setAuthFlag(dto.getAuthFlag())
        //.setPluginName(dto.getPluginName())
        .setDescription(dto.getDescription())
        .setAngusScript(dto.getScript())
        .setTestFuncFlag(dto.getTestFuncFlag())
        .setTestPerfFlag(dto.getTestPerfFlag())
        .setTestStabilityFlag(dto.getTestStabilityFlag());
    //.setDirDeletedFlag(false)
    //.setDeletedFlag(false);
  }

  public static Scenario replaceDtoToDomain(ScenarioReplaceDto dto) {
    Scenario scenario = new Scenario()
        .setId(dto.getId())
        .setName(dto.getName())
        //.setDirId(dto.getDirId())
        .setAuthFlag(isNull(dto.getId()) ? dto.getAuthFlag() : null)
        //.setPluginName(dto.getPluginName())
        .setDescription(dto.getDescription())
        .setAngusScript(dto.getScript());
    if (isNull(dto.getId())) {
      scenario.setProjectId(dto.getProjectId())
          .setPlugin(dto.getPlugin())
          .setAngusScript(nullSafe(dto.getScript(), new AngusScript()))
          .setDeletedFlag(false)
          .setTestFuncFlag(nullSafe(dto.getTestFuncFlag(), true))
          .setTestPerfFlag(nullSafe(dto.getTestPerfFlag(), true))
          .setTestStabilityFlag(nullSafe(dto.getTestStabilityFlag(), true));
    }
    return scenario;
  }

  public static ScenarioDetailVo toDetailVo(Scenario scenario) {
    return new ScenarioDetailVo()
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

  public static ScenarioListVo toListVo(Scenario scenario) {
    return new ScenarioListVo()
        .setId(scenario.getId())
        .setAuthFlag(scenario.getAuthFlag())
        .setName(scenario.getName())
        .setDescription(scenario.getDescription())
        .setProjectId(scenario.getProjectId())
        .setPlugin(scenario.getPlugin())
        .setScriptType(scenario.getScriptType())
        .setScriptId(scenario.getScriptId())
        .setLastExecId(scenario.getLastExecId())
        .setLastExecStatus(scenario.getLastExecStatus())
        .setLastExecFailureMessage(scenario.getLastExecFailureMessage())
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
        .setCreatedByName(scenario.getCreatedByName())
        .setAvatar(scenario.getAvatar())
        .setLastModifiedBy(scenario.getLastModifiedBy())
        .setLastModifiedDate(scenario.getLastModifiedDate());
  }

  public static GenericSpecification<Scenario> getSpecification(ScenarioInfoFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("id")
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("name", "description", "extSearchMerge")
        .orderByFields("id", "name", "createdDate", "createdBy", "lastModifiedBy")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(ScenarioInfoSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("id")
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("name", "description", "extSearchMerge")
        .orderByFields("id", "name", "createdDate", "createdBy", "lastModifiedBy")
        .build();
  }
}




