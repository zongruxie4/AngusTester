package cloud.xcan.angus.core.tester.application.query.node.impl;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.DOMAIN_DNS_NAME_REPEATED_T;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.tester.application.query.node.NodeDomainDnsQuery;
import cloud.xcan.angus.core.tester.domain.node.dns.NodeDomainDns;
import cloud.xcan.angus.core.tester.domain.node.dns.NodeDomainDnsRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Biz
public class NodeDomainDnsQueryImpl implements NodeDomainDnsQuery {

  @Resource
  private NodeDomainDnsRepo nodeDomainDnsRepo;

  @Override
  public NodeDomainDns find(Long id) {
    return new BizTemplate<NodeDomainDns>() {

      @Override
      protected NodeDomainDns process() {
        return nodeDomainDnsRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "NodeDomainDns"));
      }
    }.execute();
  }

  @Override
  public Page<NodeDomainDns> find(Specification<NodeDomainDns> spec, Pageable pageable) {
    return new BizTemplate<Page<NodeDomainDns>>() {

      @Override
      protected Page<NodeDomainDns> process() {
        return nodeDomainDnsRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public NodeDomainDns checkAndFind(Long id) {
    return nodeDomainDnsRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "NodeDomainDns"));
  }

  @Override
  public void checkAddNameExists(Long domainId, String name) {
    // Include logic deleted project
    Long nameCount = nodeDomainDnsRepo.countByDomainIdAndName(domainId, name);
    ProtocolAssert.assertResourceExisted(nameCount < 1,
        DOMAIN_DNS_NAME_REPEATED_T, new Object[]{name});
  }

  @Override
  public void checkUpdateNameExists(Long id, Long domainId, String name) {
    // Include logic deleted project
    Long nameCount = nodeDomainDnsRepo.countByDomainIdAndNameAndIdNot(domainId, name, id);
    ProtocolAssert.assertResourceExisted(nameCount < 1,
        DOMAIN_DNS_NAME_REPEATED_T, new Object[]{name});
  }
}




