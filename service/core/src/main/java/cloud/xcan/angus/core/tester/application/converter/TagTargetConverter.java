package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TagTargetConverter {

  public static List<TagTarget> toUpdateTaskTag(Set<Long> tagIds, TaskInfo taskDb) {
    // The time must be the same for different tags of the same task.
    return tagIds.stream().map(x -> new TagTarget()
        .setTargetType(CombinedTargetType.TASK)
        .setTargetId(taskDb.getId())
        .setTargetName(taskDb.getName())
        .setTagId(x)).toList();
  }

  public static List<TagTarget> toUpdateTaskTag(Set<Long> tagIds, Task taskDb) {
    // The time must be the same for different tags of the same task.
    return tagIds.stream().map(x -> new TagTarget()
        .setTargetType(CombinedTargetType.TASK)
        .setTargetId(taskDb.getId())
        .setTargetName(taskDb.getName())
        .setTagId(x)).toList();
  }

  public static List<TagTarget> toCaseTagTargets(Long caseId, Collection<Long> tagIds) {
    return isEmpty(tagIds) ? null : tagIds.stream()
        .map(tagId -> new TagTarget()
            .setTargetType(CombinedTargetType.FUNC_CASE)
            .setTargetId(caseId).setTagId(tagId))
        .toList();
  }

  public static List<TagTarget> toUpdateCaseTag(Collection<Long> tagIds, FuncCaseInfo caseDb) {
    // The time must be the same for different tags of the same task.
    return tagIds.stream().map(x -> new TagTarget()
        .setTargetType(CombinedTargetType.FUNC_CASE)
        .setTargetId(caseDb.getId())
        .setTargetName(caseDb.getName())
        .setTagId(x)).toList();
  }
}
