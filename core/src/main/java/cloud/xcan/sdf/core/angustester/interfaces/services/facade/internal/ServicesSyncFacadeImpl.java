package cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal;

import cloud.xcan.sdf.core.angustester.application.cmd.services.ServicesSyncCmd;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesSyncQuery;
import cloud.xcan.sdf.core.angustester.domain.services.sync.ServicesSync;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesSyncFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.config.ServicesSyncReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.config.ServicesSyncTestDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal.assembler.ServicesSyncAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.config.ServicesSyncDetailVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ServicesSyncFacadeImpl implements ServicesSyncFacade {

  @Resource
  private ServicesSyncCmd servicesSyncCmd;

  @Resource
  private ServicesSyncQuery servicesSyncQuery;

  @Override
  public void replace(Long serviceId, ServicesSyncReplaceDto dto) {
    servicesSyncCmd.replace(serviceId, ServicesSyncAssembler.toDomain(serviceId, dto));
  }

  @Override
  public void replaceAll(Long serviceId, List<ServicesSyncReplaceDto> dtos) {
    servicesSyncCmd.replaceAll(serviceId, dtos.stream()
        .map(x -> ServicesSyncAssembler.toDomain(serviceId, x)).collect(Collectors.toList()));
  }

  @Override
  public void sync(Long serviceId, String name) {
    servicesSyncCmd.sync(serviceId, name);
  }

  @Override
  public void test(ServicesSyncTestDto dto) {
    servicesSyncCmd.test(dto.getApiDocsUrl(), dto.getAuths());
  }

  @Override
  public void delete(Long serviceId, Set<String> names) {
    servicesSyncCmd.delete(serviceId, names);
  }

  @NameJoin
  @Override
  public List<ServicesSyncDetailVo> list(Long serviceId) {
    List<ServicesSync> syncs = servicesSyncQuery.find(serviceId);
    return ObjectUtils.isEmpty(syncs) ? null : syncs.stream().map(ServicesSyncAssembler::toDetailVo)
        .collect(Collectors.toList());
  }

}
