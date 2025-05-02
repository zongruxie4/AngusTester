package cloud.xcan.angus.core.tester.application.query.exec;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ExecSampleExtcQuery {

  Page<ExecSampleContent> list(Long id, GenericSpecification<ExecSampleContent> spec,
      PageRequest pageable);

  List<ExecSampleContent> findIterationSampleContent(Long execId);
}
