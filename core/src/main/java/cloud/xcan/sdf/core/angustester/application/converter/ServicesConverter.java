package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.spring.SpringContextHolder.getCachedUidGenerator;

import cloud.xcan.sdf.api.commonlink.ApisTargetType;
import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.core.angustester.domain.apis.trash.ApisTrash;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.summary.ServicesSummary;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import java.time.LocalDateTime;
import org.springframework.beans.BeanUtils;

public class ServicesConverter {

  public static Services cloneService(Long newId, Services serviceDb) {
    Services cloneProject = new Services();
    BeanUtils.copyProperties(serviceDb, cloneProject);
    cloneProject.setId(newId);
    cloneProject.setStatus(ApiStatus.UNKNOWN);
    return cloneProject;
  }

  public static Services toNewImportService(Long projectId, String name,
      ApiImportSource importSource) {
    return new Services()
        .setId(getCachedUidGenerator().getUID())
        .setProjectId(projectId)
        .setName(name)
        .setAuthFlag(false)
        .setStatus(ApiStatus.UNKNOWN)
        .setSource(ApiSource.IMPORT)
        .setImportSource(importSource);
  }

  public static void completeDeleteInfo(Services serviceDb) {
    serviceDb.setDeletedBy(getUserId());
    serviceDb.setDeletedFlag(true);
    LocalDateTime deleteDate = LocalDateTime.now();
    serviceDb.setDeletedDate(deleteDate);
  }

  public static ApisTrash toTrash(Services serviceDb) {
    return new ApisTrash()
        .setProjectId(serviceDb.getProjectId())
        .setServicesId(serviceDb.getId())
        .setTargetType(ApisTargetType.SERVICE)
        .setTargetId(serviceDb.getId())
        .setTargetName(serviceDb.getName())
        .setCreatedBy(serviceDb.getCreatedBy())
        .setDeletedBy(getUserId())
        .setDeletedDate(LocalDateTime.now());
  }

  public static ServicesSummary toServicesSummary(Services services) {
    return new ServicesSummary()
        .setId(services.getId())
        .setProjectId(services.getProjectId())
        .setSource(services.getSource())
        .setImportSource(services.getImportSource())
        .setName(services.getName())
        .setAuthFlag(services.getAuthFlag())
        .setStatus(services.getStatus())
        .setApisNum(services.getApisNum())
        .setApisCaseNum(services.getApisCaseNum())
        .setCreatedBy(services.getCreatedBy())
        .setCreatedDate(services.getCreatedDate())
        .setLastModifiedBy(services.getLastModifiedBy())
        .setLastModifiedDate(services.getLastModifiedDate());
  }
}
