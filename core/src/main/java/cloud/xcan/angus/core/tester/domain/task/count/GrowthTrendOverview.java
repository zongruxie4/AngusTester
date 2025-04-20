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
public class GrowthTrendOverview extends AbstractOverview {

  private Map<Long, UserInfo> assignees = new HashMap<>();

  private GrowthTrendCount totalOverview = new GrowthTrendCount();

  private Map<Long, GrowthTrendCount> assigneesOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<GrowthTrendDetail> dataDetails = new ArrayList<>();

}
