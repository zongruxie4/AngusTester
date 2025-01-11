package cloud.xcan.sdf.core.angustester.interfaces.node.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.dns.NodeDomainDnsAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.dns.NodeDomainDnsFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.dns.NodeDomainDnsUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.dns.NodeDomainDnsDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface NodeDomainDnsFacade {

  IdKey<Long, Object> add(NodeDomainDnsAddDto dto);

  void update(NodeDomainDnsUpdateDto dto);

  void delete(Long id);

  PageResult<NodeDomainDnsDetailVo> list(NodeDomainDnsFindDto dto);
}
