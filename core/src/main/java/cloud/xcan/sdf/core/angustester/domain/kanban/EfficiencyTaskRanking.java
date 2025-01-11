package cloud.xcan.sdf.core.angustester.domain.kanban;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class EfficiencyTaskRanking {

  /**
   * Ignoring cancel status use tasks
   */
  private List<AssigneeRanking> validTaskNumRank = new ArrayList<>();

  private List<AssigneeRanking> completedNumRank = new ArrayList<>();

  private List<AssigneeRanking> completedRateRank = new ArrayList<>();

  private List<AssigneeRanking> actualWorkloadRank = new ArrayList<>();

  private List<AssigneeRanking> completedWorkloadRank = new ArrayList<>();

  private List<AssigneeRanking> savingWorkloadRank = new ArrayList<>();

  private List<AssigneeRanking> overdueNumRank = new ArrayList<>();

  private List<AssigneeRanking> overdueRateRank = new ArrayList<>();

  private List<AssigneeRanking> oneTimePassedNumRank = new ArrayList<>();

  private List<AssigneeRanking> oneTimePassedRateRank = new ArrayList<>();

  private List<AssigneeRanking> oneTimeUnPassedNumRank = new ArrayList<>();

  private List<AssigneeRanking> oneTimeUnPassedRateRank = new ArrayList<>();

  private List<AssigneeRanking> validBugNumRank = new ArrayList<>();

  private List<AssigneeRanking> validBugRateRank = new ArrayList<>();
}
