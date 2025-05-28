package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PUBLIC_TOKEN_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Accessors(chain = true)
public class ApisShareApisSearchPubDto extends PageQuery {

  @Length(max = MAX_NAME_LENGTH)
  private String name;

  @NotNull
  @Schema(description = "Share id", requiredMode = RequiredMode.REQUIRED)
  private Long sid;

  @NotEmpty
  @Length(max = MAX_PUBLIC_TOKEN_LENGTH)
  @Schema(description = "Share public token", requiredMode = RequiredMode.REQUIRED)
  private String spt;


  //////////////////////////////////////////////////////////////

  //@NotNull
  //@Schema(description = "Target type", requiredMode = RequiredMode.REQUIRED)
  //private FuncTargetType targetType;

  //@NotNull
  //@Schema(description = "Target(Services/Service/Apis) id", requiredMode = RequiredMode.REQUIRED)
  //private Long targetId;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}
