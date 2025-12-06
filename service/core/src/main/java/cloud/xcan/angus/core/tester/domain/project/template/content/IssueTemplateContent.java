package cloud.xcan.angus.core.tester.domain.project.template.content;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.core.tester.domain.issue.BugLevel;
import cloud.xcan.angus.core.tester.domain.issue.TaskStatus;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.domain.project.template.TemplateType;
import cloud.xcan.angus.core.tester.domain.test.TestLayer;
import cloud.xcan.angus.core.tester.domain.test.TestPurpose;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseStepView;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestStep;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Setter
@Getter
public class IssueTemplateContent implements TemplateContent {

  private TemplateType templateType = TemplateType.ISSUE;

  public String name;

  @Column(name = "task_type")
  @Enumerated(EnumType.STRING)
  private TaskType taskType;

  @Column(name = "bug_level")
  @Enumerated(EnumType.STRING)
  private BugLevel bugLevel;

  @Column(name = "priority")
  @Enumerated(EnumType.STRING)
  private Priority priority;

  @Column(name = "missing_bug")
  private Boolean missingBug;

  @Column(name = "eval_workload_method")
  @Enumerated(EnumType.STRING)
  private EvalWorkloadMethod evalWorkloadMethod;

  @Column(name = "eval_workload")
  private BigDecimal evalWorkload;

  @Column(name = "actual_workload")
  private BigDecimal actualWorkload;

  public String description;

}
