package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X2;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisCaseObject;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisCaseTemplate;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisDataSource;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisResource;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisTaskObject;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisTaskTemplate;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisTimeRange;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class AnalysisReplaceDto {

  @ApiModelProperty(value = "Analysis id")
  private Long id;

  @ApiModelProperty(value = "Project id, required when creating a new analysis")
  private Long projectId;

  @NotNull
  @ApiModelProperty(value = "Analysis resource type", required = true)
  private AnalysisResource resource;

  /**
   * @see AnalysisCaseTemplate
   * @see AnalysisTaskTemplate
   */
  @NotEmpty
  @ApiModelProperty(value = "Analysis template", required = true)
  private String template;

  @NotEmpty
  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Analysis name", required = true)
  private String name;

  @Length(max = DEFAULT_DESC_LENGTH)
  @ApiModelProperty(value = "Analysis description")
  private String description;

  /**
   * @see AnalysisCaseObject
   * @see AnalysisTaskObject
   */
  @NotEmpty
  @ApiModelProperty(value = "Analysis object type", required = true)
  private String object;
  @ApiModelProperty(value = "Analysis sprint or plan id, required when the object type is a plan")
  private Long planId; // Sprint or plan id
  @ApiModelProperty(value = "Analysis organization type, required when the object type is an organization")
  private AuthObjectType orgType;
  @ApiModelProperty(value = "Analysis organization id, required when the object type is an organization")
  private Long orgId;

  @ApiModelProperty(value = "Contains tester analysis, default true")
  private Boolean containsUserAnalysis;
  @ApiModelProperty(value = "Contains data details, default true")
  private Boolean containsDataDetail;

  @NotNull
  @ApiModelProperty(value = "Analysis time range", required = true)
  private AnalysisTimeRange timeRange;
  @ApiModelProperty(value = "Analysis data start time, required when the time range is `CUSTOM_TIME`")
  private LocalDateTime startTime;
  @ApiModelProperty(value = "Analysis data end time, required when the time range is `CUSTOM_TIME`")
  private LocalDateTime endTime;

  @NotNull
  @ApiModelProperty(value = "Analysis data source", required = true)
  private AnalysisDataSource datasource;

  @Length(max = DEFAULT_URL_LENGTH_X2)
  @ApiModelProperty(value = "Analysis custom filter criteria")
  private String filterCriteria;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  public boolean isAnalysisOrg(){
    return AnalysisCaseObject.TESTER_ORG.getValue().equalsIgnoreCase(object)
        || AnalysisTaskObject.ASSIGNEE_ORG.getValue().equalsIgnoreCase(object);
  }

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  public boolean isAnalysisOrgTargetMissing(){
    return isAnalysisOrg() && (isNull(orgType) || isNull(orgId));
  }
}
