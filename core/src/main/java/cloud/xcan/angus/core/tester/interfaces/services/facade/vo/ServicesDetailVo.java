package cloud.xcan.angus.core.tester.interfaces.services.facade.vo;


import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.schema.ServiceSchemaDetailVo;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ServicesDetailVo {

  private Long id;

  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  private ApiSource source;

  private ApiImportSource importSource;

  private String name;

  private Boolean auth;

  private ApiStatus status;

  private Boolean hasApis;

  private Long mockServiceId;

  private Long apisNum;

  private Long apisCaseNum;

  private ServiceSchemaDetailVo schema;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}



