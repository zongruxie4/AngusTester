package cloud.xcan.angus.core.tester.application.query.exec.impl;

import static cloud.xcan.angus.core.tester.infra.metricsds.MetricsDynamicDataSourceAspect.DEFAULT_SHARDING_KEY;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleErrorContentQuery;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCause;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCauseRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ExecSampleErrorContentQueryImpl implements ExecSampleErrorContentQuery {

  @Resource
  private ExecSampleErrorCauseRepo execSampleErrorContentRepo;

  @Override
  public Page<ExecSampleErrorCause> list(Long id,
      GenericSpecification<ExecSampleErrorCause> spec, PageRequest pageable) {
    return new BizTemplate<Page<ExecSampleErrorCause>>() {

      @Override
      protected Page<ExecSampleErrorCause> process() {
        spec.getCriteria().add(SearchCriteria.equal("execId", id));
        spec.getCriteria().add(SearchCriteria.equal(DEFAULT_SHARDING_KEY, getOptTenantId()));
        return execSampleErrorContentRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public List<ExecSampleErrorCause> findErrorContent(Long id) {
    return execSampleErrorContentRepo.findByExecId(id);
  }
}
