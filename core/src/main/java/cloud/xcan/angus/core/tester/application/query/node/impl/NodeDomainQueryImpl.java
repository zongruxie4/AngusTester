package cloud.xcan.angus.core.tester.application.query.node.impl;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.DOMAIN_NAME_REPEATED_T;

import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.core.tester.application.query.node.NodeDomainQuery;
import cloud.xcan.angus.core.tester.domain.node.dns.DomainDnsNum;
import cloud.xcan.angus.core.tester.domain.node.dns.NodeDomainDnsRepo;
import cloud.xcan.angus.core.tester.domain.node.domain.NodeDomain;
import cloud.xcan.angus.core.tester.domain.node.domain.NodeDomainRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

@Biz
public class NodeDomainQueryImpl implements NodeDomainQuery {

  @Resource
  private NodeDomainRepo nodeDomainRepo;

  @Resource
  private NodeDomainDnsRepo nodeDomainDnsRepo;

  @Override
  public NodeDomain find(Long id) {
    return new BizTemplate<NodeDomain>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected NodeDomain process() {
        NodeDomain domain = nodeDomainRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "NodeRole"));
        setDnsNum(Collections.singletonList(domain));
        return domain;
      }
    }.execute();
  }

  @Override
  public Page<NodeDomain> find(Specification<NodeDomain> spec, Pageable pageable) {
    return new BizTemplate<Page<NodeDomain>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<NodeDomain> process() {
        Page<NodeDomain> page = nodeDomainRepo.findAll(spec, pageable);
        if (page.hasContent()) {
          setDnsNum(page.getContent());
        }
        return page;
      }
    }.execute();
  }

  @Override
  public NodeDomain checkAndFind(Long id) {
    return nodeDomainRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "NodeRole"));
  }

  @Override
  public NodeDomain checkAndFind(String name) {
    return nodeDomainRepo.findByName(name).orElseThrow(() -> ResourceNotFound.of(name, "NodeRole"));
  }

  @Override
  public void checkAddNameExists(String name) {
    long nameCount = nodeDomainRepo.countByName(name);
    ProtocolAssert.assertResourceExisted(nameCount < 1,
        DOMAIN_NAME_REPEATED_T, new Object[]{name});
  }

  @Override
  public void checkUpdateNameExists(Long id, String name) {
    long nameCount = nodeDomainRepo.countByNameAndIdNot(name, id);
    ProtocolAssert.assertResourceExisted(nameCount < 1,
        DOMAIN_NAME_REPEATED_T, new Object[]{name});
  }

  @Override
  public void setDnsNum(List<NodeDomain> domains) {
    if (CollectionUtils.isEmpty(domains)) {
      return;
    }
    Map<Long, DomainDnsNum> serviceApiNumMap = nodeDomainDnsRepo
        .selectDnsNumByDomainIdIn(domains.stream().map(NodeDomain::getId)
            .collect(Collectors.toSet())).stream()
        .collect(Collectors.toMap(DomainDnsNum::getDomainId, x -> x));
    for (NodeDomain s : domains) {
      DomainDnsNum domainDnsNum = serviceApiNumMap.get(s.getId());
      s.setDnsNum(Objects.isNull(domainDnsNum) ? 0 : domainDnsNum.getDnsNum());
    }
  }
}
