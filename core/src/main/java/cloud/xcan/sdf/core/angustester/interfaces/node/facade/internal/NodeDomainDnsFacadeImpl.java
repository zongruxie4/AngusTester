package cloud.xcan.sdf.core.angustester.interfaces.node.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.node.NodeDomainDnsCmd;
import cloud.xcan.sdf.core.angustester.application.query.node.NodeDomainDnsQuery;
import cloud.xcan.sdf.core.angustester.domain.node.dns.NodeDomainDns;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.NodeDomainDnsFacade;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.dns.NodeDomainDnsAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.dns.NodeDomainDnsFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.dns.NodeDomainDnsUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.internal.assembler.NodeDomainDnsAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.dns.NodeDomainDnsDetailVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class NodeDomainDnsFacadeImpl implements NodeDomainDnsFacade {

  @Resource
  private NodeDomainDnsCmd nodeDomainDnsCmd;

  @Resource
  private NodeDomainDnsQuery nodeDomainDnsQuery;

  @Override
  public IdKey<Long, Object> add(NodeDomainDnsAddDto dto) {
    NodeDomainDns nodeDomainDns = NodeDomainDnsAssembler.addDtoToDomain(dto);
    return nodeDomainDnsCmd.add(nodeDomainDns);
  }

  @Override
  public void update(NodeDomainDnsUpdateDto dto) {
    NodeDomainDns nodeDomainDns = NodeDomainDnsAssembler.updateDtoToDomain(dto);
    nodeDomainDnsCmd.update(nodeDomainDns);
  }

  @Override
  public void delete(Long id) {
    nodeDomainDnsCmd.delete(id);
  }

  @NameJoin
  @Override
  public PageResult<NodeDomainDnsDetailVo> list(NodeDomainDnsFindDto dto) {
    Page<NodeDomainDns> nodeDomainDnsPage = nodeDomainDnsQuery.find(
        NodeDomainDnsAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(nodeDomainDnsPage, NodeDomainDnsAssembler::toDetailVo);
  }

}
