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
public class ResourceCreationOverview extends AbstractOverview{

  private Map<Long, UserInfo> creators = new HashMap<>();

  private ResourceCreationCount totalOverview = new ResourceCreationCount();

  private Map<Long, ResourceCreationCount> creatorOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<ResourceCreationDetail> dataDetails = new ArrayList<>();

}
