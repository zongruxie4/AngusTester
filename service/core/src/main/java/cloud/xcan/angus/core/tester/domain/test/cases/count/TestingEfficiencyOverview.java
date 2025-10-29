package cloud.xcan.angus.core.tester.domain.test.cases.count;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.task.count.AbstractOverview;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestingEfficiencyOverview extends AbstractOverview {

  private Map<Long, UserInfo> testers = new HashMap<>();

  private TestingEfficiencyCount totalOverview = new TestingEfficiencyCount();

  private Map<Long, TestingEfficiencyCount> testersOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<TestingEfficiencyDetail> dataDetails = new ArrayList<>();

}
