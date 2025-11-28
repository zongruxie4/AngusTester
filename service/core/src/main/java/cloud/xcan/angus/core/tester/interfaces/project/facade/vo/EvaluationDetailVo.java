package cloud.xcan.angus.core.tester.interfaces.project.facade.vo;

import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationScope;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import cloud.xcan.angus.remote.NameJoinField;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class EvaluationDetailVo {

  private Long id;

  private String name;

  private Long projectId;

  @NameJoinField(id = "projectId", repository = "projectRepo")
  private String projectName;

  private EvaluationScope scope;

  private List<EvaluationPurpose> purposes;

  private Long resourceId;

  private String resourceName;

  private LocalDateTime startDate;

  private LocalDateTime deadlineDate;

  private TestEvaluationResult result;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedName;

  private LocalDateTime lastModifiedDate;
}

