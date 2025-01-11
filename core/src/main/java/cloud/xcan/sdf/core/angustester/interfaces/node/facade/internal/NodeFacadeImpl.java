package cloud.xcan.sdf.core.angustester.interfaces.node.facade.internal;

import static cloud.xcan.sdf.api.ApiConstant.RLimit.MAX_PAGE_SIZE;
import static cloud.xcan.sdf.core.angustester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.sdf.core.angustester.interfaces.node.facade.internal.assembler.NodeAssembler.completeNodeDetailVo;
import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angusctrl.node.NodeInfoRemote;
import cloud.xcan.sdf.api.angusctrl.node.dto.NodeInfoFindDto;
import cloud.xcan.sdf.api.angusctrl.node.vo.NodeInfoDetailVo;
import cloud.xcan.sdf.api.angustester.node.dto.NodeCountFindDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeFindDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeOnlinePurchaseDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeRenewDto;
import cloud.xcan.sdf.api.angustester.node.vo.NodeDetailVo;
import cloud.xcan.sdf.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.sdf.api.dto.EnabledOrDisabledDto;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.api.search.SearchOperation;
import cloud.xcan.sdf.core.angustester.application.cmd.node.NodeCmd;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceQuery;
import cloud.xcan.sdf.core.angustester.application.query.node.NodeQuery;
import cloud.xcan.sdf.core.angustester.application.query.node.NodeSearch;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceInfo;
import cloud.xcan.sdf.core.angustester.domain.node.Node;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.NodeFacade;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodePurchaseDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeTestDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.internal.assembler.NodeAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.NodeMockServiceListVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class NodeFacadeImpl implements NodeFacade {

  @Resource
  private NodeCmd nodeCmd;

  @Resource
  private NodeQuery nodeQuery;

  @Resource
  private NodeSearch nodeSearch;

  @Resource
  private NodeInfoRemote nodeInfoRemote;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Override
  public List<IdKey<Long, Object>> add(List<NodeAddDto> dto) {
    List<Node> nodes = dto.stream().map(NodeAssembler::addDtoToDomain)
        .collect(Collectors.toList());
    return nodeCmd.add(nodes);
  }

  @Override
  public void update(List<NodeUpdateDto> dto) {
    List<Node> nodes = dto.stream().map(NodeAssembler::updateDtoToDomain)
        .collect(Collectors.toList());
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
        .map(NodeAssembler::enabledDtoToDomain).collect(Collectors.toList());
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
        dto.getUsername(), dto.getPassd());
  }

  @Override
  public Long count(NodeCountFindDto dto) {
    return nodeQuery.count(NodeAssembler.getCountSpecification(dto));
  }

  @Override
  public List<NodeMockServiceListVo> mockServiceList(Long id) {
    List<MockServiceInfo> mockServiceInfos = mockServiceQuery.findByNodeId(id);
    return mockServiceInfos.stream().map(NodeAssembler::toServiceListVo)
        .collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public NodeDetailVo detail(Long id) {
    Node node = nodeQuery.detail(id);
    NodeInfoDetailVo infoDetailVo = null;
    try {
      infoDetailVo = nodeInfoRemote.detail(id, node.isFreeNode()).orElseContentThrow();
    } catch (Exception e) {
      // If 404 do Nothing
    }
    NodeDetailVo nodeDetailVo = NodeAssembler.toDetailVo(node, isAdmin());
    completeNodeDetailVo(nodeDetailVo, infoDetailVo);
    return nodeDetailVo;
  }

  @NameJoin
  @Override
  public PageResult<NodeDetailVo> list(NodeFindDto dto) {
    Page<Node> nodePage = nodeQuery.find(NodeAssembler.getSpecification(dto), dto.tranPage());
    boolean isAdmin = isAdmin();
    PageResult<NodeDetailVo> nodeDetailVos = buildVoPageResult(nodePage,
        x -> NodeAssembler.toDetailVo(x, isAdmin));
    if (nonNull(nodePage) && !nodePage.hasContent()) {
      return nodeDetailVos;
    }
    // Ignore door api
    if (PrincipalContext.isApi()) {
      completePageNodeInfo(nodeDetailVos);
    }
    return nodeDetailVos;
  }

  @NameJoin
  @Override
  public PageResult<NodeDetailVo> search(NodeSearchDto dto) {
    Page<Node> nodePage = nodeSearch.search(NodeAssembler.getSearchCriteria(dto),
        dto.tranPage(), Node.class, getMatchSearchFields(dto.getClass()));
    boolean isAdmin = isAdmin();
    PageResult<NodeDetailVo> nodeDetailVos =
        buildVoPageResult(nodePage, x -> NodeAssembler.toDetailVo(x, isAdmin));
    if (nonNull(nodePage) && !nodePage.hasContent()) {
      return nodeDetailVos;
    }
    completePageNodeInfo(nodeDetailVos);
    return nodeDetailVos;
  }

  private void completePageNodeInfo(PageResult<NodeDetailVo> nodeDetailVos) {
    String idIn = nodeDetailVos.getList().stream().map(NodeDetailVo::getId)
        .map(String::valueOf).collect(Collectors.joining(","));
    boolean isFreeNodes = nonNull(PrincipalContext.getExtension("isFreeNodes"))
        && Boolean.parseBoolean(PrincipalContext.getExtension("isFreeNodes").toString());
    if (isNotEmpty(idIn)) {
      NodeInfoFindDto infoFindDto = new NodeInfoFindDto();
      infoFindDto.setPageSize(MAX_PAGE_SIZE);
      infoFindDto.setFilters(singletonList(SearchCriteria.newBuilder().key("id")
          .op(SearchOperation.IN).value(idIn).build()));
      if (isFreeNodes) {
        infoFindDto.setIsFreeNodeFlag(isFreeNodes);
      }
      PageResult<NodeInfoDetailVo> infoDetailVoPage = nodeInfoRemote.list(infoFindDto)
          .orElseContentThrow();
      for (NodeDetailVo nodeDetailVo : nodeDetailVos.getList()) {
        for (NodeInfoDetailVo infoDetailVo : infoDetailVoPage.getList()) {
          if (infoDetailVo.getId().equals(nodeDetailVo.getId())) {
            completeNodeDetailVo(nodeDetailVo, infoDetailVo);
          }
        }
      }
    }
  }

}
