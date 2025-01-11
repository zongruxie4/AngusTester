package cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth;


import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@ApiModel
public abstract class ServicesAuthDetailVo implements ServicesAuthVo {

  @ApiModelProperty(value = "services authorization id")
  private Long id;

  @ApiModelProperty(value = "Authorization object type")
  private AuthObjectType authObjectType;

  @ApiModelProperty(value = "Authorization object id")
  private Long authObjectId;

  @ApiModelProperty(value = "Authorization permissions(Operation permission)")
  private List<ServicesPermission> permissions;

  @ApiModelProperty(value = "Services id")
  private Long serviceId;

  @ApiModelProperty(value = "Creator auth flag")
  private Boolean creatorFlag;

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
  public List<ServicesPermission> getPermissions() {
    return permissions;
  }

  @Override
  public void setPermissions(List<ServicesPermission> permissions) {
    this.permissions = permissions;
  }

  @Override
  public Long getServiceId() {
    return serviceId;
  }

  @Override
  public void setServiceId(Long projectId) {
    this.serviceId = projectId;
  }

  @Override
  public Boolean getCreatorFlag() {
    return creatorFlag;
  }

  @Override
  public void setCreatorFlag(Boolean creatorFlag) {
    this.creatorFlag = creatorFlag;
  }
}



