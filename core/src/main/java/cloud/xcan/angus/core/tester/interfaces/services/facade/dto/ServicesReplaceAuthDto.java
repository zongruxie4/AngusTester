package cloud.xcan.angus.core.tester.interfaces.services.facade.dto;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ServicesReplaceAuthDto {

  @NotNull
  @Schema
  private List<ServicesPermission> authData;
}
