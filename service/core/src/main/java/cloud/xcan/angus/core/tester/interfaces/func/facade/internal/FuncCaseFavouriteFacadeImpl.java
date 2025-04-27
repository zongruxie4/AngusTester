package cloud.xcan.angus.core.tester.interfaces.func.facade.internal;

import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.func.FuncCaseFavouriteCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseFavouriteQuery;
import cloud.xcan.angus.core.tester.domain.func.favourite.FuncCaseFavouriteP;
import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncCaseFavouriteFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.favourite.FuncCaseFavouriteSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncCaseFavouriteAssembler;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.favourite.FuncCaseFavouriteDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class FuncCaseFavouriteFacadeImpl implements FuncCaseFavouriteFacade {

  @Resource
  private FuncCaseFavouriteCmd funcCaseFavouriteCmd;

  @Resource
  private FuncCaseFavouriteQuery funcCaseFavouriteQuery;

  @Override
  public IdKey<Long, Object> add(Long caseId) {
    return funcCaseFavouriteCmd.add(FuncCaseFavouriteAssembler.addDtoToDomain(caseId));
  }

  @Override
  public void cancel(Long caseId) {
    funcCaseFavouriteCmd.cancel(caseId);
  }

  @Override
  public void cancelAll(Long projectId) {
    funcCaseFavouriteCmd.cancelAll(projectId);
  }

  @Override
  public PageResult<FuncCaseFavouriteDetailVo> search(FuncCaseFavouriteSearchDto dto) {
    Page<FuncCaseFavouriteP> pageResult = funcCaseFavouriteQuery
        .search(dto.getProjectId(), dto.getCaseName(), dto.tranPage());
    return buildVoPageResult(pageResult, FuncCaseFavouriteAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return funcCaseFavouriteQuery.count(projectId);
  }
}




