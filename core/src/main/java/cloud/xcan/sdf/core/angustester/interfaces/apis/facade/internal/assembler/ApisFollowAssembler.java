package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler;

import cloud.xcan.sdf.core.angustester.domain.apis.follow.ApisFollow;
import cloud.xcan.sdf.core.angustester.domain.apis.follow.ApisFollowP;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.follow.ApisFollowDetailVo;


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




