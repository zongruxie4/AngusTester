package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ApisUnarchivedListVo {

  private Long id;

  private Long projectId;

  @Schema(example = "http")
  private ApisProtocol protocol;

  @Schema(example = "GET")
  private String method;

  @Schema(example = "/comm/api/v1/country/{id}")
  private String endpoint;

  private String summary;

  private LocalDateTime createdDate;

  private LocalDateTime modifiedDate;
}



