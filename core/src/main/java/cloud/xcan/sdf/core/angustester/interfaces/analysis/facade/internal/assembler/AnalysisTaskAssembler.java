package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.internal.assembler;

import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_TASK_STATISTICS;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getTenantId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.arrayToLists;

import cloud.xcan.sdf.api.commonlink.TesterConstant;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskCount;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.TaskSummaryStatisticsDto;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.sdf.core.utils.SpringAppDirUtils;
import cloud.xcan.sdf.spec.experimental.Assert;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import cloud.xcan.sdf.spec.utils.FileUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;

public class AnalysisTaskAssembler {

  @SneakyThrows
  @NotNull
  public static InputStreamResource toTaskStatisticsExport(
      TaskCount count, String fileName) {
    String filePath = new SpringAppDirUtils().getTmpDir() + TesterConstant.EXPORT_SUMMARY_DIR
        + getTenantId() + File.separator + fileName;
    File file = new File(filePath);
    FileUtils.forceMkdirParent(file);
    String headerMessage = MessageHolder.message(EXPORT_TASK_STATISTICS);
    Assert.assertNotEmpty(headerMessage, "TaskStatisticsExport message not configured");
    EasyExcel.write(file, TaskCount.class)
        .head(arrayToLists(headerMessage.split(","))).sheet()
        .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
        .doWrite(() -> List.of(count));
    return new InputStreamResource(new FileInputStream(filePath));
  }

  public static Set<SearchCriteria> getSearchCriteria(TaskSummaryStatisticsDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .matchSearchFields("name", "code")
        .rangeSearchFields("startDate", "deadlineDate", "confirmedDate", "completedDate",
            "processedDate", "canceledDate", "execDate", "createdDate", "lastModifiedDate",
            "failNum", "totalNum", "evalWorkload", "actualWorkload")
        .inAndNotFields("id", "tagId", "status", "assigneeId", "confirmorId", "moduleId")
        .orderByFields("id", "name", "code", "createdDate", "taskType", "testType", "startDate",
            "deadlineDate", "priority", "status", "evalWorkload", "actualWorkload", "failNum",
            "totalNum", "execStatus", "execResult", "moduleId")
        .build();
  }
}
