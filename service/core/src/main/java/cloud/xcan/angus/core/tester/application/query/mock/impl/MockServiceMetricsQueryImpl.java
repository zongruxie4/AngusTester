package cloud.xcan.angus.core.tester.application.query.mock.impl;

import static cloud.xcan.angus.api.commonlink.CtrlConstant.LATEST_LIVE_NODE_INTERVAL;

import cloud.xcan.angus.agent.message.mockservice.StatusCmdParam;
import cloud.xcan.angus.agent.message.mockservice.StatusVo;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceMetricsQuery;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsageRepo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStatusDto;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class MockServiceMetricsQueryImpl implements MockServiceMetricsQuery {

  @Resource
  private JvmServiceUsageRepo jvmServiceUsageRepo;

  @Override
  public Page<JvmServiceUsage> metrics(Long id, GenericSpecification<JvmServiceUsage> spec,
      PageRequest pageable) {
    return new BizTemplate<Page<JvmServiceUsage>>() {

      @Override
      protected Page<JvmServiceUsage> process() {
        //spec.getCriteria().add(SearchCriteria.equal("tenantId", getOptTenantId())); -> Single tenant table
        spec.getCriteria().add(SearchCriteria.equal("serviceId", id));
        return jvmServiceUsageRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public List<StatusVo> status(MockServiceStatusDto dto) {
    Set<Long> getLiveServiceIds = dto.getCmdParams().stream()
        .map(StatusCmdParam::getServiceId).collect(Collectors.toSet());
    Set<Long> liveServiceIds = getLiveServiceIds(getLiveServiceIds);
    Map<Long, StatusVo> statusVos = new HashMap<>();
    for (StatusCmdParam cmdParam : dto.getCmdParams()) {
      statusVos.put(cmdParam.getServiceId(), liveServiceIds.contains(cmdParam.getServiceId())
          ? StatusVo.success(cmdParam.getServiceId())
          : StatusVo.fail(cmdParam.getServiceId(), "No latest metrics available"));
    }
    return new ArrayList<>(statusVos.values());
  }

  @Override
  public Set<Long> getLiveServiceIds(Collection<Long> serviceIds) {
    return jvmServiceUsageRepo.findLatestIdByTimestampBeforeAndServiceIdIn(
        System.currentTimeMillis() - LATEST_LIVE_NODE_INTERVAL, serviceIds);
  }
}
