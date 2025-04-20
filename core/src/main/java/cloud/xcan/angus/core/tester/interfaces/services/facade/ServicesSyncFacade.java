package cloud.xcan.angus.core.tester.interfaces.services.facade;

import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.config.ServicesSyncReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.config.ServicesSyncTestDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.config.ServicesSyncDetailVo;
import java.util.List;
import java.util.Set;

public interface ServicesSyncFacade {

  void replace(Long serviceId, ServicesSyncReplaceDto dto);

  void replaceAll(Long serviceId, List<ServicesSyncReplaceDto> dtos);

  void sync(Long serviceId, String name);

  void test(ServicesSyncTestDto dto);

  void delete(Long serviceId, Set<String> names);

  List<ServicesSyncDetailVo> list(Long serviceId);

}
