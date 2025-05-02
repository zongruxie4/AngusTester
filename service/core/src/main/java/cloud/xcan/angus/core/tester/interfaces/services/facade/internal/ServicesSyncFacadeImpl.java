package cloud.xcan.angus.core.tester.interfaces.services.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesSyncAssembler.toDomain;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesSyncCmd;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSyncQuery;
import cloud.xcan.angus.core.tester.domain.services.sync.ServicesSync;
import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesSyncFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.config.ServicesSyncReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.config.ServicesSyncTestDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesSyncAssembler;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.config.ServicesSyncDetailVo;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ServicesSyncFacadeImpl implements ServicesSyncFacade {

  @Resource
  private ServicesSyncCmd servicesSyncCmd;

  @Resource
  private ServicesSyncQuery servicesSyncQuery;

  @Override
  public void replace(Long serviceId, ServicesSyncReplaceDto dto) {
    servicesSyncCmd.replace(serviceId, toDomain(serviceId, dto));
  }

  @Override
  public void replaceAll(Long serviceId, List<ServicesSyncReplaceDto> dto) {
    servicesSyncCmd.replaceAll(serviceId,
        dto.stream().map(x -> toDomain(serviceId, x)).collect(Collectors.toList()));
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
    return isEmpty(syncs) ? null : syncs.stream().map(ServicesSyncAssembler::toDetailVo)
        .collect(Collectors.toList());
  }

}
