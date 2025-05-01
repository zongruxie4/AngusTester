package cloud.xcan.angus.core.tester.interfaces.func.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncCaseFollowAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.func.FuncCaseFollowCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseFollowQuery;
import cloud.xcan.angus.core.tester.domain.func.follow.FuncCaseFollowP;
import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncCaseFollowFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.follow.FuncCaseFollowSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncCaseFollowAssembler;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.follow.FuncCaseFollowDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class FuncCaseFollowFacadeImpl implements FuncCaseFollowFacade {

  @Resource
  private FuncCaseFollowCmd funcCaseFollowCmd;

  @Resource
  private FuncCaseFollowQuery funcCaseFollowQuery;

  @Override
  public IdKey<Long, Object> add(Long apiId) {
    return funcCaseFollowCmd.add(addDtoToDomain(apiId));
  }

  @Override
  public void cancel(Long caseId) {
    funcCaseFollowCmd.cancel(caseId);
  }

  @Override
  public void cancelAll(Long projectId) {
    funcCaseFollowCmd.cancelAll(projectId);
  }

  @Override
  public PageResult<FuncCaseFollowDetailVo> search(FuncCaseFollowSearchDto dto) {
    Page<FuncCaseFollowP> page = funcCaseFollowQuery
        .search(dto.getProjectId(), dto.getCaseName(), dto.tranPage());
    return buildVoPageResult(page, FuncCaseFollowAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return funcCaseFollowQuery.count(projectId);
  }

}




