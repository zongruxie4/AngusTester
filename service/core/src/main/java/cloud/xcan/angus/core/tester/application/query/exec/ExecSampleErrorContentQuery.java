package cloud.xcan.angus.core.tester.application.query.exec;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCause;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ExecSampleErrorContentQuery {

  Page<ExecSampleErrorCause> list(Long id, GenericSpecification<ExecSampleErrorCause> spec,
      PageRequest pageable);

  List<ExecSampleErrorCause> findErrorContent(Long id);
}
