package cloud.xcan.angus.core.tester.interfaces.task.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskSprintAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskSprintAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskSprintAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskSprintAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskSprintAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskSprintAssembler.updateDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskSprintCmd;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintSearch;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskSprintFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintSearchDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.TaskSprintUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskSprintAssembler;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.sprint.TaskSprintDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.HashSet;
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
    return taskSprintCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(TaskSprintUpdateDto dto) {
    taskSprintCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(TaskSprintReplaceDto dto) {
    return taskSprintCmd.replace(replaceDtoToDomain(dto));
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
    return toDetailVo(taskSprintQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<TaskSprintDetailVo> list(@Valid TaskSprintFindDto dto) {
    Page<TaskSprint> page = taskSprintQuery.find(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, TaskSprintAssembler::toDetailVo);
  }

  @NameJoin
  @Override
  public PageResult<TaskSprintDetailVo> search(@Valid TaskSprintSearchDto dto) {
    Page<TaskSprint> page = taskSprintSearch.search(getSearchCriteria(dto), dto.tranPage(),
        TaskSprint.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, TaskSprintAssembler::toDetailVo);
  }

}
