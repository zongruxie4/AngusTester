package cloud.xcan.sdf.core.angustester.domain.kanban;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class EfficiencyCaseRanking {

  /**
   * Ignoring cancel status use cases
   */
  private List<AssigneeRanking> validCaseNumRank = new ArrayList<>();

  private List<AssigneeRanking> passedTestNumRank = new ArrayList<>();

  private List<AssigneeRanking> passedTestRateRank = new ArrayList<>();

  private List<AssigneeRanking> actualWorkloadRank = new ArrayList<>();

  private List<AssigneeRanking> completedWorkloadRank = new ArrayList<>();

  private List<AssigneeRanking> savingWorkloadRank = new ArrayList<>();

  private List<AssigneeRanking> overdueNumRank = new ArrayList<>();

  private List<AssigneeRanking> overdueRateRank = new ArrayList<>();

  private List<AssigneeRanking> oneTimePassedTestNumRank = new ArrayList<>();

  private List<AssigneeRanking> oneTimePassedTestRateRank = new ArrayList<>();

  private List<AssigneeRanking> oneTimeUnPassedTestNumRank = new ArrayList<>();

  private List<AssigneeRanking> oneTimeUnPassedTestRateRank = new ArrayList<>();

  private List<AssigneeRanking> oneTimePassedReviewNumRank = new ArrayList<>();

  private List<AssigneeRanking> oneTimePassedReviewRateRank = new ArrayList<>();

}
