package cloud.xcan.angus.core.tester.application.cmd.tag;

import cloud.xcan.angus.core.tester.domain.func.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.task.Task;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TagTargetCmd {

  void add0(List<TagTarget> tagTargets);

  void replaceTaskTarget(List<TagTarget> tags);

  void replaceTaskTarget0(List<TagTarget> tags);

  void replaceTaskTags(Long taskId, Set<Long> tagIds);

  void replaceTaskTags0(Task taskDb, List<TagTarget> tagTargets);

  void delete0ByTaskIds(Collection<Long> taskIds);

  void addCase(List<FuncCase> cases);

  void updateCase(List<FuncCase> cases);

  void replaceCase(List<FuncCase> cases);

  void replaceCaseTargets(List<TagTarget> tags);

  void replaceCaseTags(Long caseId, Collection<Long> tagIds);

  void delete0ByCaseIds(Collection<Long> caseIds);

  void replace0(List<TagTarget> tags);

  void delete0(Collection<Long> tagIds);

  void deleteByTargetIdIn(List<Long> targetIds);
}
