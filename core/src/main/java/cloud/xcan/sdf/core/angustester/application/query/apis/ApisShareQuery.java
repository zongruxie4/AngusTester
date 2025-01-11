package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShare;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
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
