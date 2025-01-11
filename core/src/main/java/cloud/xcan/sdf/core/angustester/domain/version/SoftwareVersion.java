package cloud.xcan.sdf.core.angustester.domain.version;


import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProgressCount;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskSummary;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
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
