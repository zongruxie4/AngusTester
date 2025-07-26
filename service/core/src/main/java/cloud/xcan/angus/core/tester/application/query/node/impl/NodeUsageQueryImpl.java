package cloud.xcan.angus.core.tester.application.query.node.impl;

import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.remote.search.SearchCriteria.greaterThanEqual;
import static cloud.xcan.angus.remote.search.SearchCriteria.lessThanEqual;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.exec.result.NodeUsageSummary;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeUsageQuery;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsageRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsageRepo;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.utils.DateUtils;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * <p>
 * Implementation of NodeUsageQuery for querying node usage metrics and summaries.
 * </p>
 * <p>
 * Provides methods for retrieving node usage data, latest metrics, and usage summaries over a time range.
 * </p>
 */
@Slf4j
@Biz
public class NodeUsageQueryImpl implements NodeUsageQuery {

  @Resource
  private NodeUsageRepo nodeUsageRepo;
  @Resource
  private NetUsageRepo netUsageRepo;
  @Resource
  private NodeInfoQuery nodeInfoQuery;

  /**
   * <p>
   * Get paginated node usage metrics for a specific node.
   * </p>
   * <p>
   * If the tenant does not own nodes, forcibly queries metrics for free nodes.
   * </p>
   * @param id Node ID
   * @param spec Node usage search specification
   * @param pageable Pagination information
   * @return Page of NodeUsage
   */
  @Override
  public Page<NodeUsage> metrics(Long id, GenericSpecification<NodeUsage> spec,
      PageRequest pageable) {
    return new BizTemplate<Page<NodeUsage>>(false) {

      @Override
      protected Page<NodeUsage> process() {
        // spec.getCriteria().add(SearchCriteria.equal("tenantId", getOptTenantId())); -> Single tenant table
        spec.getCriteria().add(equal("nodeId", id));
        try {
          // Force to query free node metrics
          if (!nodeInfoQuery.hasOwnNodes()){
            PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
          }
          return nodeUsageRepo.findAll(spec, pageable);
        } catch (Exception e) {
          // When the node agent is not installed, querying the metrics table does not exist
          log.warn(e.getMessage());
          return Page.empty();
        }
      }
    }.execute();
  }

  /**
   * <p>
   * Get the latest node usage metric for a specific node.
   * </p>
   * <p>
   * If the tenant does not own nodes, forcibly queries metrics for free nodes.
   * </p>
   * @param id Node ID
   * @return Latest NodeUsage
   */
  @Override
  public NodeUsage metricsLatest(Long id) {
    return new BizTemplate<NodeUsage>(false) {

      @Override
      protected NodeUsage process() {
        try {
          // Force to query free node metrics
          if (!nodeInfoQuery.hasOwnNodes()){
            PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
          }
          return nodeUsageRepo.findFirstByNodeIdOrderByTimestampDesc(id);
        } catch (Exception e) {
          // When the node agent is not installed, querying the metrics table does not exist
          log.warn(e.getMessage());
          return null;
        }
      }
    }.execute();
  }

  /**
   * <p>
   * Get usage summaries for a collection of nodes within a time range.
   * </p>
   * @param nodeIds Collection of node IDs
   * @param from Start time
   * @param to End time
   * @return Map of node ID to NodeUsageSummary
   */
  public Map<Long, NodeUsageSummary> getUsageSummaries(Collection<Long> nodeIds,
      LocalDateTime from, LocalDateTime to) {
    return nodeIds.stream().collect(Collectors.toMap(x -> x, x -> getUsageSummary(x, from, to)));
  }

