package cloud.xcan.angus.core.tester.domain.report;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.CreatedAt;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.report.setting.BasicInfoSetting;
import cloud.xcan.angus.core.tester.domain.setting.ContentSetting;
import cloud.xcan.angus.core.tester.domain.setting.TimeSetting;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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

  @Column(name = "auth")
  private Boolean auth;

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

  @Type(JsonType.class)
  @Column(name = "create_time_setting")
  private TimeSetting createTimeSetting;

  @Type(JsonType.class)
  @Column(name = "basic_info_setting")
  private BasicInfoSetting basicInfoSetting;

  @Type(JsonType.class)
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
