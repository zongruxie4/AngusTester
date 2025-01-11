package cloud.xcan.sdf.core.angustester.application.converter;

import cloud.xcan.sdf.core.angustester.domain.services.sync.ServicesSync;

public class ServicesSyncConverter {

  public static ServicesSync setReplaceInfo(ServicesSync syncDb, ServicesSync projectSync) {
    return syncDb/*.setName(projectSync.getName())*/
        .setApiDocsUrl(projectSync.getApiDocsUrl())
        .setStrategyWhenDuplicated(projectSync.getStrategyWhenDuplicated())
        .setDeleteWhenNotExisted(projectSync.getDeleteWhenNotExisted())
        .setAuths(projectSync.getAuths());
  }

}
