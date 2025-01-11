package cloud.xcan.sdf.core.angustester.application.query.activity;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.activity.summary.ActivitySummary;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityQuery {

  Page<Activity> find(GenericSpecification<Activity> spec, Pageable pageable);

  List<ActivitySummary> findSummaryByTarget(CombinedTargetType targetType, Long targetId);
}




