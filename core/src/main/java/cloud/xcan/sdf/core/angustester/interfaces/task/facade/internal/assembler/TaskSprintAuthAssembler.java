package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.auth.TaskSprintAuth;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.auth.TaskSprintAuthCurrent;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthDeptDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthGroupDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthUserDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TaskSprintAuthAssembler {

  public static TaskSprintAuth addDtoToDomain(Long sprintId, TaskSprintAuthAddDto dto) {
    Set<TaskSprintPermission> permissions = new HashSet<>();
    if (ObjectUtils.isNotEmpty(dto.getPermissions())) {
      permissions.addAll(dto.getPermissions());
    }
    return new TaskSprintAuth()
        .setSprintId(sprintId).setCreatorFlag(false)
        .setAuthObjectType(dto.getAuthObjectType())
        .setAuthObjectId(dto.getAuthObjectId())
        .setAuths(new ArrayList<>(permissions));
  }

  public static TaskSprintAuth replaceDtoToDomain(Long id, TaskSprintAuthReplaceDto dto) {
    return new TaskSprintAuth().setId(id).setAuths(new ArrayList<>(dto.getPermissions()));
  }

  public static TaskSprintAuthVo toDetailVo(TaskSprintAuth dirAuth) {
    TaskSprintAuthVo authVo;
    switch (dirAuth.getAuthObjectType()) {
      case USER:
        authVo = new TaskSprintAuthUserDetailVo();
        break;
      case GROUP:
        authVo = new TaskSprintAuthGroupDetailVo();
        break;
      case DEPT:
        authVo = new TaskSprintAuthDeptDetailVo();
        break;
      default:
        throw ResourceNotFound.of(dirAuth.getAuthObjectType().getMessage());
    }
    authVo.setId(dirAuth.getId());
    authVo.setPermissions(dirAuth.getAuths());
    authVo.setAuthObjectType(dirAuth.getAuthObjectType());
    authVo.setAuthObjectId(dirAuth.getAuthObjectId());
    authVo.setCreatorFlag(dirAuth.getCreatorFlag());
    authVo.setSprintId(dirAuth.getSprintId());
    return authVo;
  }

  public static TaskSprintAuthCurrentVo toAuthCurrentVo(TaskSprintAuthCurrent authCurrent) {
    return new TaskSprintAuthCurrentVo()
        .setTaskSprintAuthFlag(authCurrent.isTaskSprintAuthFlag())
        .setPermissions(authCurrent.getPermissions());
  }

  public static GenericSpecification<TaskSprintAuth> getSpecification(TaskSprintAuthFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .inAndNotFields("id", "sprintId")
        .orderByFields("id", "sprintId", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}
