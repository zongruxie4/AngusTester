package cloud.xcan.angus.core.tester.interfaces.node.facade;

import cloud.xcan.angus.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.angus.api.tester.node.dto.NodeCountFindDto;
import cloud.xcan.angus.api.tester.node.dto.NodeFindDto;
import cloud.xcan.angus.api.tester.node.dto.NodeOnlinePurchaseDto;
import cloud.xcan.angus.api.tester.node.dto.NodeRenewDto;
import cloud.xcan.angus.api.tester.node.vo.NodeDetailVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeAddDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodePurchaseDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeTestDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeMockServiceListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.remote.dto.EnabledOrDisabledDto;
import cloud.xcan.angus.spec.experimental.IdKey;
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

}
