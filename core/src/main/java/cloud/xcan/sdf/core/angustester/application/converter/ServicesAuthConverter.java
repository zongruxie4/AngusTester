package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.spring.SpringContextHolder.getBean;

import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.services.auth.ServicesAuth;
import cloud.xcan.sdf.idgen.UidGenerator;
import cloud.xcan.sdf.idgen.uid.impl.CachedUidGenerator;
import java.util.Collections;

public class ServicesAuthConverter {

  public static ServicesAuth toServicesViewPermission(Long serviceId,
      AuthObjectType authObjectType, Long authObjectId) {
    return new ServicesAuth()
        .setId(getBean(CachedUidGenerator.class).getUID())
        .setServiceId(serviceId)
        .setAuthObjectType(authObjectType)
        .setAuthObjectId(authObjectId)
        .setCreatorFlag(false)
        .setAuths(Collections.singletonList(ServicesPermission.VIEW));
  }

  public static ServicesAuth toServicesCreatorAuth(Long creatorId, Long serviceId,
      UidGenerator uidGenerator) {
    return new ServicesAuth().setId(uidGenerator.getUID())
        .setServiceId(serviceId)
        .setAuthObjectType(AuthObjectType.USER)
        .setAuthObjectId(creatorId)
        .setAuths(ServicesPermission.ALL)
        .setCreatorFlag(true);
  }

}
