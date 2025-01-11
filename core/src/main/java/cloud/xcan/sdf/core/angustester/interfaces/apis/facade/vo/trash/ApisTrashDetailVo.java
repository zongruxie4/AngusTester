package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.trash;


import cloud.xcan.sdf.api.commonlink.ApisTargetType;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisTrashDetailVo {

  private Long id;

  private Long projectId;

  private String targetName;

  private ApisTargetType targetType;

  private Long targetId;

  private ApisProtocol protocol;

  private HttpMethod method;

  private String endpoint;

  private Long createdBy;

  private String createdByName;

  private String createdByAvatar;

  private Long deletedBy;

  private String deletedByName;

  private String deletedByAvatar;

  private LocalDateTime deletedDate;

}



