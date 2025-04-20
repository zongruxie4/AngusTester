package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuth;
import cloud.xcan.angus.idgen.UidGenerator;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import java.util.Collections;

public class ServicesAuthConverter {

  public static ServicesAuth toServicesViewPermission(Long serviceId,
      AuthObjectType authObjectType, Long authObjectId) {
    return new ServicesAuth()
        .setId(getBean(CachedUidGenerator.class).getUID())
        .setServiceId(serviceId)
        .setAuthObjectType(authObjectType)
        .setAuthObjectId(authObjectId)
        .setCreator(false)
        .setAuths(Collections.singletonList(ServicesPermission.VIEW));
  }

  public static ServicesAuth toServicesCreatorAuth(Long creatorId, Long serviceId,
      UidGenerator uidGenerator) {
    return new ServicesAuth().setId(uidGenerator.getUID())
        .setServiceId(serviceId)
        .setAuthObjectType(AuthObjectType.USER)
        .setAuthObjectId(creatorId)
        .setAuths(ServicesPermission.ALL)
        .setCreator(true);
  }

}
