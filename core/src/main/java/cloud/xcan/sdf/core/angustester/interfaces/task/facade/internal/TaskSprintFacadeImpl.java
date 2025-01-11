package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskSprintCmd;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSprintQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSprintSearch;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskSprintFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskSprintAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.TaskSprintDetailVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskSprintFacadeImpl implements TaskSprintFacade {

  @Resource
  private TaskSprintCmd taskSprintCmd;

  @Resource
  private TaskSprintQuery taskSprintQuery;

  @Resource
  private TaskSprintSearch taskSprintSearch;

  @Override
  public IdKey<Long, Object> add(TaskSprintAddDto dto) {
    return taskSprintCmd.add(TaskSprintAssembler.addDtoToDomain(dto));
  }

  @Override
  public void update(TaskSprintUpdateDto dto) {
    taskSprintCmd.update(TaskSprintAssembler.updateDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(TaskSprintReplaceDto dto) {
    return taskSprintCmd.replace(TaskSprintAssembler.replaceDtoToDomain(dto));
  }

  @Override
  public void start(Long id) {
    taskSprintCmd.start(id);
  }

  @Override
  public void end(Long id) {
    taskSprintCmd.end(id);
  }

  @Override
  public void block(Long id) {
    taskSprintCmd.block(id);
  }

  @Override
  public IdKey<Long, Object> clone(Long id) {
    return taskSprintCmd.clone(id);
  }

  @Override
  public void move(Long id, Long projectId) {
    taskSprintCmd.move(id, projectId);
  }

  @Override
  public void restart(HashSet<Long> ids) {
    taskSprintCmd.restart(ids);
  }

  @Override
  public void reopen(HashSet<Long> ids) {
    taskSprintCmd.reopen(ids);
  }

  @Override
  public void delete(Long id) {
    taskSprintCmd.delete(id);
  }

  @NameJoin
  @Override
  public TaskSprintDetailVo detail(Long id) {
    TaskSprint sprint = taskSprintQuery.detail(id);
    return TaskSprintAssembler.toDetailVo(sprint);
  }

  @NameJoin
  @Override
  public PageResult<TaskSprintDetailVo> list(@Valid TaskSprintFindDto dto) {
    Page<TaskSprint> page = taskSprintQuery
        .find(TaskSprintAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, TaskSprintAssembler::toDetailVo);
  }

  @NameJoin
  @Override
  public PageResult<TaskSprintDetailVo> search(@Valid TaskSprintSearchDto dto) {
    Page<TaskSprint> page = taskSprintSearch
        .search(TaskSprintAssembler.getSearchCriteria(dto), dto.tranPage(), TaskSprint.class,
            getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, TaskSprintAssembler::toDetailVo);
  }

}
