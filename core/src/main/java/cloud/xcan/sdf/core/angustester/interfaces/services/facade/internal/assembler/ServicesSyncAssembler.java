package cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal.assembler;

import cloud.xcan.sdf.core.angustester.domain.services.sync.ServicesSync;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.config.ServicesSyncReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.config.ServicesSyncDetailVo;

public class ServicesSyncAssembler {

  public static ServicesSync toDomain(Long serviceId, ServicesSyncReplaceDto dto) {
    return new ServicesSync()
        .setServiceId(serviceId)
        .setName(dto.getName())
        .setApiDocsUrl(dto.getApiDocsUrl())
        .setDeleteWhenNotExisted(dto.getDeleteWhenNotExisted())
        .setStrategyWhenDuplicated(dto.getStrategyWhenDuplicated())
        .setAuths(dto.getAuths());
  }

  public static ServicesSyncDetailVo toDetailVo(ServicesSync sync) {
    return new ServicesSyncDetailVo()
        .setServiceId(sync.getServiceId())
        .setName(sync.getName())
        .setApiDocsUrl(sync.getApiDocsUrl())
        .setDeleteWhenNotExisted(sync.getDeleteWhenNotExisted())
        .setStrategyWhenDuplicated(sync.getStrategyWhenDuplicated())
        .setAuths(sync.getAuths())
        .setSyncSuccessFlag(sync.getSyncSuccessFlag())
        .setSyncFailureCause(sync.getSyncFailureCause())
        .setLastSyncDate(sync.getLastSyncDate())
        .setLastModifiedBy(sync.getLastModifiedBy())
        .setLastModifiedDate(sync.getLastModifiedDate());
  }

}