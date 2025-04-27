package cloud.xcan.angus.core.tester.domain.task.count;

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
public class BugOverview extends AbstractOverview {

  private Map<Long, UserInfo> assignees = new HashMap<>();

  private BugCount totalOverview = new BugCount();

  private Map<Long, BugCount> assigneesOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<BugDetail> dataDetails = new ArrayList<>();

}
