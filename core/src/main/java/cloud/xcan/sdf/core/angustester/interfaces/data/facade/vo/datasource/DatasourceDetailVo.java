package cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.datasource;


import cloud.xcan.sdf.api.NameJoinField;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class DatasourceDetailVo {

  private Long id;

  private Long projectId;

  private String name;

  private String database;

  private String driverClassName;

  private String username;

  private String passd;

  private String jdbcUrl;

  private Long tenantId;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}