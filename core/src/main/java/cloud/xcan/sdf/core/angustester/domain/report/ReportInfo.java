package cloud.xcan.sdf.core.angustester.domain.report;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.enums.CreatedAt;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportPermission;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.time.LocalDateTime;
import java.util.Set;
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
@Table(name = "report")
@Setter
@Getter
@Accessors(chain = true)
public class ReportInfo extends TenantAuditingEntity<ReportInfo, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "auth_flag")
  private Boolean authFlag;

  private String name;

  private String version;

  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "category")
  private ReportCategory category;

  @Enumerated(EnumType.STRING)
  @Column(name = "template")
  private ReportTemplate template;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private ReportStatus status;

  @Column(name = "failure_message")
  private String failureMessage;

  @Enumerated(EnumType.STRING)
  @Column(name = "created_at")
  private CreatedAt createdAt;

  @Column(name = "next_generation_date")
  private LocalDateTime nextGenerationDate;

  @Column(name = "target_id")
  private Long targetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type")
  private CombinedTargetType targetType;

  @Column(name = "target_name")
  private String targetName;

  @Column(name = "share_token")
  private String shareToken;

  @Transient
  private Set<ReportPermission> currentAuths;

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public Long getParentId() {
    return projectId;
  }
}