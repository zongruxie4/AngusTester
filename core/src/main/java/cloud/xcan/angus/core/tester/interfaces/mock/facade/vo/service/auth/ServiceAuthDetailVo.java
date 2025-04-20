package cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.auth;


import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.experimental.Accessors;

@Accessors(chain = true)

public abstract class ServiceAuthDetailVo implements ServiceAuthVo {

  @Schema(description = "Datasource authorization id")
  private Long id;

  @Schema(description = "Authorization object type")
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object id")
  private Long authObjectId;

  @Schema(description = "Authorization permissions(Operation permission)")
  private List<MockServicePermission> permissions;

  private Boolean creator;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public AuthObjectType getAuthObjectType() {
    return authObjectType;
  }

  @Override
  public void setAuthObjectType(AuthObjectType authObjectType) {
    this.authObjectType = authObjectType;
  }

  @Override
  public Long getAuthObjectId() {
    return authObjectId;
  }

  @Override
  public void setAuthObjectId(Long authObjectId) {
    this.authObjectId = authObjectId;
  }

  @Override
  public abstract String getName();

  @Override
  public abstract void setName(String name);

  @Override
  public List<MockServicePermission> getPermissions() {
    return permissions;
  }

  @Override
  public void setPermissions(List<MockServicePermission> permissions) {
    this.permissions = permissions;
  }

  @Override
  public Boolean getCreator() {
    return creator;
  }

  @Override
  public void setCreator(Boolean creator) {
    this.creator = creator;
  }
}



