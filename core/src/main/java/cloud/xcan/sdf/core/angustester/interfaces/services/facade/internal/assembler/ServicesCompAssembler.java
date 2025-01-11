package cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal.assembler;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.sdf.core.angustester.domain.services.comp.ServicesComp;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.comp.ServicesCompDetailVo;

public class ServicesCompAssembler {

  public static ServicesCompDetailVo toDetailVo(ServicesComp comp) {
    return toDetailVo(comp, false);
  }

  public static ServicesCompDetailVo toDetailVo(ServicesComp comp, Boolean ignoreModel) {
    return new ServicesCompDetailVo().setId(comp.getId())
        .setServiceId(comp.getServiceId())
        .setType(comp.getType())
        .setKey(comp.getKey())
        .setRef(comp.getRef())
        .setModel(nullSafe(ignoreModel, false) ? null : comp.getModel())
        .setResolvedRefModels(nullSafe(ignoreModel, false) ? null : comp.getResolvedRefModels())
        .setDescription(comp.getDescription())
        .setLastModifiedBy(comp.getLastModifiedBy())
        .setLastModifiedDate(comp.getLastModifiedDate());
  }

}
