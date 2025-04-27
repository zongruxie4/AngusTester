package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.favourite;


import cloud.xcan.angus.spec.http.HttpMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


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



