package cloud.xcan.sdf.core.angustester.domain.kanban;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class AssigneeRanking {

  private Long assigneeId;
  private Object score;

  public AssigneeRanking() {
  }

  public AssigneeRanking(Long assigneeId, Object score) {
    this.assigneeId = assigneeId;
    this.score = score;
  }

  public static AssigneeRanking rank(Long assigneeId, Object score) {
    return new AssigneeRanking(assigneeId, score);
  }

}
