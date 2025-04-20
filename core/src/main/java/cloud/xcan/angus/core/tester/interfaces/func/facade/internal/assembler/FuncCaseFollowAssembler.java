package cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.func.follow.FuncCaseFollow;
import cloud.xcan.angus.core.tester.domain.func.follow.FuncCaseFollowP;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.follow.FuncCaseFollowDetailVo;


public class FuncCaseFollowAssembler {

  public static FuncCaseFollow addDtoToDomain(Long caseId) {
    return new FuncCaseFollow().setCaseId(caseId);
  }

  public static FuncCaseFollowDetailVo toDetailVo(FuncCaseFollowP follow) {
    return new FuncCaseFollowDetailVo().setId(follow.getId())
        .setCaseId(follow.getCaseId())
        .setCaseName(follow.getCaseName())
        .setProjectId(follow.getProjectId())
        .setPlanId(follow.getPlanId())
        .setCode(follow.getCode());
  }

}
