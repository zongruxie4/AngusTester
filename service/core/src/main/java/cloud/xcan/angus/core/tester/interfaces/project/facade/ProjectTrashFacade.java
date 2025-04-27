package cloud.xcan.angus.core.tester.interfaces.project.facade;

import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.trash.ProjectTrashSearchDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.trash.ProjectTrashDetailVo;
import cloud.xcan.angus.remote.PageResult;

public interface ProjectTrashFacade {

  void clear(Long id);

  void clearAll();

  void back(Long id);

  void backAll();

  Long count();

  PageResult<ProjectTrashDetailVo> search(ProjectTrashSearchDto dto);

}