  /**
   * <p>
   * Get usage summary for a node within a time range.
   * </p>
   * <p>
   * Calculates mean and max CPU, memory, and filesystem usage, as well as network usage if available.
   * </p>
   * @param nodeId Node ID
   * @param from Start time
   * @param to End time
   * @return NodeUsageSummary
   */
  @Override
  public NodeUsageSummary getUsageSummary(Long nodeId, LocalDateTime from, LocalDateTime to) {
    NodeUsageSummary summary = new NodeUsageSummary();

    List<NodeUsage> nodeUsages = findAllNodeUsages(nodeId, from, to);
    if (isNotEmpty(nodeUsages)) {
      List<Double> cpuUsageRates = nodeUsages.stream().map(x -> 100 - x.getCpu().getIdle())
          .collect(Collectors.toList());
      double meanCpu = cpuUsageRates.stream().mapToDouble(Double::doubleValue).sum()
          / nodeUsages.size();
      double maxCpu = Collections.max(cpuUsageRates);
      summary.setMeanCpu(meanCpu).setMaxCpu(maxCpu);

      List<Double> memoryUsageRates = nodeUsages.stream()
          .map(x -> 100 - x.getMemory().getUsedPercent())
          .collect(Collectors.toList());
      double meanMemory = memoryUsageRates.stream().mapToDouble(Double::doubleValue).sum()
          / nodeUsages.size();
      double maxMemory = Collections.max(memoryUsageRates);
      summary.setMeanMemory(meanMemory).setMaxMemory(maxMemory);

      List<Double> fsUsageRates = nodeUsages.stream()
          .map(x -> 100 - x.getFilesystem().getUsedPercent())
          .collect(Collectors.toList());
      double meanFs = fsUsageRates.stream().mapToDouble(Double::doubleValue).sum()
          / nodeUsages.size();
      double maxFs = Collections.max(fsUsageRates);
      summary.setMeanFilesystem(meanFs).setMaxFilesystem(maxFs);
    }

    Map<String, List<NetUsage>> netUsagesMap = findAllMetUsages(nodeId, from, to);
    if (isNotEmpty(netUsagesMap)) {
      // Note: Currently, only one network device is supported.
      List<Double> netUsageRates = netUsagesMap.entrySet().stream()
          .findFirst().get().getValue().stream()
          // The sum of sending and receiving
          .map(x -> x.getNetwork().getRxBytesRate() + x.getNetwork().getTxBytesRate())
          .collect(Collectors.toList());
      double meanCpu = netUsageRates.stream().mapToDouble(Double::doubleValue).sum()
          / nodeUsages.size();
      double maxCpu = Collections.max(netUsageRates);
      summary.setMeanCpu(meanCpu).setMaxCpu(maxCpu);
    }
    return summary;
  }

  /**
   * <p>
   * Find all node usage records for a node within a time range, paginated.
   * </p>
   * @param nodeId Node ID
   * @param from Start time
   * @param to End time
   * @return List of NodeUsage
   */
  private List<NodeUsage> findAllNodeUsages(Long nodeId, LocalDateTime from, LocalDateTime to) {
    int page = 0, pageSize = 500;
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Direction.ASC, "timestamp"));
    GenericSpecification<NodeUsage> spec = new GenericSpecification<>();
    spec.getCriteria().add(equal("nodeId", nodeId));
    spec.getCriteria().add(greaterThanEqual("timestamp", DateUtils.asDate(from).getTime()));
    spec.getCriteria().add(lessThanEqual("timestamp", DateUtils.asDate(to).getTime()));
    List<NodeUsage> nodeUsages = new ArrayList<>();
    Page<NodeUsage> nodeUsagePage;
    do {
      nodeUsagePage = nodeUsageRepo.findAll(spec, pageable);
      pageable = nodeUsagePage.nextPageable();
      nodeUsages.addAll(nodeUsagePage.getContent());
    } while (nodeUsagePage.hasNext());
    return nodeUsages;
  }

  /**
   * <p>
   * Find all network usage records for a node within a time range, grouped by device name.
   * </p>
   * @param nodeId Node ID
   * @param from Start time
   * @param to End time
   * @return Map of device name to list of NetUsage
   */
  private Map<String, List<NetUsage>> findAllMetUsages(Long nodeId, LocalDateTime from,
      LocalDateTime to) {
    int page = 0, pageSize = 500;
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Direction.ASC, "timestamp"));
    GenericSpecification<NetUsage> spec = new GenericSpecification<>();
    spec.getCriteria().add(equal("nodeId", nodeId));
    spec.getCriteria().add(greaterThanEqual("timestamp", DateUtils.asDate(from).getTime()));
    spec.getCriteria().add(lessThanEqual("timestamp", DateUtils.asDate(to).getTime()));
    List<NetUsage> netUsages = new ArrayList<>();
    Page<NetUsage> netUsagePage;
    do {
      netUsagePage = netUsageRepo.findAll(spec, pageable);
      pageable = netUsagePage.nextPageable();
      netUsages.addAll(netUsagePage.getContent());
    } while (netUsagePage.hasNext());
    return netUsages.stream().collect(Collectors.groupingBy(NetUsage::getDeviceName));
  }
}
