package cloud.xcan.sdf.core.angustester.domain.services.summary;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ServicesSummary {

  private Long id;

  private Long projectId;

  @NameJoinField(id = "projectId", repository = "projectRepo")
  private String projectName;

  private ApiSource source;

  private ApiImportSource importSource;

  private String name;

  private Boolean authFlag;

  private ApiStatus status;

  private Boolean hasApisFlag;

  private Long mockServiceId;

  private Long apisNum;

  private Long apisCaseNum;

  //private ServiceSchemaDetailVo schema;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime lastModifiedDate;
}
