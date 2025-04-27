package cloud.xcan.angus.core.tester.application.cmd.func;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCase;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public interface FuncCaseCmd {

  List<IdKey<Long, Object>> add(List<FuncCase> cases);

  void update(List<FuncCase> cases);

  List<IdKey<Long, Object>> replace(List<FuncCase> cases);

  void rename(Long id, String name);

  List<IdKey<Long, Object>> clone(Set<Long> ids);

  void move(Set<Long> caseIds, Long targetPlanId);

  void replaceTester(Long id, Long testerId);

  void replaceDeadline(Long id, LocalDateTime deadlineDate);

  void replacePriority(Long id, Priority priority);

  void replaceSoftwareVersion(Long id, String softwareVersion);

  void replaceEvalWorkload(Long id, BigDecimal workload);

  void replaceActualWorkload(Long id, BigDecimal workload);

  void replaceAttachment(Long id, List<Attachment> attachments);

  void resultModify(List<FuncCase> cases, boolean replace);

  void resultReset(HashSet<Long> ids);

  void retest(HashSet<Long> ids);

  void review(List<FuncCase> cases);

  void reviewReset(HashSet<Long> ids);

  void taskAssocAdd(Long id, HashSet<Long> assocTaskIds);

  void taskAssocCancel(Long id, HashSet<Long> assocTaskIds);

  void caseAssocAdd(Long id, HashSet<Long> assocCaseIds);

  void caseAssocCancel(Long id, HashSet<Long> assocCaseIds);

  List<IdKey<Long, Object>> imports(Long planId, StrategyWhenDuplicated strategyWhenDuplicated,
      MultipartFile file);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Collection<Long> ids);

  void delete0(List<Long> caseIds);

  void addReviewActivities(List<FuncCase> casesDb);

}




