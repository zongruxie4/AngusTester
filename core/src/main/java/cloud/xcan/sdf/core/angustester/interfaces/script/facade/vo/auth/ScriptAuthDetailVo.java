package cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth;


import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.spec.experimental.Value;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@ApiModel
public abstract class ScriptAuthDetailVo implements ScriptAuthVo {

  @ApiModelProperty(value = "Apis authorization id")
  private Long id;

  @ApiModelProperty(value = "Authorization object type")
  private AuthObjectType authObjectType;

  @ApiModelProperty(value = "Authorization object id")
  private Long authObjectId;

  @ApiModelProperty(value = "Authorization permissions(Operation permission)")
  private List<? extends Value<?>> permissions;

  private Boolean creatorFlag;

  @ApiModelProperty(value = "Script id")
  private Long scriptId;

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
  public List<? extends Value<?>> getPermissions() {
    return permissions;
  }

  @Override
  public void setPermissions(List<? extends Value<?>> permissions) {
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

  @Override
  public Long getScriptId() {
    return scriptId;
  }

  @Override
  public void setScriptId(Long scriptId) {
    this.scriptId = scriptId;
  }
}



