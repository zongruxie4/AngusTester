package cloud.xcan.sdf.core.angustester.domain.task.cases;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.spec.experimental.EntitySupport;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "task_func_case")
@Setter
@Getter
@Accessors(chain = true)
public class TaskFuncCase extends EntitySupport<TaskFuncCase, Long> {

  @Id
  private Long id;

  /**
   * Note: Only TASK and FUNC_CASE are supported.
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "target_type")
  private CombinedTargetType targetType;

  @Column(name = "target_id")
  private Long targetId;

  /**
   * Note: Only TASK and FUNC_CASE are supported.
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "assoc_target_type")
  private CombinedTargetType assocTargetType;

  @Column(name = "assoc_target_id")
  private Long assocTargetId;

  public List<Long> getWideTargetIds() {
    return List.of(this.assocTargetId, this.targetId);
  }

  public boolean isAssocTask() {
    return CombinedTargetType.TASK.equals(assocTargetType);
  }

  public boolean isAssocFuncCase() {
    return CombinedTargetType.FUNC_CASE.equals(assocTargetType);
  }

  public Long getWideAssocTaskTargetId() {
    return CombinedTargetType.TASK.equals(assocTargetType) ? assocTargetId
        : CombinedTargetType.TASK.equals(targetType) ? targetId : -1L;
  }

  public Long getWideAssocCaseTargetId() {
    return CombinedTargetType.FUNC_CASE.equals(assocTargetType) ? assocTargetId
        : CombinedTargetType.FUNC_CASE.equals(targetType) ? targetId : -1L;
  }

  public boolean isTaskAssocTask() {
    return CombinedTargetType.TASK.equals(assocTargetType)
        && CombinedTargetType.TASK.equals(targetType);
  }

  public boolean isTaskAssocCase() {
    return (CombinedTargetType.TASK.equals(assocTargetType)
        && CombinedTargetType.FUNC_CASE.equals(targetType))
        || (CombinedTargetType.FUNC_CASE.equals(assocTargetType)
        && CombinedTargetType.TASK.equals(targetType));
  }

  public boolean isCaseAssocCase() {
    return CombinedTargetType.FUNC_CASE.equals(assocTargetType)
        && CombinedTargetType.FUNC_CASE.equals(targetType);
  }

  @Override
  public Long identity() {
    return id;
  }

}
