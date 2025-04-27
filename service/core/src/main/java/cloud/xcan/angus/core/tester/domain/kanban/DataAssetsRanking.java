package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class DataAssetsRanking {

  private Map<Long, UserInfo> userInfos;

  private Map<DataAssetsLabel, List<ResourcesRanking>> rankings;

}
