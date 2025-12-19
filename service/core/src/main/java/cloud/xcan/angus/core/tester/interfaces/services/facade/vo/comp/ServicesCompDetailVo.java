package cloud.xcan.angus.core.tester.interfaces.services.facade.vo.comp;


import cloud.xcan.angus.core.tester.domain.services.comp.ServicesCompType;
import cloud.xcan.angus.remote.NameJoinField;
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

  private Long modifiedBy;

  @NameJoinField(id = "modifiedBy", repository = "commonUserBaseRepo")
  private String modifier;

  private LocalDateTime modifiedDate;

}
