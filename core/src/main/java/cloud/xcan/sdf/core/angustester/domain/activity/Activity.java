package cloud.xcan.sdf.core.angustester.domain.activity;


import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantListener;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Only a single activity is recorded, and resource information is not recorded during batch
 * operations
 */
@Entity
@Table(name = "activity")
@EntityListeners({TenantListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class Activity extends TenantEntity<Activity, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "target_id")
  private Long targetId;

  @Column(name = "parent_target_id")
  private Long parentTargetId;

  @Column(name = "target_type")
  @Enumerated(EnumType.STRING)
  private CombinedTargetType targetType;

  @Column(name = "target_name")
  private String targetName;

  @Column(name = "main_target_id")
  private Long mainTargetId;

  @Column(name = "user_id")
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private ActivityType type;

  @Column(name = "opt_date")
  private LocalDateTime optDate;

  private String description;

  private String detail;

  @Transient
  private String projectName;
  @Transient
  private String fullname;
  @Transient
  private String avatar;

  @Override
  public Long identity() {
    return this.id;
  }
}
