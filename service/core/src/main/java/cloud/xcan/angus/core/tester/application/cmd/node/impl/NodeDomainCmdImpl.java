package cloud.xcan.angus.core.tester.application.cmd.node.impl;

import cloud.xcan.angus.api.enums.NormalStatus;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeDomainCmd;
import cloud.xcan.angus.core.tester.application.query.node.NodeDomainQuery;
import cloud.xcan.angus.core.tester.domain.node.domain.NodeDomain;
import cloud.xcan.angus.core.tester.domain.node.domain.NodeDomainRepo;
import cloud.xcan.angus.core.tester.infra.iaas.DnsClient;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class NodeDomainCmdImpl extends CommCmd<NodeDomain, Long> implements NodeDomainCmd {

  @Resource
  private NodeDomainRepo nodeDomainRepo;

  @Resource
  private NodeDomainQuery nodeDomainQuery;

  @Resource
  private DnsClient dnsClient;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(NodeDomain domain) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        nodeDomainQuery.checkAddNameExists(domain.getName());
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (dnsClient.addDomain(domain.getName())) {
          domain.setStatus(NormalStatus.NORMAL);
        } else {
          domain.setStatus(NormalStatus.EXCEPTION);
        }
        return insert(domain);
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(NodeDomain domain) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        nodeDomainQuery.checkUpdateNameExists(domain.getId(), domain.getName());
      }

      @Override
      protected Void process() {
        updateOrNotFound(domain);
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
        nodeDomainRepo.deleteById(id);
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<NodeDomain, Long> getRepository() {
    return this.nodeDomainRepo;
  }
}




