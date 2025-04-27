package cloud.xcan.angus.core.tester.domain.apis.trash;


import cloud.xcan.angus.api.commonlink.ApisTargetType;
import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Entity
@Table(name = "apis_trash")
@Setter
@Getter
@Accessors(chain = true)
public class ApisTrash extends TenantEntity<ApisTrash, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "services_id")
  private Long servicesId;

  @Column(name = "target_type")
  @Enumerated(EnumType.STRING)
  private ApisTargetType targetType;

  @Column(name = "target_id")
  private Long targetId;

  @Column(name = "target_name")
  private String targetName;

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
    return servicesId;
  }
}
