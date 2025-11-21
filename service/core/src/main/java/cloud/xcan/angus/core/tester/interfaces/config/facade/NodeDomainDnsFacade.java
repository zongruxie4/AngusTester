package cloud.xcan.angus.core.tester.interfaces.config.facade;

import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.dns.NodeDomainDnsAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.dns.NodeDomainDnsFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.dns.NodeDomainDnsUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.dns.NodeDomainDnsDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface NodeDomainDnsFacade {

  IdKey<Long, Object> add(NodeDomainDnsAddDto dto);

  void update(NodeDomainDnsUpdateDto dto);

  void delete(Long id);

  PageResult<NodeDomainDnsDetailVo> list(NodeDomainDnsFindDto dto);
}
