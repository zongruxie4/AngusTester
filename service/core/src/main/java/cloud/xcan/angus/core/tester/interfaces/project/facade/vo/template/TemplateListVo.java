package cloud.xcan.angus.core.tester.interfaces.project.facade.vo.template;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.angus.core.tester.domain.project.template.TemplateType;
import cloud.xcan.angus.core.tester.domain.project.template.content.TemplateContent;
import cloud.xcan.angus.remote.NameJoinField;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TemplateListVo {

  private Long id;

  private String name;

  private TemplateType templateType;

  private TemplateContent templateContent;

  private Boolean isSystem;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime lastModifiedDate;

}

