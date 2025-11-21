package cloud.xcan.angus.core.tester.interfaces.node.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.api.tester.node.vo.NodeInfoDetailVo;
import cloud.xcan.angus.api.tester.node.vo.NodeInfoDetailVo.InfoVo;
import cloud.xcan.angus.api.tester.node.vo.NodeInfoDetailVo.OsVo;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.domain.config.node.info.Info;
import cloud.xcan.angus.core.tester.domain.config.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.config.node.info.Os;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeInfoFindDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeExecVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Objects;
import java.util.Set;

public class NodeInfoAssembler {

  public static NodeInfoDetailVo toNodeInfoDetailVo(NodeInfo nodeInfo) {
    return new NodeInfoDetailVo().setId(nodeInfo.getId())
        .setAgentInstalled(nodeInfo.getAgentInstalled())
        .setAgentOnline(nodeInfo.getAgentOnline())
        .setLastModifiedDate(nodeInfo.getLastModifiedDate())
        .setInfo(Objects.nonNull(nodeInfo.getInfo()) ? toInfoVo(nodeInfo.getInfo()) : null)
        .setOs(Objects.nonNull(nodeInfo.getOs()) ? toOsVo(nodeInfo.getOs()) : null);
  }

  public static InfoVo toInfoVo(Info info) {
    return isNull(info) ? null : new InfoVo().setPlatform(info.getPlatform())
        .setCpuNum(info.getCpuNum())
        .setCpuPhysicalNum(info.getCpuPhysicalNum())
        .setMemTotal(info.getMemTotal())
        .setSwapTotal(info.getSwapTotal())
        .setFsTotal(info.getFsTotal())
        .setNetworkSpeed(info.getNetworkSpeed())
        .setMaxOpenFiles(info.getMaxOpenFiles())
        .setStackSize(info.getStackSize());
  }

  public static OsVo toOsVo(Os os) {
    return isNull(os) ? null : new OsVo().setName(os.getName())
        .setVersion(os.getVersion())
        .setArch(os.getArch())
        .setMachine(os.getMachine())
        .setVendor(os.getVendor());
  }

  public static NodeExecVo toNodeExecInfo(ExecInfo exec) {
    return new NodeExecVo()
        .setId(exec.getId())
        .setName(exec.getName())
        .setPlugin(exec.getPlugin())
        .setScriptType(exec.getScriptType())
        .setStatus(exec.getStatus())
        .setPriority(exec.getPriority())
        .setActualStartDate(exec.getActualStartDate())
        .setExecBy(exec.getExecBy())
        .setCreatedBy(exec.getCreatedBy())
        .setCreatedDate(exec.getCreatedDate());
  }

  public static GenericSpecification<NodeInfo> getSpecification(NodeInfoFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "lastModifiedDate")
        .orderByFields("id", "lastModifiedDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}
