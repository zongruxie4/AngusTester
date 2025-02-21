package cloud.xcan.sdf.core.angustester.application.query.node.impl;

import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isDoorApi;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.node.NodeQuery;
import cloud.xcan.sdf.core.angustester.application.query.node.NodeSearch;
import cloud.xcan.sdf.core.angustester.domain.node.Node;
import cloud.xcan.sdf.core.angustester.domain.node.NodeSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import java.util.Set;
import javax.annotation.Resource;
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
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Node> process() {
        if (PrincipalContext.isTenantClient()) {
          criteria.add(SearchCriteria.equal("deletedFlag", false));
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
    Object tenantId = isDoorApi() ? findFirstValue(criteria, "tenantId") : null;
    return nonNull(tenantId) ? Long.valueOf(tenantId.toString()) : getOptTenantId();
  }

}
