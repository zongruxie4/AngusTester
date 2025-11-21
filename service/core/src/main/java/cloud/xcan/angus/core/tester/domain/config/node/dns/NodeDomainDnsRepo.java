package cloud.xcan.angus.core.tester.domain.config.node.dns;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NodeDomainDnsRepo extends BaseRepository<NodeDomainDns, Long> {

  Long countByDomainIdAndName(Long domainId, String name);

  Long countByDomainIdAndNameAndIdNot(Long domainId, String name, Long id);

  @Query(value = "SELECT count(a.id) as dnsNum, a.domainId as domainId FROM NodeDomainDns a WHERE a.domainId IN ?1 GROUP BY a.domainId")
  List<DomainDnsNum> selectDnsNumByDomainIdIn(Set<Long> domainIds);

}
