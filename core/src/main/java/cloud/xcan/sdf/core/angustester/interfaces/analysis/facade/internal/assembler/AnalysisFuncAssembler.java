package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.internal.assembler;

import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginMessage.EXPORT_CASE_STATISTICS;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getTenantId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.arrayToLists;

import cloud.xcan.sdf.api.commonlink.TesterConstant;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.FuncCaseCount;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.FuncCaseSummaryStatisticsDto;
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

public class AnalysisFuncAssembler {

  @SneakyThrows
  @NotNull
  public static InputStreamResource toCaseStatisticsExportResource(
      FuncCaseCount count, String fileName) {
    String filePath = new SpringAppDirUtils().getTmpDir() + TesterConstant.EXPORT_SUMMARY_DIR
        + getTenantId() + File.separator + fileName;
    File file = new File(filePath);
    FileUtils.forceMkdirParent(file);
    String headerMessage = MessageHolder.message(EXPORT_CASE_STATISTICS);
    Assert.assertNotEmpty(headerMessage, "CaseStatisticsExport message not configured");
    EasyExcel.write(file, FuncCaseCount.class)
        .head(arrayToLists(headerMessage.split(","))).sheet()
        .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
        .doWrite(() -> List.of(count));
    return new InputStreamResource(new FileInputStream(filePath));
  }

  public static Set<SearchCriteria> getSearchCriteria(FuncCaseSummaryStatisticsDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("deadlineDate", "createdDate", "lastModifiedDate",
            "reviewDate", "testResultHandleDate", "reviewNum", "testNum", "testFailNum")
        .inAndNotFields("id", "tagId", "testResult", "testerId")
        .orderByFields("id", "createdDate", "lastModifiedDate", "priority",
            "deadlineDate", "reviewStatus", "reviewNum", "testerId", "reviewerId",
            "testNum", "testFailNum", "testResult", "reviewDate", "testResultHandleDate")
        .matchSearchFields("name", "description", "code", "uri")
        .build();
  }

}
