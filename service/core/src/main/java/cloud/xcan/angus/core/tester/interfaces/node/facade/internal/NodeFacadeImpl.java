package cloud.xcan.angus.core.tester.interfaces.node.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.core.tester.interfaces.node.facade.internal.assembler.NodeAssembler.completeNodeDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.node.facade.internal.assembler.NodeAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.node.facade.internal.assembler.NodeInfoAssembler.getSpecification;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isApi;
import static cloud.xcan.angus.remote.ApiConstant.RLimit.MAX_PAGE_SIZE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getExtension;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.angus.api.tester.node.dto.NodeCountFindDto;
import cloud.xcan.angus.api.tester.node.dto.NodeFindDto;
import cloud.xcan.angus.api.tester.node.dto.NodeOnlinePurchaseDto;
import cloud.xcan.angus.api.tester.node.dto.NodeRenewDto;
import cloud.xcan.angus.api.tester.node.vo.NodeDetailVo;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeCmd;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeQuery;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.NodeFacade;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeAddDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeInfoFindDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodePurchaseDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeTestDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.internal.assembler.NodeAssembler;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeMockServiceListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.remote.dto.EnabledOrDisabledDto;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class NodeFacadeImpl implements NodeFacade {

  @Resource
  private NodeCmd nodeCmd;

  @Resource
  private NodeQuery nodeQuery;

  @Resource
  private NodeInfoQuery nodeInfoQuery;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Override
  public List<IdKey<Long, Object>> add(List<NodeAddDto> dto) {
    List<Node> nodes = dto.stream().map(NodeAssembler::addDtoToDomain)
        .toList();
    return nodeCmd.add(nodes);
  }

  @Override
  public void update(List<NodeUpdateDto> dto) {
    List<Node> nodes = dto.stream().map(NodeAssembler::updateDtoToDomain)
        .toList();
    nodeCmd.update(nodes);
  }

  @Override
  public void rename(Long id, String name) {
    nodeCmd.rename(id, name.trim());
  }

  @Override
  public void stop(HashSet<Long> ids) {
    nodeCmd.stop(ids);
  }

  @Override
  public void restart(LinkedHashSet<Long> ids) {
    nodeCmd.restart(ids);
  }

  @Override
  public void enabled(LinkedHashSet<EnabledOrDisabledDto> dto) {
    List<Node> nodes = dto.stream()
        .map(NodeAssembler::enabledDtoToDomain).toList();
    nodeCmd.enabled(nodes);
  }

  @Override
  public void delete(Collection<Long> ids) {
    nodeCmd.delete(NodeAssembler.deletesDtoToDomain(ids));
  }

  @Override
  public void purchase(NodeOnlinePurchaseDto dto) {
    nodeCmd.purchase(dto.getOrderId(), dto.getTenantId());
  }

  @Override
  public void purchase(NodePurchaseDto dto) {
    Node node = NodeAssembler.purchaseDtoToDomain(dto);
    nodeCmd.purchase(node, dto.getNodeNum());
  }

  @Override
  public void renew(NodeRenewDto dto) {
    nodeCmd.renew(dto.getOrderId(), dto.getOriginalOrderId(), dto.getTenantId());
  }

  @Override
  public AgentInstallCmd agentInstall(Long id) {
    return nodeCmd.agentInstall(id);
  }

  @Override
  public void agentRestart(Long id) {
    nodeCmd.agentRestart(id);
  }

  @Override
  public void test(NodeTestDto dto) {
    nodeCmd.testConnectionNodeConfig(dto.getIp(), dto.getSshPort(),
        dto.getUsername(), dto.getPassword());
  }

  @Override
  public Long count(NodeCountFindDto dto) {
    return nodeQuery.count(NodeAssembler.getCountSpecification(dto));
  }

  @Override
  public List<NodeMockServiceListVo> mockServiceList(Long id) {
    List<MockServiceInfo> mockServiceInfos = mockServiceQuery.findByNodeId(id);
    return mockServiceInfos.stream().map(NodeAssembler::toServiceListVo)
        .toList();
  }

  @NameJoin
  @Override
  public NodeDetailVo detail(Long id) {
    Node node = nodeQuery.detail(id);
    NodeInfo nodeInfo = null;
    try {
      nodeInfo = nodeInfoQuery.detail(id, node.isFreeNode());
    } catch (Exception e) {
      // If 404 do Nothing
    }
    NodeDetailVo nodeDetailVo = toDetailVo(node, isAdmin());
    completeNodeDetailVo(nodeDetailVo, nodeInfo);
    return nodeDetailVo;
  }

  @NameJoin
  @Override
  public PageResult<NodeDetailVo> list(NodeFindDto dto) {
    Page<Node> page = nodeQuery.list(NodeAssembler.getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    boolean isAdmin = isAdmin();
    PageResult<NodeDetailVo> detailPage = buildVoPageResult(page, x -> toDetailVo(x, isAdmin));
    if (nonNull(page) && !page.hasContent()) {
      return detailPage;
    }
    // Ignore door api
    if (isApi()) {
      completePageNodeInfo(detailPage);
    }
    return detailPage;
  }

  private void completePageNodeInfo(PageResult<NodeDetailVo> nodeDetailVos) {
    String idIn = nodeDetailVos.getList().stream().map(NodeDetailVo::getId)
        .map(String::valueOf).collect(Collectors.joining(","));
    boolean isFreeNodes = nonNull(getExtension("isFreeNodes"))
        && Boolean.parseBoolean(getExtension("isFreeNodes").toString());
    if (isNotEmpty(idIn)) {
      NodeInfoFindDto infoFindDto = new NodeInfoFindDto();
      infoFindDto.setPageSize(MAX_PAGE_SIZE);
      infoFindDto.setFilters(singletonList(SearchCriteria.newBuilder().key("id")
          .op(SearchOperation.IN).value(idIn).build()));
      if (isFreeNodes) {
        infoFindDto.setIsFreeNode(isFreeNodes);
      }
      Page<NodeInfo> infoPage = nodeInfoQuery.list(getSpecification(infoFindDto), infoFindDto.tranPage());
      for (NodeDetailVo detailVo : nodeDetailVos.getList()) {
        for (NodeInfo info : infoPage.getContent()) {
          if (info.getId().equals(detailVo.getId())) {
            completeNodeDetailVo(detailVo, info);
          }
        }
      }
    }
  }

}
