package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisCaseObject;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisCaseTemplate;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisDataSource;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisResource;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisTaskObject;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisTaskTemplate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class AnalysisSearchDto extends PageQuery {

  @ApiModelProperty(value = "Analysis id")
  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @ApiModelProperty(value = "Analysis resource type")
  private AnalysisResource resource;

  /**
   * @see AnalysisCaseTemplate
   * @see AnalysisTaskTemplate
   */
  @ApiModelProperty(value = "Analysis template")
  private String template;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Analysis name")
  private String name;

  /**
   * @see AnalysisCaseObject
   * @see AnalysisTaskObject
   */
  @ApiModelProperty(value = "Analysis object type")
  private String object;
  @ApiModelProperty(value = "Analysis sprint or plan id, required when the object type is a plan")
  private Long planId; // Sprint or plan id
  @ApiModelProperty(value = "Analysis organization type, required when the object type is an organization")
  private AuthObjectType orgType;
  @ApiModelProperty(value = "Analysis organization id, required when the object type is an organization")
  private Long orgId;

  @ApiModelProperty(value = "Analysis data source")
  private AnalysisDataSource datasource;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
