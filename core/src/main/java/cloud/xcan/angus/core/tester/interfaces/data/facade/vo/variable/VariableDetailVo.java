package cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable;


import cloud.xcan.angus.core.tester.domain.data.ParameterizationDataSource;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class VariableDetailVo {

  private Long id;

  private Long projectId;

  private String name;

  private String description;

  private Boolean extracted;

  private ParameterizationDataSource dataSource;

  private String value;

  private Boolean passwordValue;

  private DefaultExtraction extraction;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
