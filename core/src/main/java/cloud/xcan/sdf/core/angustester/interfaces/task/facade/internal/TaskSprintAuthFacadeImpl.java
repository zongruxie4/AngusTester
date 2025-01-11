package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskSprintAuthCmd;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSprintAuthQuery;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.auth.TaskSprintAuth;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.auth.TaskSprintAuthCurrent;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskSprintAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth.TaskSprintAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskSprintAuthAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth.TaskSprintAuthVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskSprintAuthFacadeImpl implements TaskSprintAuthFacade {

  @Resource
  private TaskSprintAuthCmd taskSprintAuthCmd;

  @Resource
  private TaskSprintAuthQuery taskSprintAuthQuery;

  @Override
  public IdKey<Long, Object> add(Long sprintId, TaskSprintAuthAddDto dto) {
    return taskSprintAuthCmd.add(TaskSprintAuthAssembler.addDtoToDomain(sprintId, dto));
  }

  @Override
  public void replace(Long sprintId, TaskSprintAuthReplaceDto dto) {
    taskSprintAuthCmd.replace(TaskSprintAuthAssembler.replaceDtoToDomain(sprintId, dto));
  }

  @Override
  public void enabled(Long sprintId, Boolean enabled) {
    taskSprintAuthCmd.enabled(sprintId, enabled);
  }

  @Override
  public Boolean status(Long sprintId) {
    return taskSprintAuthQuery.status(sprintId);
  }

  @Override
  public void delete(Long sprintId) {
    taskSprintAuthCmd.delete(sprintId);
  }

  @Override
  public List<TaskSprintPermission> userAuth(Long sprintId, Long userId, Boolean adminFlag) {
    return taskSprintAuthQuery.userAuth(sprintId, userId, adminFlag);
  }

  @Override
  public TaskSprintAuthCurrentVo currentUserAuth(Long sprintId, Boolean adminFlag) {
    TaskSprintAuthCurrent authCurrent = taskSprintAuthQuery.currentUserAuth(sprintId, adminFlag);
    return TaskSprintAuthAssembler.toAuthCurrentVo(authCurrent);
  }

  @Override
  public void authCheck(Long sprintId, TaskSprintPermission permission, Long userId) {
    taskSprintAuthQuery.check(sprintId, permission, userId);
  }

  @Override
  @NameJoin
  public PageResult<TaskSprintAuthVo> list(TaskSprintAuthFindDto dto) {
    List<String> sprintIds = dto.getFilterInValue("sprintId");
    if (dto.getSprintId() != null) {
      sprintIds.add(String.valueOf(dto.getSprintId()));
    }
    Page<TaskSprintAuth> authPage = taskSprintAuthQuery
        .find(TaskSprintAuthAssembler.getSpecification(dto), sprintIds, dto.tranPage());
    return buildVoPageResult(authPage, TaskSprintAuthAssembler::toDetailVo);
  }

}
