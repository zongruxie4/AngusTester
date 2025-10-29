package cloud.xcan.angus.core.tester.domain.test.cases.count;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.task.count.AbstractOverview;
import cloud.xcan.angus.core.tester.domain.task.count.RecentDeliveryCount;
import cloud.xcan.angus.core.tester.domain.task.count.RecentDeliveryDetail;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecentDeliveryOverview extends AbstractOverview {

  private Map<Long, UserInfo> testers = new HashMap<>();

  private Map<String, RecentDeliveryCount> totalOverview = new HashMap<>();

  private Map<Long, Map<String, RecentDeliveryCount>> testersOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private Map<String, List<RecentDeliveryDetail>> dataDetails = new HashMap<>();

}
