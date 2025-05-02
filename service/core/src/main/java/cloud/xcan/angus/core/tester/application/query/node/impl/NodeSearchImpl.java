package cloud.xcan.angus.core.tester.application.query.node.impl;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isInnerApi;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isTenantClient;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.node.NodeQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeSearch;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.tester.domain.node.NodeSearchRepo;
import cloud.xcan.angus.core.utils.PrincipalContextUtils;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class NodeSearchImpl implements NodeSearch {

  @Resource
  private NodeSearchRepo nodeSearchRepo;

  @Resource
  private NodeQuery nodeQuery;

  @Override
  public Page<Node> search(Set<SearchCriteria> criteria, Pageable pageable, Class<Node> clz,
      String... matches) {
    return new BizTemplate<Page<Node>>(true, true) {

      @Override
      protected Page<Node> process() {
        if (isTenantClient()) {
          criteria.add(SearchCriteria.equal("deleted", false));
        }

        Page<Node> page = null;
        Long tenantId0 = getTenantId(criteria);
        if (nodeQuery.hasOwnNodes(tenantId0)) {
          page = nodeSearchRepo.find(criteria, pageable, clz, matches);
        } else if (isUserAction()) {
          PrincipalContext.addExtension("isFreeNodes", true);
          page = nodeQuery.getFreeWhenNonNodes(findFirstValue(criteria, "role"));
        }

        if (nonNull(page) && page.hasContent()) {
          nodeQuery.setNodeRoles(page.getContent());
        }
        return page;
      }
    }.execute();
  }

  private Long getTenantId(Set<SearchCriteria> criteria) {
    Object tenantId = PrincipalContextUtils.isInnerApi() ? findFirstValue(criteria, "tenantId") : null;
    return nonNull(tenantId) ? Long.valueOf(tenantId.toString()) : getOptTenantId();
  }

}
