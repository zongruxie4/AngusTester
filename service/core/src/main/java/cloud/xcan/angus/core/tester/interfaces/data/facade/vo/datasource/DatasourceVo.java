package cloud.xcan.angus.core.tester.interfaces.data.facade.vo.datasource;


import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class DatasourceVo {

  private Long id;

  private Long projectId;

  private String name;

  private String database;

  private String driverClassName;

  private String username;

  private String password;

  private String jdbcUrl;

  private Long tenantId;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  private String lastModifiedByName;

  private String avatar;

  private LocalDateTime lastModifiedDate;

}
