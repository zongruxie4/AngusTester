package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollow;
import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollowP;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.follow.ApisFollowDetailVo;


public class ApisFollowAssembler {

  public static ApisFollow addDtoToDomain(Long apiId) {
    return new ApisFollow().setApisId(apiId);
  }

  public static ApisFollowDetailVo toDetailVo(ApisFollowP apisFollowP) {
    return new ApisFollowDetailVo().setId(apisFollowP.getId())
        .setApisId(apisFollowP.getApisId())
        .setApisName(apisFollowP.getApisName())
        .setMethod(apisFollowP.getMethod())
        .setApisUri(apisFollowP.getApisUri());
  }

}




