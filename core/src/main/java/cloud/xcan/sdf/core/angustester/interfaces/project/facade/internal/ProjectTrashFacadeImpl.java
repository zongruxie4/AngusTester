package cloud.xcan.sdf.core.angustester.interfaces.project.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.project.ProjectTrashCmd;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectTrashQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectTrashSearch;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrash;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.ProjectTrashFacade;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.trash.ProjectTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.internal.assembler.ProjectTrashAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.vo.trash.ProjectTrashDetailVo;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProjectTrashFacadeImpl implements ProjectTrashFacade {

  @Resource
  private ProjectTrashCmd projectTrashCmd;

  @Resource
  private ProjectTrashQuery projectTrashQuery;

  @Resource
  private ProjectTrashSearch projectTrashSearch;

  @Override
  public void clear(Long id) {
    projectTrashCmd.clear(id);
  }

  @Override
  public void clearAll() {
    projectTrashCmd.clearAll();
  }

  @Override
  public void back(Long id) {
    projectTrashCmd.back(id);
  }

  @Override
  public void backAll() {
    projectTrashCmd.backAll();
  }

  @Override
  public Long count() {
    return projectTrashQuery.count();
  }

  @Override
  public PageResult<ProjectTrashDetailVo> search(ProjectTrashSearchDto dto) {
    Page<ProjectTrash> alTrashPage = projectTrashSearch
        .search(ProjectTrashAssembler.getSearchCriteria(dto), dto.tranPage(), ProjectTrash.class,
            getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(alTrashPage, ProjectTrashAssembler::toDetailVo);
  }

}




