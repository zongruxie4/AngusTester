package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.auth;


import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServicePermission;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.persistence.Column;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@ApiModel
public abstract class ServiceAuthDetailVo implements ServiceAuthVo {

  @ApiModelProperty(value = "Datasource authorization id")
  private Long id;

  @ApiModelProperty(value = "Authorization object type")
  private AuthObjectType authObjectType;

  @ApiModelProperty(value = "Authorization object id")
  private Long authObjectId;

  @ApiModelProperty(value = "Authorization permissions(Operation permission)")
  private List<MockServicePermission> permissions;

  @Column(name = "creator_flag")
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
  public List<MockServicePermission> getPermissions() {
    return permissions;
  }

  @Override
  public void setPermissions(List<MockServicePermission> permissions) {
    this.permissions = permissions;
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



