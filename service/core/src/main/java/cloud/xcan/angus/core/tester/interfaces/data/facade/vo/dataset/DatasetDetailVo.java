package cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset;


import cloud.xcan.angus.core.tester.domain.data.ParameterizationDataSource;
import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class DatasetDetailVo {

  private Long id;

  private Long projectId;

  private String name;

  private String description;

  private Boolean extracted;

  private ParameterizationDataSource dataSource;

  private List<DatasetParameter> parameters;

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
