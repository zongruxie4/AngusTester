package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskTrashCmd;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskTrashQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskTrashSearch;
import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrash;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskTrashFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.trash.TaskTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskTrashAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.trash.TaskTrashDetailVo;
import javax.annotation.Resource;
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
    Page<TaskTrash> alTrashPage = taskTrashSearch
        .search(TaskTrashAssembler.getSearchCriteria(dto), dto.tranPage(), TaskTrash.class,
            getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(alTrashPage, TaskTrashAssembler::toDetailVo);
  }

}




