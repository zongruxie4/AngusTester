package cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler;

import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_CASE_STATISTICS;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.arrayToLists;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncCaseCount;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.FuncCaseSummaryStatisticsDto;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.locale.MessageHolder;
import cloud.xcan.angus.spec.utils.FileUtils;
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
        .rangeSearchFields("deadlineDate", "createdDate", "modifiedDate",
            "reviewDate", "testResultHandleDate", "reviewNum", "testNum", "testFailNum")
        .inAndNotFields("id", "tagId", "testResult", "testerId")
        .orderByFields("id", "createdDate", "modifiedDate", "priority",
            "deadlineDate", "reviewStatus", "reviewNum", "testerId", "reviewerId",
            "testNum", "testFailNum", "testResult", "reviewDate", "testResultHandleDate")
        .matchSearchFields("name", "description", "code", "uri")
        .build();
  }

}
