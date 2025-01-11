package cloud.xcan.sdf.core.angustester.interfaces.node.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain.NodeDomainAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain.NodeDomainFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain.NodeDomainUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.domain.NodeDomainDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface NodeDomainFacade {

  IdKey<Long, Object> add(NodeDomainAddDto dto);

  void update(NodeDomainUpdateDto dto);

  void delete(Long id);

  NodeDomainDetailVo detail(Long id);

  PageResult<NodeDomainDetailVo> list(NodeDomainFindDto dto);
}
