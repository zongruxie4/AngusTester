package cloud.xcan.sdf.core.angustester.application.cmd.activity;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import java.util.Collection;
import java.util.List;

public interface ActivityCmd {

  void add(Activity activity);

  void addAll(Collection<Activity> activities);

  void deleteByTarget(CombinedTargetType targetType, List<Long> targetIds);

  void deleteByTaskIds(List<Long> taskIds);

}




