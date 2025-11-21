package cloud.xcan.angus.core.tester.interfaces.config.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeDomainDnsAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeDomainDnsAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeDomainDnsAssembler.updateDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.config.NodeDomainDnsCmd;
import cloud.xcan.angus.core.tester.application.query.config.NodeDomainDnsQuery;
import cloud.xcan.angus.core.tester.domain.config.node.dns.NodeDomainDns;
import cloud.xcan.angus.core.tester.interfaces.config.facade.NodeDomainDnsFacade;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.dns.NodeDomainDnsAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.dns.NodeDomainDnsFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.dns.NodeDomainDnsUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeDomainDnsAssembler;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.dns.NodeDomainDnsDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
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
    return nodeDomainDnsCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(NodeDomainDnsUpdateDto dto) {
    nodeDomainDnsCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public void delete(Long id) {
    nodeDomainDnsCmd.delete(id);
  }

  @NameJoin
  @Override
  public PageResult<NodeDomainDnsDetailVo> list(NodeDomainDnsFindDto dto) {
    Page<NodeDomainDns> page = nodeDomainDnsQuery.find(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, NodeDomainDnsAssembler::toDetailVo);
  }

}
