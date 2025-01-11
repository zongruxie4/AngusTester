package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.node.Node;
import cloud.xcan.sdf.core.angustester.domain.node.NodeListRepo;
import cloud.xcan.sdf.core.angustester.domain.node.NodeSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import javax.annotation.Resource;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

/**
 * @author xiaolong.liu
 */
@Repository
public class NodeSearchRepoMysql extends AbstractSearchRepository<Node> implements NodeSearchRepo {

  @Resource
  private NodeListRepo nodeListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... matches) {
    return nodeListRepo.getSqlTemplate0(getSearchMode(), step, criterias, "node", matches);
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params) {
    return nodeListRepo.getReturnFieldsCondition(criterias, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }

}
