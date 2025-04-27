package cloud.xcan.angus.core.tester.interfaces.node.facade.internal;

import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.node.NodeDomainCmd;
import cloud.xcan.angus.core.tester.application.query.node.NodeDomainQuery;
import cloud.xcan.angus.core.tester.domain.node.domain.NodeDomain;
import cloud.xcan.angus.core.tester.interfaces.node.facade.NodeDomainFacade;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain.NodeDomainAddDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain.NodeDomainFindDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain.NodeDomainUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.internal.assembler.NodeDomainAssembler;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.domain.NodeDomainDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class NodeDomainFacadeImpl implements NodeDomainFacade {

  @Resource
  private NodeDomainCmd nodeDomainCmd;

  @Resource
  private NodeDomainQuery nodeDomainQuery;

  @Override
  public IdKey<Long, Object> add(NodeDomainAddDto dto) {
    NodeDomain nodeDomain = NodeDomainAssembler.addDtoToDomain(dto);
    return nodeDomainCmd.add(nodeDomain);
  }

  @Override
  public void update(NodeDomainUpdateDto dto) {
    NodeDomain nodeDomain = NodeDomainAssembler.updateDtoToDomain(dto);
    nodeDomainCmd.update(nodeDomain);
  }

  @Override
  public void delete(Long id) {
    nodeDomainCmd.delete(id);
  }

  @Override
  public NodeDomainDetailVo detail(Long id) {
    NodeDomain domain = nodeDomainQuery.find(id);
    return NodeDomainAssembler.toDetailVo(domain);
  }

  @Override
  public PageResult<NodeDomainDetailVo> list(NodeDomainFindDto dto) {
    Page<NodeDomain> nodeDomainPage = nodeDomainQuery
        .find(NodeDomainAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(nodeDomainPage, NodeDomainAssembler::toDetailVo);
  }
}




