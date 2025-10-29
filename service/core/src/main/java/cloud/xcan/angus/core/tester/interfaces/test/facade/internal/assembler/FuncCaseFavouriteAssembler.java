package cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.test.favourite.FuncCaseFavourite;
import cloud.xcan.angus.core.tester.domain.test.favourite.FuncCaseFavouriteP;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.favourite.FuncCaseFavouriteDetailVo;


public class FuncCaseFavouriteAssembler {

  public static FuncCaseFavourite addDtoToDomain(Long caseId) {
    return new FuncCaseFavourite().setCaseId(caseId);
  }

  public static FuncCaseFavouriteDetailVo toDetailVo(FuncCaseFavouriteP favourite) {
    return new FuncCaseFavouriteDetailVo()
        .setId(favourite.getId())
        .setCaseId(favourite.getCaseId())
        .setCaseName(favourite.getCaseName())
        .setProjectId(favourite.getProjectId())
        .setPlanId(favourite.getPlanId())
        .setCode(favourite.getCode());
  }

}
