package cloud.xcan.sdf.core.angustester.domain.report;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.enums.CreatedAt;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.report.setting.BasicInfoSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.ContentSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.TimeSetting;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "report")
@Setter
@Getter
@Accessors(chain = true)
public class Report extends TenantAuditingEntity<Report, Long> implements ActivityResource {

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
  private ReportCategory category;

  @Enumerated(EnumType.STRING)
  private ReportTemplate template;

  @Enumerated(EnumType.STRING)
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

  @Type(type = "json")
  @Column(name = "create_time_setting")
  private TimeSetting createTimeSetting;

  @Type(type = "json")
  @Column(name = "basic_info_setting")
  private BasicInfoSetting basicInfoSetting;

  @Type(type = "json")
  @Column(name = "content_setting")
  private ContentSetting contentSetting;

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public Long getParentId() {
    return projectId;
  }
}
