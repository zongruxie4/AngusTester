package cloud.xcan.angus.core.tester.application.query.exec.impl;

import static cloud.xcan.angus.core.tester.infra.metricsds.MetricsDynamicDataSourceAspect.DEFAULT_SHARDING_KEY;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.metrics.ExecMetrics.EXT_KEY_SAMPLE_RESULT_CONTENT;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleExtcQuery;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContentRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ExecSampleExtcQueryImpl implements ExecSampleExtcQuery {

  @Resource
  private ExecSampleContentRepo execSampleExtcRepo;

  @Override
  public Page<ExecSampleContent> list(Long execId, GenericSpecification<ExecSampleContent> spec,
      PageRequest pageable) {
    return new BizTemplate<Page<ExecSampleContent>>() {

      @Override
      protected Page<ExecSampleContent> process() {
        spec.getCriteria().add(SearchCriteria.equal("execId", execId));
        spec.getCriteria().add(SearchCriteria.equal(DEFAULT_SHARDING_KEY, getOptTenantId()));
        return execSampleExtcRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public List<ExecSampleContent> findIterationSampleContent(Long execId){
    return execSampleExtcRepo.findAllByExecIdAndExtField(execId, EXT_KEY_SAMPLE_RESULT_CONTENT);
  }
}
