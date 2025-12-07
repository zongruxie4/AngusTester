package cloud.xcan.angus.core.tester.domain.issue.count;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecentDeliveryOverview extends AbstractOverview {

  private Map<Long, UserInfo> assignees = new HashMap<>();

  private Map<String, RecentDeliveryCount> totalOverview = new HashMap<>();

  private Map<Long, Map<String, RecentDeliveryCount>> assigneesOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private Map<String, List<RecentDeliveryDetail>> dataDetails = new HashMap<>();

}
