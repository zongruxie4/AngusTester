package cloud.xcan.sdf.core.angustester.domain.scenario.trash;


import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Entity
@Table(name = "scenario_trash")
@Setter
@Getter
@Accessors(chain = true)
public class ScenarioTrash extends TenantEntity<ScenarioTrash, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "target_name")
  private String targetName;

  @Column(name = "target_id")
  private Long targetId;

  @Column(name = "created_by")
  private Long createdBy;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @Column(name = "deleted_date")
  private LocalDateTime deletedDate;

  @Transient
  private String createdByName;
  @Transient
  private String createdByAvatar;
  @Transient
  private String deletedByName;
  @Transient
  private String deletedByAvatar;

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public String getName() {
    return this.targetName;
  }

  @Override
  public Long getParentId() {
    return projectId;
  }
}
