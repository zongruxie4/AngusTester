package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.tester.domain.node.NodeListRepo;
import cloud.xcan.angus.core.tester.domain.node.NodeSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * @author XiaoLong Liu
 */
@Repository
public class NodeSearchRepoMysql extends AbstractSearchRepository<Node> implements NodeSearchRepo {

  @Resource
  private NodeListRepo nodeListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<Node> mainClz,
      Object[] params, String... matches) {
    return nodeListRepo.getSqlTemplate0(getSearchMode(), mainClz, criteria, "node", matches);
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return nodeListRepo.getReturnFieldsCondition(criteria, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }

}
