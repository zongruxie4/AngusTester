package cloud.xcan.angus.core.tester.interfaces.config.facade;

import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.domain.NodeDomainAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.domain.NodeDomainFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.domain.NodeDomainUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.domain.NodeDomainDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface NodeDomainFacade {

  IdKey<Long, Object> add(NodeDomainAddDto dto);

  void update(NodeDomainUpdateDto dto);

  void delete(Long id);

  NodeDomainDetailVo detail(Long id);

  PageResult<NodeDomainDetailVo> list(NodeDomainFindDto dto);
}
