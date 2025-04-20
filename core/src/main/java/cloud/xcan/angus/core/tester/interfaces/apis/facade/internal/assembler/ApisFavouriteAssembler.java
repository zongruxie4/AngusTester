package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavourite;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteP;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.favourite.ApisFavouriteDetailVo;


public class ApisFavouriteAssembler {

  public static ApisFavourite addDtoToDomain(Long apisId) {
    return new ApisFavourite().setApisId(apisId);
  }

  public static ApisFavouriteDetailVo toDetailVo(ApisFavouriteP apisFavouriteP) {
    return new ApisFavouriteDetailVo().setId(apisFavouriteP.getId())
        .setApisId(apisFavouriteP.getApisId())
        .setApisName(apisFavouriteP.getApisName())
        .setMethod(apisFavouriteP.getMethod())
        .setApisUri(apisFavouriteP.getApisUri());
  }

}




