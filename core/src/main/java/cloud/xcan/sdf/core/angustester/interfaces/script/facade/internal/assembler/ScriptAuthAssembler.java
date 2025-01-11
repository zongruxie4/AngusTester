package cloud.xcan.sdf.core.angustester.interfaces.script.facade.internal.assembler;

import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.script.auth.ScriptAuth;
import cloud.xcan.sdf.core.angustester.domain.script.auth.ScriptAuthCurrent;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth.ScriptAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth.ScriptAuthDeptDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth.ScriptAuthGroupDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth.ScriptAuthUserDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth.ScriptAuthVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

public class ScriptAuthAssembler {

  public static ScriptAuth addDtoToDomain(Long id, ScriptAuthAddDto dto) {
    Set<ScriptPermission> permissions = new HashSet<>();
    permissions.add(ScriptPermission.VIEW);
    if (ObjectUtils.isNotEmpty(dto.getPermissions())) {
      permissions.addAll(dto.getPermissions());
    }
    return new ScriptAuth().setScriptId(id).setCreatorFlag(false)
        .setAuthObjectType(dto.getAuthObjectType())
        .setAuthObjectId(dto.getAuthObjectId())
        .setAuths(new ArrayList<>(permissions));
  }

  public static ScriptAuth replaceDtoToDomain(Long id, ScriptAuthReplaceDto dto) {
    dto.getPermissions().add(ScriptPermission.VIEW);
    return new ScriptAuth().setId(id).setAuths(new ArrayList<>(dto.getPermissions()));
  }

  public static ScriptAuthVo toDetailVo(ScriptAuth scriptAuth) {
    ScriptAuthVo authVo;
    switch (scriptAuth.getAuthObjectType()) {
      case USER:
        authVo = new ScriptAuthUserDetailVo();
        break;
      case GROUP:
        authVo = new ScriptAuthGroupDetailVo();
        break;
      case DEPT:
        authVo = new ScriptAuthDeptDetailVo();
        break;
      default:
        throw ResourceNotFound.of(scriptAuth.getAuthObjectType().getMessage());
    }
    authVo.setId(scriptAuth.getId());
    authVo.setPermissions(scriptAuth.getAuths());
    authVo.setAuthObjectType(scriptAuth.getAuthObjectType());
    authVo.setAuthObjectId(scriptAuth.getAuthObjectId());
    authVo.setCreatorFlag(scriptAuth.getCreatorFlag());
    authVo.setScriptId(scriptAuth.getScriptId());
    return authVo;
  }

  public static ScriptAuthCurrentVo toAuthCurrentVo(ScriptAuthCurrent authCurrent) {
    return new ScriptAuthCurrentVo()
        .setScriptAuthFlag(authCurrent.isScriptAuthFlag())
        .setPermissions(authCurrent.getPermissions());
  }

  public static Specification<ScriptAuth> getSpecification(ScriptAuthFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .inAndNotFields("id", "scriptId", "authObjectId", "createdDate")
        .orderByFields("id", "scriptId", "authObjectId", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}
