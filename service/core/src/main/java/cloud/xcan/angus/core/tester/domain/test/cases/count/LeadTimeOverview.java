package cloud.xcan.angus.core.tester.domain.test.cases.count;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.issue.count.AbstractOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeCount;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeDetail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LeadTimeOverview extends AbstractOverview {

  private Map<Long, UserInfo> testers = new HashMap<>();

  private LeadTimeCount totalOverview = new LeadTimeCount();

  private Map<Long, LeadTimeCount> testersOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<LeadTimeDetail> dataDetails = new ArrayList<>();

}
