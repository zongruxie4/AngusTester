package cloud.xcan.sdf.core.angustester.application.cmd.node.impl;

import cloud.xcan.sdf.api.enums.NormalStatus;
import cloud.xcan.sdf.core.angustester.application.cmd.node.NodeDomainCmd;
import cloud.xcan.sdf.core.angustester.application.query.node.NodeDomainQuery;
import cloud.xcan.sdf.core.angustester.domain.node.domain.NodeDomain;
import cloud.xcan.sdf.core.angustester.domain.node.domain.NodeDomainRepo;
import cloud.xcan.sdf.core.angustester.infra.iaas.DnsClient;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
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




