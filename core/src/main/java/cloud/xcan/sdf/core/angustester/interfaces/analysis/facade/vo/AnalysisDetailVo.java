package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.vo;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisCaseObject;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisCaseTemplate;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisDataSource;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisResource;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisTaskObject;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisTaskTemplate;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisTimeRange;
import cloud.xcan.sdf.core.angustester.domain.task.count.AbstractOverview;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class AnalysisDetailVo {

  @ApiModelProperty(value = "Analysis id")
  private Long id;

  @ApiModelProperty(value = "Project id")
  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

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

  @Length(max = DEFAULT_DESC_LENGTH)
  @ApiModelProperty(value = "Analysis description")
  private String description;

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

  @ApiModelProperty(value = "Contains tester analysis, default true")
  private Boolean containsUserAnalysis;
  @ApiModelProperty(value = "Contains data details, default true")
  private Boolean containsDataDetail;

  @ApiModelProperty(value = "Analysis time range")
  private AnalysisTimeRange timeRange;
  @ApiModelProperty(value = "Analysis data start time, required when the time range is `CUSTOM_TIME`")
  private LocalDateTime startTime;
  @ApiModelProperty(value = "Analysis data end time, required when the time range is `CUSTOM_TIME`")
  private LocalDateTime endTime;

  @ApiModelProperty(value = "Analysis data source")
  private AnalysisDataSource datasource;

  @Length(max = DEFAULT_URL_LENGTH)
  @ApiModelProperty(value = "Analysis custom filter criteria")
  private String filterCriteria;

  @ApiModelProperty(value = "Analysis data")
  private AbstractOverview data;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;
}
