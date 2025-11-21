package cloud.xcan.angus.core.tester.interfaces.config.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeDomainAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeDomainAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeDomainAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeDomainAssembler.updateDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.config.NodeDomainCmd;
import cloud.xcan.angus.core.tester.application.query.config.NodeDomainQuery;
import cloud.xcan.angus.core.tester.domain.config.node.domain.NodeDomain;
import cloud.xcan.angus.core.tester.interfaces.config.facade.NodeDomainFacade;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.domain.NodeDomainAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.domain.NodeDomainFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.domain.NodeDomainUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeDomainAssembler;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.domain.NodeDomainDetailVo;
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
    return nodeDomainCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(NodeDomainUpdateDto dto) {
    nodeDomainCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public void delete(Long id) {
    nodeDomainCmd.delete(id);
  }

  @Override
  public NodeDomainDetailVo detail(Long id) {
    return toDetailVo(nodeDomainQuery.find(id));
  }

  @Override
  public PageResult<NodeDomainDetailVo> list(NodeDomainFindDto dto) {
    Page<NodeDomain> page = nodeDomainQuery.find(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, NodeDomainAssembler::toDetailVo);
  }
}




