package cloud.xcan.angus.core.tester.domain.issue.count;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkloadOverview extends AbstractOverview{

  private Map<Long, UserInfo> assignees = new HashMap<>();

  private WorkloadCount totalOverview = new WorkloadCount();

  private Map<Long, WorkloadCount> assigneesOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<WorkloadDetail> dataDetails = new ArrayList<>();

}
