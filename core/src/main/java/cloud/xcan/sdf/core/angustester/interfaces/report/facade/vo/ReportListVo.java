package cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.enums.CreatedAt;
import cloud.xcan.sdf.core.angustester.domain.report.ReportCategory;
import cloud.xcan.sdf.core.angustester.domain.report.ReportStatus;
import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportPermission;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ReportListVo {

  private Long id;

  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  private String name;

  public Boolean authFlag;

  private String version;

  private String description;

  private ReportCategory category;

  private ReportTemplate template;

  private ReportStatus status;

  private String failureMessage;

  private CreatedAt createdAt;

  private LocalDateTime nextGenerationDate;

  private Long targetId;

  private CombinedTargetType targetType;

  private String targetName;

  @ApiModelProperty(value = "Current user authorization information")
  private Set<ReportPermission> currentAuths;

  private Long tenantId;

  //@NameJoinField(id = "tenantId", repository = "commonTenantRepo")
  //private String tenantName;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
