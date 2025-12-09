package cloud.xcan.angus.core.tester.interfaces.script.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.tester.script.ScriptDetailVo;
import cloud.xcan.angus.api.tester.script.dto.ScriptAddDto;
import cloud.xcan.angus.api.tester.script.dto.ScriptFindDto;
import cloud.xcan.angus.api.tester.script.vo.AngusScriptDetailVo;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfoListVo;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfoVo;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfosVo;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptImportDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.ScriptListVo;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.TestPlatform;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class ScriptAssembler {

  public static Script addDtoToDomain(ScriptAddDto dto) {
    return new Script()
        .setProjectId(dto.getProjectId())
        .setName(dto.getName())
        .setPlatform(nullSafe(dto.getPlatform(), TestPlatform.API))
        .setType(dto.getType())
        .setSource(ScriptSource.USER_DEFINED)
        .setAuth(nullSafe(dto.getAuth(), false))
        .setContent(dto.getContent())
        .setDescription(dto.getDescription());
  }

  public static Script updateDtoToDomain(ScriptUpdateDto dto) {
    return new Script().setId(dto.getId())
        .setName(dto.getName())
        .setPlatform(dto.getPlatform())
        .setType(dto.getType())
        .setContent(dto.getContent())
        .setDescription(dto.getDescription());
  }

  public static Script replaceDtoToDomain(ScriptReplaceDto dto) {
    return new Script().setId(dto.getId())
        .setProjectId(isNull(dto.getId()) ? dto.getProjectId() : null)
        .setName(dto.getName())
        .setAuth(isNull(dto.getId()) ? nullSafe(dto.getAuth(), false) : null)
        .setPlatform(nullSafe(dto.getPlatform(), TestPlatform.API))
        .setType(dto.getType())
        .setSource(isNull(dto.getId()) ? ScriptSource.USER_DEFINED : null)
        .setContent(dto.getContent())
        .setDescription(dto.getDescription());
  }

  public static Script importDtoToDomain(ScriptImportDto dto) {
    return new Script()
        .setProjectId(dto.getProjectId())
        .setName(stringSafe(dto.getName(), "Script" + System.currentTimeMillis()))
        //.setType(dto.getType())
        .setSource(ScriptSource.IMPORTED)
        .setAuth(false)
        .setDescription(dto.getDescription())
        .setContent(dto.getContent())
        .setFile(dto.getFile());
  }

  public static ScriptDetailVo toDetailVo(Script script) {
    return new ScriptDetailVo()
        .setId(script.getId())
        .setProjectId(script.getProjectId())
        .setServiceId(script.getServiceId())
        .setName(script.getName())
        .setPlatform(script.getPlatform())
        .setType(script.getType())
        .setSource(script.getSource())
        .setSourceId(script.getSourceId())
        .setSourceName(script.getSourceName())
        .setAuth(script.getAuth())
        .setTags(script.getTags())
        .setPermissions(script.getPermissions())
        .setPlugin(script.getPlugin())
        .setDescription(script.getDescription())
        .setContent(script.getContent())
        .setTenantId(script.getTenantId())
        .setCreatedBy(script.getCreatedBy())
        .setCreatedDate(script.getCreatedDate())
        .setLastModifiedBy(script.getLastModifiedBy())
        .setLastModifiedDate(script.getLastModifiedDate());
  }

  public static ScriptInfoVo toInfoVo(Script script) {
    return new ScriptInfoVo()
        .setId(script.getId())
        .setProjectId(script.getProjectId())
        .setServiceId(script.getServiceId())
        .setName(script.getName())
        .setPlatform(script.getPlatform())
        .setType(script.getType())
        .setSource(script.getSource())
        .setSourceId(script.getSourceId())
        //.setSourceName(script.getSourceName())
        .setAuth(script.getAuth())
        .setPermissions(script.getPermissions())
        .setPlugin(script.getPlugin())
        .setDescription(script.getDescription())
        .setContent(script.getContent())
        .setTenantId(script.getTenantId())
        .setCreatedBy(script.getCreatedBy())
        .setCreatedDate(script.getCreatedDate())
        .setLastModifiedBy(script.getLastModifiedBy())
        .setLastModifiedDate(script.getLastModifiedDate());
  }

  public static ScriptInfosVo toInfosVo(ScriptInfo script) {
    return new ScriptInfosVo()
        .setId(script.getId())
        .setProjectId(script.getProjectId())
        .setServiceId(script.getServiceId())
        .setName(script.getName())
        .setPlatform(script.getPlatform())
        .setType(script.getType())
        .setSource(script.getSource())
        .setSourceId(script.getSourceId())
        .setSourceName(script.getSourceName())
        .setAuth(script.getAuth())
        //.setPermissions(script.getPermissions())
        .setPlugin(script.getPlugin())
        .setDescription(script.getDescription())
        //.setContent(script.getContent())
        .setTenantId(script.getTenantId())
        .setCreatedBy(script.getCreatedBy())
        .setCreatedDate(script.getCreatedDate())
        .setLastModifiedBy(script.getLastModifiedBy())
        .setLastModifiedDate(script.getLastModifiedDate());
  }

  public static AngusScriptDetailVo toAngusDetailVo(Script script) {
    return new AngusScriptDetailVo()
        .setId(script.getId())
        .setProjectId(script.getProjectId())
        .setServiceId(script.getServiceId())
        .setName(script.getName())
        .setPlatform(script.getPlatform())
        .setType(script.getType())
        .setSource(script.getSource())
        .setSourceId(script.getSourceId())
        .setSourceName(script.getSourceName())
        .setAuth(script.getAuth())
        .setPlugin(script.getPlugin())
        .setDescription(script.getDescription())
        .setContent(script.getAngusScript())
        .setCreatedBy(script.getCreatedBy())
        .setCreatedDate(script.getCreatedDate())
        .setLastModifiedBy(script.getLastModifiedBy())
        .setLastModifiedDate(script.getLastModifiedDate());
  }

  public static ScriptListVo toScriptListVo(ScriptInfo script) {
    return new ScriptListVo()
        .setId(script.getId())
        .setProjectId(script.getProjectId())
        .setServiceId(script.getServiceId())
        .setName(script.getName())
        .setPlatform(script.getPlatform())
        .setType(script.getType())
        .setSource(script.getSource())
        .setSourceId(script.getSourceId())
        .setSourceName(script.getSourceName())
        .setAuth(script.getAuth())
        .setTags(script.getTags())
        //.setPermissions(script.getPermissions())
        .setPlugin(script.getPlugin())
        .setCreatedBy(script.getCreatedBy())
        .setCreatedDate(script.getCreatedDate())
        .setLastModifiedBy(script.getLastModifiedBy())
        .setLastModifiedDate(script.getLastModifiedDate());
  }

  public static ScriptInfoListVo toScriptInfoListVo(ScriptInfo script) {
    return new ScriptInfoListVo()
        .setId(script.getId())
        .setProjectId(script.getProjectId())
        .setServiceId(script.getServiceId())
        .setName(script.getName())
        .setPlatform(script.getPlatform())
        .setType(script.getType())
        .setSourceId(script.getSourceId())
        .setSource(script.getSource())
        .setSourceName(script.getSourceName())
        .setAuth(script.getAuth())
        //.setSourceTargets(script.getSourceTargets())
        //.setPermissions(script.getPermissions())
        .setPlugin(script.getPlugin())
        .setCreatedBy(script.getCreatedBy())
        .setCreatedDate(script.getCreatedDate())
        .setLastModifiedBy(script.getLastModifiedBy())
        .setLastModifiedDate(script.getLastModifiedDate());
  }


  @NotNull
  public static ApiLocaleResult<PageResult<ScriptListVo>> assembleAllowImportSampleStatus(
      PageResult<ScriptListVo> result) {
    ApiLocaleResult<PageResult<ScriptListVo>> apiResult = ApiLocaleResult.success(result);
    Object queryAll = PrincipalContext.getExtension("queryAllEmpty");
    if (result.isEmpty() && nonNull(queryAll) && (boolean) queryAll) {
      apiResult.getExt().put("allowImportSamples", true);
    }
    return apiResult;
  }

  @NotNull
  public static ApiLocaleResult<PageResult<ScriptInfoListVo>> assembleInfoAllowImportSampleStatus(
      PageResult<ScriptInfoListVo> result) {
    ApiLocaleResult<PageResult<ScriptInfoListVo>> apiResult = ApiLocaleResult.success(result);
    Object queryAll = PrincipalContext.getExtension("queryAllEmpty");
    if (result.isEmpty() && nonNull(queryAll) && (boolean) queryAll) {
      apiResult.getExt().put("allowImportSamples", true);
    }
    return apiResult;
  }

  public static GenericSpecification<ScriptInfo> getSpecification(ScriptFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate")
        .orderByFields("id", "name", "platform", "type", "source", "plugin",
            "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate")
        .inAndNotFields("id", "type", "source", "sourceId", "tag", "createdBy")
        .matchSearchFields("name", "description", "extSearchMerge", "plugin")
        .build();
    return new GenericSpecification<>(filters);
  }

}
