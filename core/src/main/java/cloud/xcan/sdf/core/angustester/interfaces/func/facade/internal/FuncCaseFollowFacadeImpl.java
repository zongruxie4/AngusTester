package cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncCaseFollowCmd;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseFollowQuery;
import cloud.xcan.sdf.core.angustester.domain.func.follow.FuncCaseFollowP;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncCaseFollowFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.follow.FuncCaseFollowSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler.FuncCaseFollowAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.follow.FuncCaseFollowDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
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
    return funcCaseFollowCmd.add(FuncCaseFollowAssembler.addDtoToDomain(apiId));
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
    Page<FuncCaseFollowP> pageResult = funcCaseFollowQuery
        .search(dto.getProjectId(), dto.getCaseName(), dto.tranPage());
    return buildVoPageResult(pageResult, FuncCaseFollowAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return funcCaseFollowQuery.count(projectId);
  }

}




