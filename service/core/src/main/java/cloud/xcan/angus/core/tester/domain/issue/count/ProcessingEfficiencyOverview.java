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
public class ProcessingEfficiencyOverview extends AbstractOverview {

  private Map<Long, UserInfo> assignees = new HashMap<>();

  private ProcessingEfficiencyCount totalOverview = new ProcessingEfficiencyCount();

  private Map<Long, ProcessingEfficiencyCount> assigneesOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<ProcessingEfficiencyDetail> dataDetails = new ArrayList<>();

}
