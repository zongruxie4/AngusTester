package cloud.xcan.angus.core.tester.interfaces.project.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.ProjectTrashAssembler.getSpecification;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.project.ProjectTrashCmd;
import cloud.xcan.angus.core.tester.application.query.project.ProjectTrashQuery;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;
import cloud.xcan.angus.core.tester.interfaces.project.facade.ProjectTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.trash.ProjectTrashFindto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.ProjectTrashAssembler;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.trash.ProjectTrashDetailVo;
import cloud.xcan.angus.remote.PageResult;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProjectTrashFacadeImpl implements ProjectTrashFacade {

  @Resource
  private ProjectTrashCmd projectTrashCmd;

  @Resource
  private ProjectTrashQuery projectTrashQuery;

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
  public PageResult<ProjectTrashDetailVo> list(ProjectTrashFindto dto) {
    Page<ProjectTrash> page = projectTrashQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, ProjectTrashAssembler::toDetailVo);
  }

}




