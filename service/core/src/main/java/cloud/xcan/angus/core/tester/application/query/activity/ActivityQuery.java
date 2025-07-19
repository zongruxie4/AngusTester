package cloud.xcan.angus.core.tester.application.query.activity;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.summary.ActivitySummary;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ActivityQuery {

  Page<Activity> find(GenericSpecification<Activity> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  void setProjectName(Page<Activity> page);

  List<ActivitySummary> findSummaryByTarget(CombinedTargetType targetType, Long targetId);

  int getActivityNumByMainTarget(Long id);

}




