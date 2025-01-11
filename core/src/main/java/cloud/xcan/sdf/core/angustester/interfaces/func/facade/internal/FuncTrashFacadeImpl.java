package cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncTrashCmd;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncTrashQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncTrashSearch;
import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrash;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncTrashFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.trash.FuncTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler.FuncTrashAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.trash.FuncTrashDetailVo;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class FuncTrashFacadeImpl implements FuncTrashFacade {

  @Resource
  private FuncTrashCmd funcTrashCmd;

  @Resource
  private FuncTrashQuery funcTrashQuery;

  @Resource
  private FuncTrashSearch funcTrashSearch;

  @Override
  public void clear(Long id) {
    funcTrashCmd.clear(id);
  }

  @Override
  public void clearAll(Long projectId) {
    funcTrashCmd.clearAll(projectId);
  }

  @Override
  public void back(Long id) {
    funcTrashCmd.back(id);
  }

  @Override
  public void backAll(Long projectId) {
    funcTrashCmd.backAll(projectId);
  }

  @Override
  public Long count(Long projectId) {
    return funcTrashQuery.count(projectId);
  }

  @Override
  public PageResult<FuncTrashDetailVo> search(FuncTrashSearchDto dto) {
    Page<FuncTrash> funcTrashPage = funcTrashSearch
        .search(FuncTrashAssembler.getSearchCriteria(dto), dto.tranPage(), FuncTrash.class,
            getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(funcTrashPage, FuncTrashAssembler::toDetailVo);
  }
}