package cloud.xcan.sdf.core.angustester.domain.report.record.content;

import cloud.xcan.sdf.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.sdf.api.commonlink.exec.result.ExecScenarioResultInfo;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyTaskOverview;
import cloud.xcan.sdf.core.angustester.domain.project.summary.ProjectSummary;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ProjectProgressContent implements ReportContent {

  private final String template = PROJECT_PROGRESS;

  private ProjectSummary project;

  private Progress progress;

  private EfficiencyTaskOverview tasks;

  private EfficiencyCaseOverview cases;

  private ExecApisResultInfo apis;

  private ExecScenarioResultInfo scenarios;

}
