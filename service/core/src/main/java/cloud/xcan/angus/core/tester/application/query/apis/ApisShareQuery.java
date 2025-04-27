package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.apis.share.ApisShare;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisShareQuery {

  ApisShare detail(Long id);

  Page<ApisShare> find(GenericSpecification<ApisShare> spec, PageRequest pageable);

  ApisShare view(Long sid, String pat);

  ApisShare checkAndFind(Long id);

  List<ApisShare> checkAndFind(Collection<Long> ids);

}
