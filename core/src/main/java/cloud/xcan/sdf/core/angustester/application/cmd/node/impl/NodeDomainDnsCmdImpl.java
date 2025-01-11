package cloud.xcan.sdf.core.angustester.application.cmd.node.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.enums.NormalStatus;
import cloud.xcan.sdf.core.angustester.application.cmd.node.NodeDomainDnsCmd;
import cloud.xcan.sdf.core.angustester.application.query.node.NodeDomainDnsQuery;
import cloud.xcan.sdf.core.angustester.application.query.node.NodeDomainQuery;
import cloud.xcan.sdf.core.angustester.domain.node.dns.NodeDomainDns;
import cloud.xcan.sdf.core.angustester.domain.node.dns.NodeDomainDnsRepo;
import cloud.xcan.sdf.core.angustester.domain.node.domain.NodeDomain;
import cloud.xcan.sdf.core.angustester.infra.iaas.DnsClient;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.utils.CoreUtils;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
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
      protected void checkParams() {
        // NOOP
      }

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




