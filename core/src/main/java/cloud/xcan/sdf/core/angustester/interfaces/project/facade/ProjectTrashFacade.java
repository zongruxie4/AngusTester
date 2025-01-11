package cloud.xcan.sdf.core.angustester.interfaces.project.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.trash.ProjectTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.vo.trash.ProjectTrashDetailVo;

public interface ProjectTrashFacade {

  void clear(Long id);

  void clearAll();

  void back(Long id);

  void backAll();

  Long count();

  PageResult<ProjectTrashDetailVo> search(ProjectTrashSearchDto dto);

}
