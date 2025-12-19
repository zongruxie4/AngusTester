package cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.services.sync.ServicesSync;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.config.ServicesSyncReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.config.ServicesSyncDetailVo;

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
        .setSyncSuccess(sync.getSyncSuccess())
        .setSyncFailureCause(sync.getSyncFailureCause())
        .setLastSyncDate(sync.getLastSyncDate())
        .setModifiedBy(sync.getModifiedBy())
        .setModifiedDate(sync.getModifiedDate());
  }

}
