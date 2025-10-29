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
public class TesterSubmittedBugOverview extends AbstractOverview{

  private Map<Long, UserInfo> testers = new HashMap<>();

  private TesterSubmittedBugCount totalOverview = new TesterSubmittedBugCount();

  private Map<Long, TesterSubmittedBugCount> testersOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<TesterSubmittedBugDetail> dataDetails = new ArrayList<>();

}
