package cloud.xcan.angus.core.tester.domain.kanban;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ResourcesRanking {

  private long userId;

  private long count;

}
