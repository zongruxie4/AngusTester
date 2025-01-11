package cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.comp;


import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.core.angustester.domain.services.comp.ServicesCompType;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServicesCompDetailVo {

  private Long id;

  private Long serviceId;

  private ServicesCompType type;

  private String key;

  private String ref;

  private String model;

  private String description;

  //private int schemaHash;

  private Map<String, String> resolvedRefModels;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
