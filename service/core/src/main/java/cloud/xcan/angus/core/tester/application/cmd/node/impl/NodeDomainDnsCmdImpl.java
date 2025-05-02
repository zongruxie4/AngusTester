package cloud.xcan.angus.core.tester.application.cmd.node.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.NormalStatus;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeDomainDnsCmd;
import cloud.xcan.angus.core.tester.application.query.node.NodeDomainDnsQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeDomainQuery;
import cloud.xcan.angus.core.tester.domain.node.dns.NodeDomainDns;
import cloud.xcan.angus.core.tester.domain.node.dns.NodeDomainDnsRepo;
import cloud.xcan.angus.core.tester.domain.node.domain.NodeDomain;
import cloud.xcan.angus.core.tester.infra.iaas.DnsClient;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class NodeDomainDnsCmdImpl extends CommCmd<NodeDomainDns, Long> implements NodeDomainDnsCmd {

  @Resource
  private NodeDomainDnsRepo nodeDomainDnsRepo;

  @Resource
  private NodeDomainDnsQuery nodeDomainDnsQuery;

  @Resource
  private NodeDomainQuery nodeDomainQuery;

  @Resource
  private DnsClient dnsClient;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(NodeDomainDns dns) {
    return new BizTemplate<IdKey<Long, Object>>() {
      NodeDomain nodeDomainDb;

      @Override
      protected void checkParams() {
        nodeDomainDb = nodeDomainQuery.checkAndFind(dns.getDomainId());
        nodeDomainDnsQuery.checkAddNameExists(dns.getDomainId(), dns.getName());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Call cloud interface to add records
        String recordId = dnsClient.addDomainDns(nodeDomainDb.getName(), dns);
        dns.setStatus(nonNull(recordId) ? NormalStatus.NORMAL : NormalStatus.EXCEPTION);
        dns.setCloudRecordId(recordId);
        // save db
        return insert(dns);
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(NodeDomainDns dns) {
    new BizTemplate<Void>() {
      private NodeDomainDns nodeDomainDnsDb;

      @Override
      protected void checkParams() {
        nodeDomainDnsDb = nodeDomainDnsQuery.checkAndFind(dns.getId());
        nodeDomainDnsQuery.checkUpdateNameExists(dns.getId(), nodeDomainDnsDb.getDomainId(),
            dns.getName());
      }

      @Override
      protected Void process() {
        CoreUtils.copyPropertiesIgnoreNull(dns, nodeDomainDnsDb);
        dnsClient.updateDomainDns(nodeDomainDnsDb);
        nodeDomainDnsRepo.save(nodeDomainDnsDb);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {


      @Override
      protected Void process() {
        NodeDomainDns nodeDomainDns = nodeDomainDnsRepo.findById(id).orElse(null);
        if (isNull(nodeDomainDns)) {
          return null;
        }

        dnsClient.deleteDomainDns(nodeDomainDns.getCloudRecordId());
        nodeDomainDnsRepo.deleteById(id);
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<NodeDomainDns, Long> getRepository() {
    return this.nodeDomainDnsRepo;
  }
}




