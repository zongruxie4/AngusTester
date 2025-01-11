package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.core.angustester.domain.task.count.AbstractOverview;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CoreKpiOverview extends AbstractOverview {

  private Map<Long, UserInfo> testers = new HashMap<>();

  private CoreKpiCount totalOverview = new CoreKpiCount();

  private Map<Long, CoreKpiCount> testersOverview = new HashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<CoreKpiDetail> dataDetails = new ArrayList<>();

}
