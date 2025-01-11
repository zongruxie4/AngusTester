package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.favourite;


import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisFavouriteDetailVo {

  private Long id;

  private Long apisId;

  private String apisName;

  private HttpMethod method;

  private String apisUri;

}



