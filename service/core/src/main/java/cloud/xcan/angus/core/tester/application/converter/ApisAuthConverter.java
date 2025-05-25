package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuth;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.idgen.UidGenerator;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author XiaoLong Liu
 */
public class ApisAuthConverter {

  public static List<ApisAuth> toApisCreatorAuth(Apis apis, Set<Long> userIds) {
    List<ApisAuth> apisAuths = new ArrayList<>();
    UidGenerator uidGenerator = getBean(CachedUidGenerator.class);
    for (Long userId : userIds) {
      ApisAuth apisAuth = new ApisAuth();
      apisAuth.setId(uidGenerator.getUID());
      apisAuth.setApisId(apis.getId());
      apisAuth.setAuthObjectId(userId);
      apisAuth.setCreatedBy(userId);
      apisAuth.setTenantId(apis.getTenantId());
      apisAuth.setAuthObjectType(AuthObjectType.USER);
      apisAuth.setAuths(ApiPermission.ALL);
      apisAuth.setCreator(true);
      apisAuths.add(apisAuth);
    }
    return apisAuths;
  }

  public static List<ApisAuth> toApisCreatorAuth(ApisBaseInfo apis, Set<Long> userIds) {
    List<ApisAuth> apisAuths = new ArrayList<>();
    UidGenerator uidGenerator = getBean(CachedUidGenerator.class);
    for (Long userId : userIds) {
      ApisAuth apisAuth = new ApisAuth();
      apisAuth.setId(uidGenerator.getUID());
      apisAuth.setApisId(apis.getId());
      apisAuth.setAuthObjectId(userId);
      apisAuth.setCreatedBy(userId);
      apisAuth.setTenantId(apis.getTenantId());
      apisAuth.setAuthObjectType(AuthObjectType.USER);
      apisAuth.setAuths(ApiPermission.ALL);
      apisAuth.setCreator(true);
      apisAuths.add(apisAuth);
    }
    return apisAuths;
  }

  public static ApisAuth toApisCreatorAuth(Long apisId, Long creatorId, UidGenerator uidGenerator) {
    return new ApisAuth().setId(uidGenerator.getUID())
        .setApisId(apisId)
        .setAuthObjectType(AuthObjectType.USER)
        .setAuthObjectId(creatorId)
        .setAuths(ApiPermission.ALL)
        .setCreator(true);
  }

  public static List<ApisAuth> toApisCreatorAuth(List<Services> projects, List<Apis> apis) {
    List<ApisAuth> apisAuths = new ArrayList<>();
    for (Services project : projects) {
      for (Apis api : apis) {
        ApisAuth apisAuth = new ApisAuth();
        apisAuth.setId(getBean(CachedUidGenerator.class).getUID());
        apisAuth.setApisId(api.getId());
        apisAuth.setAuthObjectId(project.getCreatedBy());
        apisAuth.setCreatedBy(project.getCreatedBy());
        apisAuth.setTenantId(api.getTenantId());
        apisAuth.setAuthObjectType(AuthObjectType.USER);
        apisAuth.setAuths(ApiPermission.ALL);
        apisAuth.setCreator(true);
        apisAuths.add(apisAuth);
      }
    }
    return apisAuths;
  }
}
