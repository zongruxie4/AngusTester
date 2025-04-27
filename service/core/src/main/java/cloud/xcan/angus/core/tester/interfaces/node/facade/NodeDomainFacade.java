package cloud.xcan.angus.core.tester.interfaces.node.facade;

import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain.NodeDomainAddDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain.NodeDomainFindDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain.NodeDomainUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.domain.NodeDomainDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface NodeDomainFacade {

  IdKey<Long, Object> add(NodeDomainAddDto dto);

  void update(NodeDomainUpdateDto dto);

  void delete(Long id);

  NodeDomainDetailVo detail(Long id);

  PageResult<NodeDomainDetailVo> list(NodeDomainFindDto dto);
}
