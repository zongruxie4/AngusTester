package cloud.xcan.angus.core.tester.interfaces.tag.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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
public class TagSearchDto extends PageQuery {

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  private String name;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}
