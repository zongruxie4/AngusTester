package cloud.xcan.sdf.core.angustester.interfaces.node.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.node.dto.NodeCountFindDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeFindDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeOnlinePurchaseDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeRenewDto;
import cloud.xcan.sdf.api.angustester.node.vo.NodeDetailVo;
import cloud.xcan.sdf.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.sdf.api.dto.EnabledOrDisabledDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodePurchaseDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeTestDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.NodeMockServiceListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public interface NodeFacade {

  List<IdKey<Long, Object>> add(List<NodeAddDto> dto);

  void update(List<NodeUpdateDto> dto);

  void rename(Long id, String name);

  void stop(HashSet<Long> ids);

  void restart(LinkedHashSet<Long> ids);

  void enabled(LinkedHashSet<EnabledOrDisabledDto> dto);

  void delete(Collection<Long> ids);

  void test(NodeTestDto dto);

  void purchase(NodeOnlinePurchaseDto dto);

  void purchase(NodePurchaseDto dto);

  void renew(NodeRenewDto dto);

  AgentInstallCmd agentInstall(Long id);

  void agentRestart(Long id);

  NodeDetailVo detail(Long id);

  Long count(NodeCountFindDto dto);

  List<NodeMockServiceListVo> mockServiceList(Long id);

  PageResult<NodeDetailVo> list(NodeFindDto dto);

  PageResult<NodeDetailVo> search(NodeSearchDto dto);


}
