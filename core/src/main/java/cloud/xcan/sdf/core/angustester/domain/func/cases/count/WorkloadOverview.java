package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.core.angustester.domain.task.count.AbstractOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.WorkloadCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.WorkloadDetail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkloadOverview extends AbstractOverview {

  private Map<Long, UserInfo> testers = new HashMap<>();

  private WorkloadCount totalOverview = new WorkloadCount();

  private Map<Long, WorkloadCount> testersOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<WorkloadDetail> dataDetails = new ArrayList<>();

}
