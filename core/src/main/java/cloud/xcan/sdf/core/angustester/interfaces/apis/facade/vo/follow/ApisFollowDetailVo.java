package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.follow;


import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisFollowDetailVo {

  private Long id;

  private Long apisId;

  private String apisName;

  private HttpMethod method;

  private String apisUri;

}



