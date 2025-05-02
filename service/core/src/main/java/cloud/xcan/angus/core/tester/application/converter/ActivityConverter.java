package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_ACTIVITY_LENGTH;
import static cloud.xcan.angus.spec.experimental.Assert.assertTrue;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_ROOT_PID;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.angus.spec.utils.DateUtils.DATE_TIME_FMT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.activity.MainTargetActivityResource;
import cloud.xcan.angus.core.tester.domain.activity.summary.ActivitySummary;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.tag.Tag;
import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.spec.locale.EnumValueMessage;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ActivityConverter {

  public static <T extends ActivityResource> Activity toActivity(CombinedTargetType targetType,
      T resource, ActivityType activityType, Object... params) {
    return createActivity(targetType, resource, activityType, PrincipalContext.get(), params);
  }

  public static List<Activity> toActivities(CombinedTargetType targetType,
      List<? extends ActivityResource> resources, ActivityType activityType,
      List<Object[]> params) {
    Principal principal = PrincipalContext.get();
    Long tenantId = nonNull(principal.getTenantId()) ? principal.getTenantId() : -1L;
    List<Activity> activities = new ArrayList<>(resources.size());
    for (int i = 0; i < resources.size(); i++) {
      Activity activity = createActivity(targetType, resources.get(i), activityType,
          principal, params.get(i));
      activity.setTenantId(tenantId);
      activities.add(activity);
    }
    return activities;
  }

  public static List<Activity> toActivities(CombinedTargetType targetType,
      List<? extends ActivityResource> resources, ActivityType activityType, Object... params) {
    Principal principal = PrincipalContext.get();
    Long tenantId = nonNull(principal.getTenantId()) ? principal.getTenantId() : -1L;
    List<Activity> activities = new ArrayList<>(resources.size());
    for (ActivityResource resource : resources) {
      Activity activity = createActivity(targetType, resource, activityType, principal, params);
      activity.setTenantId(tenantId);
      activities.add(activity);
    }
    return activities;
  }

  /**
   * Support max three parameters, need to keep order
   *
   * @param params The last parameter is the resource name
   */
  private static Activity createActivity(CombinedTargetType targetType, ActivityResource resource,
      ActivityType activityType, Principal principal, Object[] params) {
    Activity activity = new Activity().setType(activityType)
        .setTargetType(targetType)
        .setUserId(principal.getUserId()).setOptDate(LocalDateTime.now())
        .setDescription(getDescription(targetType, activityType, params))
        .setDetail(getDetail(targetType, resource, activityType, params));
    activity.setTenantId(principal.getTenantId());
    if (nonNull(resource)) {
      activity.setProjectId(resource.getProjectId())
          .setTargetId(resource.getId())
          .setParentTargetId(nullSafe(resource.getParentId(), DEFAULT_ROOT_PID))
          .setTargetName(resource.getName());
      if (resource instanceof MainTargetActivityResource) {
        activity.setMainTargetId(((MainTargetActivityResource) resource).getMainTargetId());
      }
    }
    return activity;
  }

  /**
   * The resource name does not need to be displayed in the description activity.
   */
  private static String getDescription(CombinedTargetType targetType, ActivityType activityType,
      Object[] params) {
    if (ObjectUtils.isEmpty(params)) {
      return message(activityType.getDescMessageKey(),
          new Object[]{targetType.getMessage()}, getDefaultLanguage().toLocale());
    }

    assertTrue(params.length <= 2, "Support max two parameters");
    if (params.length == 1) {
      // Move the resource name to the front
      return message(activityType.getDescMessageKey(),
          new Object[]{targetType.getMessage(), safeEnumString(params[0])
              , getDefaultLanguage().toLocale()});
    }
    // Move the resource name to the front
    return message(activityType.getDescMessageKey(),
        new Object[]{targetType.getMessage(), safeEnumString(params[0]),
            safeEnumString(params[1])}, getDefaultLanguage().toLocale());
  }

  /**
   * The resource name needs to be displayed in the detail activity.
   * <p>
   * Set the resource name to the second parameter position.
   */
  private static String getDetail(CombinedTargetType targetType, ActivityResource resource,
      ActivityType activityType, Object[] params) {
    if (ObjectUtils.isEmpty(params)) {
      return message(activityType.getDetailMessageKey(),
          new Object[]{targetType.getMessage(), "[" + resource.getName() + "]"
              , getDefaultLanguage().toLocale()});
    }
    assertTrue(params.length <= 2, "Support max two parameters");
    if (params.length == 1) {
      // Move the resource name to the front
      return message(activityType.getDetailMessageKey(),
          new Object[]{targetType.getMessage(), "[" + resource.getName() + "]",
              safeEnumString(params[0])},
          getDefaultLanguage().toLocale());
    }
    // Move the resource name to the front
    return message(activityType.getDetailMessageKey(),
        new Object[]{targetType.getMessage(), "[" + resource.getName() + "]",
            safeEnumString(params[0]), safeEnumString(params[1])}, getDefaultLanguage().toLocale());
  }

  private static String safeEnumString(Object param) {
    if (param instanceof EnumValueMessage) {
      return ((EnumValueMessage<?>) param).getMessage();
    }
    return param.toString();
  }

  public static List<String[]> activityParams(Collection<? extends ActivityResource> resources) {
    List<String[]> params = new ArrayList<>(resources.size());
    for (ActivityResource resource : resources) {
      params.add(new String[]{"[" + resource.getName() + "]"});
    }
    return params;
  }

  public static Activity toModifyTaskActivity(boolean replace, boolean hasModifyAssigness,
      boolean hasModifyConfirmors, boolean hasModifyTags, boolean hasModifyAttachments,
      Task task, Task taskDb, UserBase assigneeDb, UserBase confirmorDb, List<Tag> taskTagsDb,
      boolean hasModRefTasks, List<TaskInfo> refTasks, boolean hasModRefCases,
      List<FuncCaseInfo> refCases) {
    Activity mainActivity = toActivity(CombinedTargetType.TASK, taskDb, ActivityType.UPDATED);
    StringBuilder activity = new StringBuilder();
    StringBuilder safeActivity = new StringBuilder();

    boolean hasChanged = false;
    // Required params
    if (isNotEmpty(task.getName()) && !task.getName().equals(taskDb.getName())) {
      activity.append("<br/>").append(message(
          ActivityType.NAME_UPDATED.getDescMessageKey(), new Object[]{"", task.getName()}));
      hasChanged = true;
      if (activity.length() < MAX_ACTIVITY_LENGTH) {
        safeActivity = activity;
      }
    }

    if (nonNull(task.getTaskType()) && !task.getTaskType().equals(taskDb.getTaskType())) {
      activity.append("<br/>").append(message(
          ActivityType.TYPE_UPDATED.getDescMessageKey(), new Object[]{"", task.getTaskType()}));
      hasChanged = true;
      if (activity.length() < MAX_ACTIVITY_LENGTH) {
        safeActivity = activity;
      }
    }

    if (nonNull(task.getBugLevel()) && !task.getBugLevel().equals(taskDb.getBugLevel())) {
      activity.append("<br/>").append(message(
          ActivityType.BUG_LEVEL_UPDATED.getDescMessageKey(),
          new Object[]{"", task.getBugLevel()}));
      hasChanged = true;
      if (activity.length() < MAX_ACTIVITY_LENGTH) {
        safeActivity = activity;
      }
    }

    if (nonNull(task.getMissingBug()) && !task.getMissingBug()
        .equals(taskDb.getMissingBug())) {
      activity.append("<br/>").append(message(
          task.getMissingBug() ? ActivityType.MISSING_BUG_SET.getDescMessageKey()
              : ActivityType.MISSING_BUG_CLEAR.getDescMessageKey(), new Object[]{""}));
      hasChanged = true;
      if (activity.length() < MAX_ACTIVITY_LENGTH) {
        safeActivity = activity;
      }
    }

    // Required params
    if (isNotEmpty(task.getPriority())
        && !task.getPriority().equals(taskDb.getPriority())) {
      activity.append("<br/>").append(message(
          ActivityType.PRIORITY.getDescMessageKey(), new Object[]{"", task.getPriority()}));
      hasChanged = true;
      if (activity.length() < MAX_ACTIVITY_LENGTH) {
        safeActivity = activity;
      }
    }

    if (hasModifyAssigness) {
      if (nonNull(task.getAssigneeId())) {
        activity.append("<br/>").append(message(
            ActivityType.TASK_ASSIGNEE.getDescMessageKey(),
            new Object[]{"", assigneeDb.getFullName()}));
        hasChanged = true;
        if (activity.length() < MAX_ACTIVITY_LENGTH) {
          safeActivity = activity;
        }
      } else {
        if (replace && nonNull(taskDb.getAssigneeId())) {
          activity.append("<br/>").append(message(
              ActivityType.TASK_ASSIGNEE_CLEAR.getDescMessageKey(), new Object[]{""}));
          hasChanged = true;
          if (activity.length() < MAX_ACTIVITY_LENGTH) {
            safeActivity = activity;
          }
        }
      }
    }

    if (hasModifyConfirmors) {
      if (nonNull(task.getConfirmorId())) {
        activity.append("<br/>").append(message(
            ActivityType.TASK_CONFIRMOR.getDescMessageKey(),
            new Object[]{"", confirmorDb.getFullName()}));
        hasChanged = true;
        if (activity.length() < MAX_ACTIVITY_LENGTH) {
          safeActivity = activity;
        }
      } else {
        if (replace && nonNull(taskDb.getConfirmorId())) {
          activity.append("<br/>").append(message(
              ActivityType.TASK_CONFIRMOR_CLEAR.getDescMessageKey(), new Object[]{""}));
          hasChanged = true;
          if (activity.length() < MAX_ACTIVITY_LENGTH) {
            safeActivity = activity;
          }
        }
      }
    }

    if (hasModifyTags) {
      if (isNotEmpty(task.getTagTargets())) {
        activity.append("<br/>").append(message(
            ActivityType.TAG.getDescMessageKey(), new Object[]{"",
                taskTagsDb.stream().map(Tag::getName).collect(Collectors.joining(","))}));
        hasChanged = true;
        if (activity.length() < MAX_ACTIVITY_LENGTH) {
          safeActivity = activity;
        }
      } else {
        if (replace && isEmpty(task.getTagTargets()) && isNotEmpty(taskDb.getTagTargets())) {
          activity.append("<br/>").append(message(
              ActivityType.TAG_CLEAR.getDescMessageKey(), new Object[]{""}));
          hasChanged = true;
          if (activity.length() < MAX_ACTIVITY_LENGTH) {
            safeActivity = activity;
          }
        }
      }
    }

    if (isNotEmpty(task.getDeadlineDate())
        && !task.getDeadlineDate().equals(taskDb.getDeadlineDate())) {
      activity.append("<br/>").append(message(
          ActivityType.DEADLINE.getDescMessageKey(), new Object[]{"",
              task.getDeadlineDate().format(DATE_TIME_FMT)}));
      hasChanged = true;
      if (activity.length() < MAX_ACTIVITY_LENGTH) {
        safeActivity = activity;
      }
    } else {
      if (replace && isEmpty(task.getDeadlineDate()) && isNotEmpty(taskDb.getDeadlineDate())) {
        activity.append("<br/>").append(message(
            ActivityType.DEADLINE_CLEAR.getDescMessageKey(), new Object[]{""}));
        hasChanged = true;
        if (activity.length() < MAX_ACTIVITY_LENGTH) {
          safeActivity = activity;
        }
      }
    }

    if (isNotEmpty(task.getSoftwareVersion())
        && !task.getSoftwareVersion().equals(taskDb.getSoftwareVersion())) {
      activity.append("<br/>").append(message(
          ActivityType.SOFTWARE_VERSION_UPDATE.getDescMessageKey(), new Object[]{"", task.getSoftwareVersion()}));
      hasChanged = true;
      if (activity.length() < MAX_ACTIVITY_LENGTH) {
        safeActivity = activity;
      }
    } else {
      if (isEmpty(task.getSoftwareVersion()) && isNotEmpty(taskDb.getSoftwareVersion())) {
        activity.append("<br/>").append(message(
            ActivityType.SOFTWARE_VERSION_CLEAR.getDescMessageKey(), new Object[]{""}));
        hasChanged = true;
        if (activity.length() < MAX_ACTIVITY_LENGTH) {
          safeActivity = activity;
        }
      }
    }

    if (hasModifyAttachments) {
      if (isNotEmpty(task.getAttachments())) {
        activity.append("<br/>").append(message(
            ActivityType.ATTACHMENT_UPDATED.getDescMessageKey(), new Object[]{"",
                task.getAttachments().stream().map(x ->
                    "<a data-type=\"attachment\" data-href=\"" + x.getUrl() + "\">" + x.getName()
                        + "</a>").collect(Collectors.joining(","))}));
        hasChanged = true;
        if (activity.length() < MAX_ACTIVITY_LENGTH) {
          safeActivity = activity;
        }
      } else {
        if (replace && isEmpty(task.getAttachments()) && isNotEmpty(taskDb.getAttachments())) {
          activity.append("<br/>").append(message(
              ActivityType.ATTACHMENT_CLEAR.getDescMessageKey(), new Object[]{""}));
          hasChanged = true;
          if (activity.length() < MAX_ACTIVITY_LENGTH) {
            safeActivity = activity;
          }
        }
      }
    }

    if (isNotEmpty(task.getEvalWorkload())
        && !task.getEvalWorkload().equals(taskDb.getEvalWorkload())) {
      activity.append("<br/>").append(message(
          ActivityType.EVAL_WORKLOAD.getDescMessageKey(),
          new Object[]{taskDb.getName(), taskDb.getEvalWorkloadMethod(), task.getEvalWorkload()}));
      hasChanged = true;
      if (activity.length() < MAX_ACTIVITY_LENGTH) {
        safeActivity = activity;
      }
    } else {
      if (replace && isNull(task.getEvalWorkload()) && nonNull(taskDb.getEvalWorkload())) {
        activity.append("<br/>").append(message(
            ActivityType.EVAL_WORKLOAD_CLEAR.getDescMessageKey(),
            new Object[]{taskDb.getName(), taskDb.getEvalWorkloadMethod()}));
        hasChanged = true;
        if (activity.length() < MAX_ACTIVITY_LENGTH) {
          safeActivity = activity;
        }
      }
    }

    if (isNotEmpty(task.getActualWorkload())
        && !task.getActualWorkload().equals(taskDb.getActualWorkload())) {
      activity.append("<br/>").append(message(
          ActivityType.ACTUAL_WORKLOAD.getDescMessageKey(),
          new Object[]{taskDb.getName(), taskDb.getEvalWorkloadMethod(),
              task.getActualWorkload()}));
      hasChanged = true;
      if (activity.length() < MAX_ACTIVITY_LENGTH) {
        safeActivity = activity;
      }
    } else {
      if (replace && isNull(task.getActualWorkload()) && nonNull(taskDb.getActualWorkload())) {
        activity.append("<br/>").append(message(
            ActivityType.ACTUAL_WORKLOAD_CLEAR.getDescMessageKey(),
            new Object[]{taskDb.getName(), taskDb.getEvalWorkloadMethod()}));
        hasChanged = true;
        if (activity.length() < MAX_ACTIVITY_LENGTH) {
          safeActivity = activity;
        }
      }
    }

    if (isNotEmpty(task.getDescription())
        && !task.getDescription().equals(taskDb.getDescription())) {
      activity.append("<br/>").append(message(
          ActivityType.DESCRIPTION_UPDATED.getDescMessageKey(), new Object[]{""}));
      if (activity.length() < MAX_ACTIVITY_LENGTH) {
        safeActivity = activity;
      }
    } else {
      if (replace && isEmpty(task.getDescription()) && isNotEmpty(taskDb.getDescription())) {
        activity.append("<br/>").append(message(
            ActivityType.DESCRIPTION_CLEAR.getDescMessageKey(), new Object[]{""}));
        hasChanged = true;
        if (activity.length() < MAX_ACTIVITY_LENGTH) {
          safeActivity = activity;
        }
      }
    }

    if (hasModRefTasks) {
      if (isNotEmpty(task.getRefTaskIds())) {
        activity.append("<br/>").append(message(ActivityType.REF_TASK_UPDATE.getDescMessageKey(),
            new Object[]{refTasks.stream().map(TaskInfo::getName)
                .collect(Collectors.joining(","))}));
        hasChanged = true;
        if (activity.length() < MAX_ACTIVITY_LENGTH) {
          safeActivity = activity;
        }
      } else {
        if (replace && isEmpty(task.getRefTaskIds()) && isNotEmpty(taskDb.getRefTaskIds())) {
          activity.append("<br/>").append(message(ActivityType.REF_TASK_CLEAR.getDescMessageKey()));
          hasChanged = true;
          if (activity.length() < MAX_ACTIVITY_LENGTH) {
            safeActivity = activity;
          }
        }
      }
    }

    if (hasModRefCases) {
      if (isNotEmpty(task.getRefCaseIds())) {
        activity.append("<br/>").append(message(ActivityType.REF_CASE_UPDATE.getDescMessageKey(),
            new Object[]{refCases.stream().map(FuncCaseInfo::getName)
                .collect(Collectors.joining(","))}));
        hasChanged = true;
        if (activity.length() < MAX_ACTIVITY_LENGTH) {
          safeActivity = activity;
        }
      } else {
        if (replace && isEmpty(task.getRefCaseIds()) && isNotEmpty(taskDb.getRefCaseIds())) {
          activity.append("<br/>").append(message(ActivityType.REF_CASE_CLEAR.getDescMessageKey()));
          hasChanged = true;
          if (activity.length() < MAX_ACTIVITY_LENGTH) {
            safeActivity = activity;
          }
        }
      }
    }

    if (!hasChanged) {
      return mainActivity;
    }
    mainActivity.setDescription(mainActivity.getDescription() + safeActivity);
    mainActivity.setDetail(mainActivity.getDetail() + safeActivity);
    return mainActivity;
  }

  public static ActivitySummary toActivitySummary(Activity activity) {
    return new ActivitySummary().setId(activity.getId())
        .setProjectId(activity.getProjectId())
        .setUserId(activity.getUserId())
        .setFullName(activity.getFullName()).setAvatar(activity.getAvatar())
        .setTargetId(activity.getTargetId()).setTargetType(activity.getTargetType())
        .setTargetName(activity.getTargetName())
        .setParentTargetId(activity.getParentTargetId())
        .setOptDate(activity.getOptDate())
        .setDescription(activity.getDescription())
        .setDetail(activity.getDetail());
  }
}
