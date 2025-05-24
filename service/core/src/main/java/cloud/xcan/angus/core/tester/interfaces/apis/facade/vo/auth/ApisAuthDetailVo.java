package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth;


import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.api.enums.AuthObjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class ApisAuthDetailVo implements ApiAuthVo {

  @Schema(description = "Apis authorization id")
  private Long id;

  @Schema(description = "Authorization object testType")
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object id")
  private Long authObjectId;

  @Schema(description = "Authorization permissions(Operation permission)")
  private List<ApiPermission> permissions;

  @Schema(description = "Apis id")
  private Long apisId;

  @Schema(description = "Creator auth flag")
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
  public List<ApiPermission> getPermissions() {
    return permissions;
  }

  @Override
  public void setPermissions(List<ApiPermission> permissions) {
    this.permissions = permissions;
  }

  @Override
  public Long getApisId() {
    return apisId;
  }

  @Override
  public void setApisId(Long apisId) {
    this.apisId = apisId;
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



