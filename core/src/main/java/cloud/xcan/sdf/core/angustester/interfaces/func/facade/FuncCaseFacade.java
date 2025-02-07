package cloud.xcan.sdf.core.angustester.interfaces.func.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseAttachmentReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseResultModifyDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseReviewDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseTagReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseTesterReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.FuncCaseWorkloadReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseReviewVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface FuncCaseFacade {

  List<IdKey<Long, Object>> add(List<FuncCaseAddDto> dto);

  void update(List<FuncCaseUpdateDto> dto);

  List<IdKey<Long, Object>> replace(List<FuncCaseReplaceDto> dto);

  void rename(Long id, String name);

  List<IdKey<Long, Object>> clone(Set<Long> ids);

  void move(Set<Long> ids, Long targetPlanId);

  void replaceTester(Long id, FuncCaseTesterReplaceDto dto);

  void replaceTag(Long id, FuncCaseTagReplaceDto dto);

  void replaceDeadline(Long id, LocalDateTime deadlineDate);

  void replacePriority(Long id, Priority priority);

  void replaceEvalWorkload(Long id, FuncCaseWorkloadReplaceDto dto);

  void replaceActualWorkload(Long id, FuncCaseWorkloadReplaceDto dto);

  void replaceAttachment(Long id, FuncCaseAttachmentReplaceDto dto);

  void resultReplace(List<FuncCaseResultModifyDto> dto);

  void resultUpdate(List<FuncCaseResultModifyDto> dto);

  void resultReset(HashSet<Long> ids);

  void retest(HashSet<Long> ids);

  void review(List<FuncCaseReviewDto> dto);

  void reviewReset(HashSet<Long> ids);

  void taskAssocAdd(Long id, HashSet<Long> assocTaskIds);

  void taskAssocCancel(Long id, HashSet<Long> assocTaskIds);

  void caseAssocAdd(Long id, HashSet<Long> assocCaseIds);

  void caseAssocCancel(Long id, HashSet<Long> assocCaseIds);

  void delete(Collection<Long> ids);

  List<TaskInfoVo> notAssociatedTask(Long id, Long moduleId, TaskType taskType);

  List<FuncCaseListVo> notAssociatedCase(Long id, Long moduleId);

  FuncCaseDetailVo detail(Long id);

  List<FuncCaseReviewVo> reviewList(Long id);

  PageResult<FuncCaseListVo> list(FuncCaseFindDto dto);

  PageResult<FuncCaseListVo> search(boolean exportFlag, FuncCaseSearchDto dto);

  List<IdKey<Long, Object>> exampleImport(Long projectId);

  List<IdKey<Long, Object>> imports(FuncCaseImportDto dto);

  ResponseEntity<Resource> export(FuncCaseSearchDto dto, HttpServletResponse response);

}
