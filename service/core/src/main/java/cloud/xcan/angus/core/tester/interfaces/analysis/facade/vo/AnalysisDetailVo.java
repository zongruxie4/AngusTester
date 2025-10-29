package cloud.xcan.angus.core.tester.interfaces.analysis.facade.vo;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisCaseObject;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisCaseTemplate;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisDataSource;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisResource;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisTaskObject;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisTaskTemplate;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisTimeRange;
import cloud.xcan.angus.core.tester.domain.issue.count.AbstractOverview;
import cloud.xcan.angus.remote.NameJoinField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@Accessors(chain = true)
public class AnalysisDetailVo {

  @Schema(description = "Analysis id")
  private Long id;

  @Schema(description = "Project id")
  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  @Schema(description = "Analysis resource type")
  private AnalysisResource resource;

  /**
   * @see AnalysisCaseTemplate
   * @see AnalysisTaskTemplate
   */
  @Schema(description = "Analysis template")
  private String template;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Analysis name")
  private String name;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Analysis description")
  private String description;

  /**
   * @see AnalysisCaseObject
   * @see AnalysisTaskObject
   */
  @Schema(description = "Analysis object type")
  private String object;
  @Schema(description = "Analysis sprint or plan id, required when the object type is a plan")
  private Long planId; // Sprint or plan id
  @Schema(description = "Analysis organization type, required when the object type is an organization")
  private AuthObjectType orgType;
  @Schema(description = "Analysis organization id, required when the object type is an organization")
  private Long orgId;

  @Schema(description = "Contains tester analysis, default true")
  private Boolean containsUserAnalysis;
  @Schema(description = "Contains data details, default true")
  private Boolean containsDataDetail;

  @Schema(description = "Analysis time range")
  private AnalysisTimeRange timeRange;
  @Schema(description = "Analysis data start time, required when the time range is `CUSTOM_TIME`")
  private LocalDateTime startTime;
  @Schema(description = "Analysis data end time, required when the time range is `CUSTOM_TIME`")
  private LocalDateTime endTime;

  @Schema(description = "Analysis data source")
  private AnalysisDataSource datasource;

  @Length(max = MAX_URL_LENGTH)
  @Schema(description = "Analysis custom filter criteria")
  private String filterCriteria;

  @Schema(description = "Analysis data")
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
