package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.DateUtils.asDate;
import static cloud.xcan.sdf.spec.utils.DateUtils.formatByDatePattern;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.project.summary.ProjectSummary;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrash;
import java.time.LocalDateTime;

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
}
