package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.DateUtils.asDate;
import static cloud.xcan.sdf.spec.utils.DateUtils.formatByDatePattern;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.core.angustester.domain.ExampleDataType;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.project.ProjectType;
import cloud.xcan.sdf.core.angustester.domain.project.summary.ProjectSummary;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrash;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class ProjectConverter {

  public static ProjectTrash toTaskTrash(Project project) {
    return new ProjectTrash()
        .setTargetId(project.getId())
        .setTargetName(project.getName())
        .setCreatedBy(project.getCreatedBy())
        .setDeletedBy(getUserId())
        .setDeletedDate(LocalDateTime.now());
  }

  public static ProjectSummary toProjectSummary(Project projectDb) {
    return new ProjectSummary()
        .setId(projectDb.getId()).setName(projectDb.getName())
        .setOwnerId(projectDb.getOwnerId()).setOwnerName(projectDb.getOwnerName())
        .setOwnerAvatar(projectDb.getOwnerAvatar())
        .setStartDate(nonNull(projectDb.getStartDate())
            ? formatByDatePattern(asDate(projectDb.getStartDate())) : null)
        .setDeadlineDate(nonNull(projectDb.getDeadlineDate())
            ? formatByDatePattern(asDate(projectDb.getDeadlineDate())) : null)
        .setDescription(projectDb.getDescription())
        .setMembers(projectDb.getMembers());
  }

  public static @NotNull Set<ExampleDataType> getSafeExampleDataTypes(ProjectType type,
      @Nullable Set<ExampleDataType> dataTypes) {
    Set<ExampleDataType> finalDataTypes = new HashSet<>(
        isNull(dataTypes) ? List.of(ExampleDataType.values()) : dataTypes);
    if (type.isTesting()) {
      finalDataTypes.remove(ExampleDataType.TASK);
    }
    if (finalDataTypes.contains(ExampleDataType.SCENARIO)) {
      finalDataTypes.remove(ExampleDataType.SCRIPT);
    }
    if (finalDataTypes.contains(ExampleDataType.EXECUTION)
        && !finalDataTypes.contains(ExampleDataType.SCENARIO)
        && !finalDataTypes.contains(ExampleDataType.SCRIPT)) {
      finalDataTypes.add(ExampleDataType.SCRIPT);
    }
    return finalDataTypes;
  }
}
