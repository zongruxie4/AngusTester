package cloud.xcan.angus.core.tester.domain.version;


import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.core.tester.domain.task.count.ProgressCount;
import cloud.xcan.angus.core.tester.domain.task.summary.TaskSummary;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "software_version")
@Setter
@Getter
@Accessors(chain = true)
public class SoftwareVersion extends TenantAuditingEntity<SoftwareVersion, Long> implements
    ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  private String name;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "release_date")
  private LocalDateTime releaseDate;

  @Enumerated(EnumType.STRING)
  private SoftwareVersionStatus status;

  private String description;

  @Transient
  private ProgressCount progress;
  @Transient
  private Map<TaskStatus, List<TaskSummary>> taskByStatus;

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public Long getParentId() {
    return projectId;
  }
}
