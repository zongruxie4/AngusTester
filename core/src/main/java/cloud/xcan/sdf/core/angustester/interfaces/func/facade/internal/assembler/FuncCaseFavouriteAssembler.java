package cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler;

import cloud.xcan.sdf.core.angustester.domain.func.favourite.FuncCaseFavourite;
import cloud.xcan.sdf.core.angustester.domain.func.favourite.FuncCaseFavouriteP;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.favourite.FuncCaseFavouriteDetailVo;


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