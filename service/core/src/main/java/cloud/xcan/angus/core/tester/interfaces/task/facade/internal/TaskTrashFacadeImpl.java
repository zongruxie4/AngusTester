package cloud.xcan.angus.core.tester.interfaces.task.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskTrashAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.task.TaskTrashCmd;
import cloud.xcan.angus.core.tester.application.query.task.TaskTrashQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskTrashSearch;
import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrash;
import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.trash.TaskTrashSearchDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskTrashAssembler;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.trash.TaskTrashDetailVo;
import cloud.xcan.angus.remote.PageResult;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskTrashFacadeImpl implements TaskTrashFacade {

  @Resource
  private TaskTrashCmd taskTrashCmd;

  @Resource
  private TaskTrashQuery taskTrashQuery;

  @Resource
  private TaskTrashSearch taskTrashSearch;

  @Override
  public void clear(Long id) {
    taskTrashCmd.clear(id);
  }

  @Override
  public void clearAll(Long projectId) {
    taskTrashCmd.clearAll(projectId);
  }

  @Override
  public void back(Long id) {
    taskTrashCmd.back(id);
  }

  @Override
  public void backAll(Long projectId) {
    taskTrashCmd.backAll(projectId);
  }

  @Override
  public Long count(Long projectId) {
    return taskTrashQuery.count(projectId);
  }

  @Override
  public PageResult<TaskTrashDetailVo> search(TaskTrashSearchDto dto) {
    Page<TaskTrash> page = taskTrashSearch.search(getSearchCriteria(dto), dto.tranPage(),
        TaskTrash.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, TaskTrashAssembler::toDetailVo);
  }

}




