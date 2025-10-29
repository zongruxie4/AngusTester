package cloud.xcan.angus.core.tester.interfaces.test.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncTrashAssembler.getSpecification;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.func.FuncTrashCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncTrashQuery;
import cloud.xcan.angus.core.tester.domain.test.trash.FuncTrash;
import cloud.xcan.angus.core.tester.interfaces.test.facade.FuncTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.trash.FuncTrashFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncTrashAssembler;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.trash.FuncTrashDetailVo;
import cloud.xcan.angus.remote.PageResult;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class FuncTrashFacadeImpl implements FuncTrashFacade {

  @Resource
  private FuncTrashCmd funcTrashCmd;

  @Resource
  private FuncTrashQuery funcTrashQuery;

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
  public PageResult<FuncTrashDetailVo> list(FuncTrashFindDto dto) {
    Page<FuncTrash> page = funcTrashQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, FuncTrashAssembler::toDetailVo);
  }
}
