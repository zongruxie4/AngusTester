package cloud.xcan.angus.core.tester.application.cmd.test.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.FUNC_CASE_BID_KEY;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_FUNC_BASELINE_FILE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_FUNC_CASE_FILE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_FUNC_PLAN_FILE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_FUNC_REVIEW_FILE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.FuncActivityConverter.toModifyCaseActivity;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.assembleExampleFuncBaseline;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.assembleExampleFuncCase;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.assembleExampleFuncPlan;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.assembleExampleFuncReview;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.importToDomain;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.setReplaceInfo;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.setReviewInfoAndStatus;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.setTestInfoAndResult;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.CASE_IMPORT_COLUMNS;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.CASE_IMPORT_REQUIRED_COLUMNS;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ASSOC_CASE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ASSOC_CASE_CANCEL;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ASSOC_TASK;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ASSOC_TASK_CANCEL;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.DEADLINE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.DELETED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.EVAL_WORKLOAD;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.FUNC_TESTER;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.MOVED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.PRIORITY;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SOFTWARE_VERSION_CLEAR;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SOFTWARE_VERSION_UPDATE;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.parseSample;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.DateUtils.DATE_TIME_FMT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.duplicateByKey;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.remove;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.api.commonlink.user.User;
import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskFuncCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.TagTargetCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncBaselineCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncPlanCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncReviewCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncReviewCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncTrashCmd;
import cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.project.ModuleQuery;
import cloud.xcan.angus.core.tester.application.query.project.SoftwareVersionQuery;
import cloud.xcan.angus.core.tester.application.query.project.TagQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanQuery;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.comment.CommentRepo;
import cloud.xcan.angus.core.tester.domain.comment.CommentTargetType;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.project.module.Module;
import cloud.xcan.angus.core.tester.domain.project.tag.Tag;
import cloud.xcan.angus.core.tester.domain.project.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.favourite.FuncCaseFavouriteRepo;
import cloud.xcan.angus.core.tester.domain.test.follow.FuncCaseFollowRepo;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.test.review.cases.FuncReviewCaseRepo;
import cloud.xcan.angus.extraction.utils.PoiUtils;
import cloud.xcan.angus.idgen.BidGenerator;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command implementation for functional test case management operations.
 * <p>
 * Provides comprehensive CRUD operations for functional test cases including creation,
 * modification, deletion, import/export, and lifecycle management.
 * <p>
 * Implements business logic validation, permission checks, activity logging,
 * and transaction management for all case operations.
 * <p>
 * Supports batch operations, association management, test result tracking,
 * and review workflow integration.
 */
@Biz
public class FuncCaseCmdImpl extends CommCmd<FuncCase, Long> implements FuncCaseCmd {

  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;
  @Resource
  private FuncCaseFollowRepo funcCaseFollowRepo;
  @Resource
  private FuncCaseFavouriteRepo funcCaseFavouriteRepo;
  @Resource
  private FuncReviewCaseRepo funcReviewCaseRepo;
  @Resource
  private CommentRepo commentRepo;
  @Resource
  private FuncCaseQuery funcCaseQuery;
  @Resource
  private FuncCaseCmd funcCaseCmd;
  @Resource
  private TagQuery tagQuery;
  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;
  @Resource
  private FuncPlanQuery funcPlanQuery;
  @Resource
  private FuncPlanCmd funcPlanCmd;
  @Resource
  private SoftwareVersionQuery softwareVersionQuery;
  @Resource
  private ModuleQuery moduleQuery;
  @Resource
  private TagTargetCmd tagTargetCmd;
  @Resource
  private TaskFuncCaseCmd taskFuncCaseCmd;
  @Resource
  private FuncReviewCmd funcReviewCmd;
  @Resource
  private FuncReviewCaseCmd funcReviewCaseCmd;
  @Resource
  private FuncBaselineCmd funcBaselineCmd;
  @Resource
  private FuncTrashCmd funcTrashCmd;
  @Resource
  private TaskQuery taskQuery;
  @Resource
  private UserManager userManager;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds a batch of functional test cases to the system.
   * <p>
   * Performs comprehensive validation including plan existence, module validation,
   * user permissions, duplicate name checks, and quota limitations.
   * <p>
   * Creates associated tags, task relationships, and logs creation activities
   * for audit trail purposes.
   * <p>
   * All cases must belong to the same test plan for batch processing.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(List<FuncCase> cases) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Validate that all cases belong to the same plan
        Set<Long> planIds = cases.stream().map(FuncCase::getPlanId).collect(Collectors.toSet());
        assertTrue(planIds.size() == 1, "Only batch adding cases with one plan is allowed");
        planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());

        // Validate all referenced modules exist
        moduleQuery.checkAndFind(cases.stream()
            .map(FuncCase::getModuleId).collect(Collectors.toSet()));

        // Check user permission to add cases to the plan
        funcPlanAuthQuery.checkAddCaseAuth(getUserId(), planDb.getId());

        // Validate case names are unique within the plan
        funcCaseQuery.checkAddCaseNameExists(planDb, cases);

        // Check if adding these cases exceeds the plan's quota
        funcCaseQuery.checkCaseQuota(cases.size(), planDb.getId());
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Fill creation info for each case
        FuncCaseConverter.assembleAddInfo(cases, planDb);
        List<IdKey<Long, Object>> idKeys = batchInsert(cases, "name");

        // Add tags if present (optional)
        tagTargetCmd.addCase(cases);

        // Add task and case associations for each case
        for (FuncCase case0 : cases) {
          taskFuncCaseCmd.addAssoc(FUNC_CASE, case0.getId(), case0.getRefTaskIds(),
              case0.getRefCaseIds());
        }

        // Log creation activities for audit
        activityCmd.addAll(toActivities(FUNC_CASE, cases, ActivityType.CREATED));
        return idKeys;
      }
    }.execute();
  }

  /**
   * Updates a batch of functional test cases in the system.
   * <p>
   * Validates case existence, plan association, module references, user permissions,
   * and prevents duplicate names within the same plan.
   * <p>
   * Updates associated tags, task relationships, and logs modification activities
   * for audit trail and notification purposes.
   * <p>
   * Supports partial updates while maintaining data integrity and business rules.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<FuncCase> cases) {
    new BizTemplate<Void>() {
      List<FuncCase> updatedCasesDb;

      @Override
      protected void checkParams() {
        // Ensure all cases to update exist in DB
        updatedCasesDb = funcCaseQuery.checkAndFind(
            cases.stream().map(FuncCase::getId).toList());

        // Validate the plan for the cases
        FuncPlan planDb = funcPlanQuery.checkAndFind(updatedCasesDb.get(0).getPlanId());

        // Validate all referenced modules exist
        moduleQuery.checkAndFind(cases.stream()
            .map(FuncCase::getModuleId).collect(Collectors.toSet()));

        // Check user permission to modify cases in the plan
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), planDb.getId());

        // Validate updated names are unique within the plan
        funcCaseQuery.checkAndSafeUpdateNameExists(planDb, cases);
      }

      @Override
      protected Void process() {
        // Log modification activities and send notifications
        addModifyActivitiesAndEvents(false, cases, updatedCasesDb);

        // Update case info in DB
        FuncCaseConverter.assembleUpdateInfo(cases, updatedCasesDb);
        batchUpdate0(updatedCasesDb);

        // Update tags if present
        tagTargetCmd.updateCase(cases);

        // Update task and case associations for each case
        for (FuncCase case0 : updatedCasesDb) {
          taskFuncCaseCmd.updateAssoc(FUNC_CASE, case0.getId(), case0.getRefTaskIds(),
              case0.getRefCaseIds());
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces functional test cases by adding new ones and updating existing ones.
   * <p>
   * Handles both creation and modification in a single operation, managing tags,
   * task associations, and related data appropriately.
   * <p>
   * Logs comprehensive modification activities and sends notification events
   * for all affected cases.
   * <p>
   * Provides atomic operation ensuring data consistency across all related entities.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> replace(List<FuncCase> cases) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      List<FuncCase> updatedCases;
      List<FuncCase> updatedCasesDb;
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        updatedCases = cases.stream().filter(x -> nonNull(x.getId())).toList();
        if (isNotEmpty(updatedCases)) {
          // Ensure all cases to update exist in DB
          List<Long> ids = updatedCases.stream().map(FuncCase::getId).toList();
          updatedCasesDb = funcCaseQuery.checkAndFind(ids);

          // Validate the plan for the cases
          planDb = funcPlanQuery.checkAndFind(updatedCasesDb.get(0).getPlanId());

          // Validate all referenced modules exist
          moduleQuery.checkAndFind(updatedCases.stream().map(FuncCase::getModuleId)
              .collect(Collectors.toSet()));

          // Check user permission to modify cases in the plan
          funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), planDb.getId());

          // Validate updated names are unique within the plan
          funcCaseQuery.checkAndSafeUpdateNameExists(planDb, updatedCases);
        }
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = new ArrayList<>();
        List<FuncCase> addCases = cases.stream().filter(x -> isNull(x.getId()))
            .toList();
        if (isNotEmpty(addCases)) {
          // Add new cases
          idKeys.addAll(add(addCases));
        }

        if (isNotEmpty(updatedCases)) {
          // Log modification activities and send notifications
          addModifyActivitiesAndEvents(true, updatedCases, updatedCasesDb);

          // Update case info in DB
          Map<Long, FuncCase> updatedCasesMap = updatedCases.stream()
              .collect(Collectors.toMap(FuncCase::getId, x -> x));
          funcCaseRepo.batchUpdate(updatedCasesDb.stream()
              .map(x -> setReplaceInfo(x, updatedCasesMap.get(x.getId())))
              .toList());

          // Replace tags for updated cases
          tagTargetCmd.replaceCase(updatedCases);

          // Update task and case associations for each case
          for (FuncCase case0 : updatedCasesDb) {
            taskFuncCaseCmd.replaceAssoc(FUNC_CASE, case0.getId(), case0.getRefTaskIds(),
                case0.getRefCaseIds());
          }

          List<Activity> activities = toActivities(FUNC_CASE, updatedCasesDb, ActivityType.UPDATED);
          activityCmd.addAll(activities);

          idKeys.addAll(updatedCasesDb.stream()
              .map(x -> new IdKey<Long, Object>().setId(x.getId()).setKey(x.getName()))
              .toList());

          // Send modification events for updated cases
          funcCaseQuery.assembleAndSendModifyNoticeEvent(updatedCasesDb.stream()
              .map(x -> new FuncCaseInfo().setId(x.getId()).setName(x.getName())
                  .setTesterId(x.getTesterId()))
              .toList(), activities);
        }
        return idKeys;
      }
    }.execute();
  }

  /**
   * Renames a functional test case with validation and audit trail.
   * <p>
   * Validates user permissions and ensures the new name is unique within the plan
   * before performing the rename operation.
   * <p>
   * Updates related review case names and logs the rename activity
   * for audit and notification purposes.
   * <p>
   * Only performs the operation if the name has actually changed.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void rename(Long id, String name) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        assertNotNull(name, "Name is required");

        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFind(id);

        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());

        // Only check for duplicate if the name is actually changed
        if (!caseDb.getName().equals(name)) {
          funcCaseQuery.checkUpdateNameExists(caseDb.getPlanId(), name, id);
        }
      }

      @Override
      protected Void process() {
        // Only perform rename if the name is actually changed
        if (!name.equals(caseDb.getName())) {
          caseDb.setName(name);
          funcCaseRepo.save(caseDb);

          // Update related review case names
          funcReviewCaseRepo.updateNameByCaseId(name, id);

          // Log rename activity
          Activity activity = toActivity(FUNC_CASE, caseDb, ActivityType.NAME_UPDATED, name);
          activityCmd.add(activity);

          // Send notification event
          funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Clones a batch of functional test cases to create new instances.
   * <p>
   * Validates case existence and user permissions before creating duplicates
   * with unique names and preserved relationships.
   * <p>
   * Generates new case codes and ensures no naming conflicts while maintaining
   * all original case properties and associations.
   * <p>
   * Logs clone activities for audit trail and tracking purposes.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> clone(Set<Long> ids) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      List<FuncCase> casesDb;

      @Override
      protected void checkParams() {
        // Ensure all cases to clone exist in DB
        casesDb = funcCaseQuery.checkAndFind(ids);

        // Validate all referenced plans exist
        List<FuncPlan> plansDb = funcPlanQuery.checkAndFind(casesDb.stream()
            .map(FuncCase::getPlanId).collect(Collectors.toSet()));

        // Check user permission to add cases to all involved plans
        funcPlanAuthQuery.batchCheckPermission(plansDb.stream().map(FuncPlan::getId)
            .collect(Collectors.toSet()), FuncPlanPermission.ADD_CASE);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<FuncCase> newCases = new ArrayList<>();
        for (FuncCase caseDb : casesDb) {
          // Create a deep copy of the case with a new name and code
          FuncCase newCase = FuncCaseConverter.toCloneCase(caseDb);
          funcCaseQuery.setSafeCloneName(newCase);
          newCases.add(newCase);
        }

        List<IdKey<Long, Object>> idKeys = batchInsert(newCases, "name");

        // Log clone activities for audit
        activityCmd.addAll(toActivities(FUNC_CASE, newCases,
            ActivityType.CLONE, casesDb.stream().map(s -> new Object[]{s.getName()}).toList()));
        return idKeys;
      }
    }.execute();
  }

  /**
   * Moves functional test cases from one plan to another plan.
   * <p>
   * Validates case existence, target plan validity, and user permissions
   * before performing the move operation.
   * <p>
   * Updates project and plan associations, handles unplanned status,
   * and maintains data integrity across the move operation.
   * <p>
   * Logs move activities and sends notification events for affected cases.
   * <p>
   * Only allows moving cases within the same project directory structure.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void move(Set<Long> ids, Long targetPlanId) {
    new BizTemplate<Void>() {
      FuncPlan targetPlanDb;
      List<FuncCaseInfo> casesDb;
      //FuncDir dirDb;

      @Override
      protected void checkParams() {
        // Ensure all cases to move exist in DB
        casesDb = funcCaseQuery.checkAndFindInfo(ids);

        // Validate the target plan exists
        targetPlanDb = funcPlanQuery.checkAndFind(targetPlanId);

        // Ensure all cases are from the same project directory
        Set<Long> dirIds = casesDb.stream().map(FuncCaseInfo::getProjectId)
            .collect(Collectors.toSet());
        assertTrue(dirIds.size() == 1, "Only batch move cases with one dir is allowed");

        // Ensure the move is actually to a different plan
        assertTrue(!casesDb.get(0).getPlanId().equals(targetPlanId), "The moving position has not changed");

        // Check user permission to modify cases in the original plan
        funcPlanAuthQuery.batchCheckPermission(dirIds, FuncPlanPermission.MODIFY_CASE);

        // Check user permission to add cases to the target plan
        funcPlanAuthQuery.checkAddCaseAuth(getUserId(), targetPlanDb.getId());
      }

      @Override
      protected Void process() {
        // Update project and plan associations for all cases
        funcCaseRepo.updateProjectByIdIn(ids, targetPlanDb.getProjectId());
        funcCaseRepo.updatePlanByIdIn(ids, targetPlanId);
        funcCaseRepo.updateUnplannedByIdIn(ids, !targetPlanDb.getStatus().isStarted());

        // Log move activities for audit
        List<Activity> activities = toActivities(FUNC_CASE, casesDb, MOVED,
            casesDb.stream().map(s -> new Object[]{s.getName()}).toList());
        activityCmd.addAll(activities);

        // Send notification events for moved cases
        funcCaseQuery.assembleAndSendModifyNoticeEvent(casesDb, activities);
        return null;
      }
    }.execute();
  }

  /**
   * Replaces the assigned tester for a functional test case.
   * <p>
   * Validates case existence, user permissions, and new tester validity
   * before updating the assignment.
   * <p>
   * Only performs the update if the tester has actually changed,
   * and logs the activity for audit trail purposes.
   * <p>
   * Sends notification events to inform relevant stakeholders
   * about the tester change.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceTester(Long id, Long testerId) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;
      UserBase userDb = null;

      @Override
      protected void checkParams() {
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFind(id);

        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());

        // Ensure the new tester is a valid user
        userDb = userManager.checkValidAndFindUserBase(testerId);
      }

      @Override
      protected Void process() {
        // Only update if the tester is actually changed
        if (!testerId.equals(caseDb.getTesterId())) {
          caseDb.setTesterId(testerId);
          funcCaseRepo.save(caseDb);

          // Log tester replacement activity
          Activity activity = toActivity(FUNC_CASE, caseDb, FUNC_TESTER, userDb.getFullName());
          activityCmd.add(activity);

          // Send notification event for tester change
          funcCaseQuery.assembleAndSendModifyTesterNoticeEvent(caseDb);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Updates the priority level of a functional test case.
   * <p>
   * Validates case existence, user permissions, and priority value
   * before updating the case priority.
   * <p>
   * Only performs the update if the priority has actually changed,
   * and logs the activity for audit trail purposes.
   * <p>
   * Sends notification events to inform relevant stakeholders
   * about the priority change.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replacePriority(Long id, Priority priority) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        assertNotNull(priority, "Priority is required");
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFind(id);
        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Only update if the priority is actually changed
        if (!priority.equals(caseDb.getPriority())) {
          caseDb.setPriority(priority);
          funcCaseRepo.save(caseDb);

          // Log priority change activity
          Activity activity = toActivity(FUNC_CASE, caseDb, PRIORITY, priority.getMessage());
          activityCmd.add(activity);

          // Send notification event for priority change
          funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replace the software version of a functional test case.
   * <p>
   * Checks permission and version existence before updating.
   * <p>
   * Logs version update or clear activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceSoftwareVersion(Long id, String version) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFind(id);
        // If version is not empty, check it does not already exist
        if (isNotEmpty(version)) {
          softwareVersionQuery.checkNotExits(caseDb.getProjectId(), version);
        }
        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Clear version if input is empty
        if (isEmpty(version)) {
          caseDb.setSoftwareVersion(null);
          funcCaseRepo.save(caseDb);

          // Log version clear activity
          Activity activity = toActivity(FUNC_CASE, caseDb, SOFTWARE_VERSION_CLEAR, version);
          activityCmd.add(activity);
          return null;
        }
        // Only update if the version is actually changed
        if (!version.equals(caseDb.getSoftwareVersion())) {
          caseDb.setSoftwareVersion(version);
          funcCaseRepo.save(caseDb);

          // Log version update activity
          Activity activity = toActivity(FUNC_CASE, caseDb, SOFTWARE_VERSION_UPDATE, version);
          activityCmd.add(activity);
        }
        return null;
      }
    }.execute();
  }


  /**
   * Replace the evaluation workload of a functional test case.
   * <p>
   * Checks permission and updates the evaluation workload, logging the activity and sending notification event.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceEvalWorkload(Long id, BigDecimal evalWorkload) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFind(id);
        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Clear evaluation workload if input is null
        if (Objects.isNull(evalWorkload)) {
          if (nonNull(caseDb.getEvalWorkload())) {
            // Log workload clear activity
            Activity activity = toActivity(FUNC_CASE, caseDb,
                ActivityType.EVAL_WORKLOAD_CLEAR, caseDb.getEvalWorkloadMethod());
            activityCmd.add(activity);

            caseDb.setEvalWorkload(null).setActualWorkload(null);
            funcCaseRepo.save(caseDb);

            // Send notification event for workload clear
            funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
          }
          return null;
        }
        // Only update if the workload is actually changed
        if (!evalWorkload.equals(caseDb.getEvalWorkload())) {
          caseDb.setEvalWorkload(evalWorkload);
          funcCaseRepo.save(caseDb);

          // Log workload update activity
          Activity activity = toActivity(FUNC_CASE, caseDb, EVAL_WORKLOAD,
              caseDb.getEvalWorkloadMethod(), evalWorkload);
          activityCmd.add(activity);

          // Send notification event for workload update
          funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replace the actual workload of a functional test case.
   * <p>
   * Checks permission and updates the actual workload, logging the activity and sending notification event.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceActualWorkload(Long id, BigDecimal actualWorkload) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFind(id);
        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());
      }

      @Override
      protected Void process() {
        // If actualWorkload is set but evalWorkload is not, set evalWorkload as well
        if (nonNull(actualWorkload) && isNull(caseDb.getEvalWorkload())) {
          caseDb.setEvalWorkload(actualWorkload);
        }
        // Clear actual workload if input is null
        if (isNull(actualWorkload)) {
          if (nonNull(caseDb.getActualWorkload())) {
            // Log actual workload clear activity
            Activity activity = toActivity(FUNC_CASE, caseDb,
                ActivityType.ACTUAL_WORKLOAD_CLEAR, caseDb.getEvalWorkloadMethod());
            activityCmd.add(activity);

            caseDb.setActualWorkload(null);
            funcCaseRepo.save(caseDb);

            // Send notification event for actual workload clear
            funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
          }
          return null;
        }
        // Only update if the actual workload is actually changed
        if (!actualWorkload.equals(caseDb.getActualWorkload())) {
          caseDb.setActualWorkload(actualWorkload);
          funcCaseRepo.save(caseDb);

          // Log actual workload update activity
          Activity activity = toActivity(FUNC_CASE, caseDb,
              ActivityType.ACTUAL_WORKLOAD, caseDb.getEvalWorkloadMethod(), actualWorkload);
          activityCmd.add(activity);

          // Send notification event for actual workload update
          funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replace the attachments of a functional test case.
   * <p>
   * Checks permission and updates attachments, logging the activity and sending notification event.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceAttachment(Long id, List<Attachment> attachments) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFind(id);
        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Clear attachments if input is empty
        if (isEmpty(attachments)) {
          if (isNotEmpty(caseDb.getAttachments())) {
            // Log attachment clear activity
            Activity activity = toActivity(FUNC_CASE, caseDb,
                ActivityType.ATTACHMENT_CLEAR, caseDb.getAttachments().stream().map(x ->
                    "<a data-type=\"attachment\" data-href=\"" + x.getUrl() + "\">" + x.getName()
                        + "</a>").collect(Collectors.joining(",")));
            activityCmd.add(activity);

            caseDb.setAttachments(null);
            funcCaseRepo.save(caseDb);

            // Send notification event for attachment clear
            funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
          }
          return null;
        }
        // Only update if the attachments are actually changed
        if (funcCaseQuery.hasModifyAttachments(attachments, caseDb)) {
          caseDb.setAttachments(attachments);
          funcCaseRepo.save(caseDb);

          // Log attachment update activity
          Activity activity = toActivity(FUNC_CASE, caseDb,
              ActivityType.ATTACHMENT_UPDATED, attachments.stream().map(x ->
                  "<a data-type=\"attachment\" data-href=\"" + x.getUrl() + "\">" + x.getName()
                      + "</a>").collect(Collectors.joining(",")));
          activityCmd.add(activity);

          // Send notification event for attachment update
          funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Modify the test result of a batch of functional test cases.
   * <p>
   * Checks permission, plan status, and review status before updating test results.
   * <p>
   * Logs result update activities and sends notification events.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void resultModify(List<FuncCase> cases, boolean replace) {
    new BizTemplate<Void>() {
      List<FuncCase> casesDb;

      @Override
      protected void checkParams() {
        // Ensure all cases to modify exist in DB
        casesDb = funcCaseQuery.checkAndFind(cases.stream().map(FuncCase::getId).toList());

        // Validate all cases belong to the same plan
        Set<Long> planIds = casesDb.stream().map(FuncCase::getPlanId).collect(Collectors.toSet());
        assertTrue(planIds.size() == 1, "Only batch update cases with one plan is allowed");
        FuncPlan planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());

        // Check user permission to modify test results
        funcPlanAuthQuery.checkTestAuth(getUserId(), planDb.getId());

        // Ensure the plan has started (test results can only be modified after plan starts)
        funcPlanQuery.checkHasStarted(planDb);

        // If plan requires review, ensure all cases have passed review
        if (planDb.getReview()) {
          funcCaseQuery.checkReviewPassed(casesDb);
        }
      }

      @Override
      protected Void process() {
        // Update test results and related info for each case
        Map<Long, FuncCase> casesMap = cases.stream()
            .collect(Collectors.toMap(FuncCase::getId, x -> x));
        for (FuncCase caseDb : casesDb) {
          setTestInfoAndResult(caseDb, casesMap.get(caseDb.getId()), replace);
        }
        funcCaseRepo.saveAll(casesDb);

        // Log activities and send notifications grouped by test result
        addActivitiesAndEvents();
        return null;
      }

      private void addActivitiesAndEvents() {
        // Group cases by test result for batch activity logging
        Map<CaseTestResult, List<Long>> resultCaseIdMap = casesDb.stream()
            .collect(Collectors.groupingBy(FuncCase::getTestResult,
                Collectors.mapping(FuncCase::getId, Collectors.toList())));
        Map<Long, String> caseNameDbMap = casesDb.stream()
            .collect(Collectors.toMap(FuncCase::getId, FuncCase::getName));

        for (CaseTestResult caseTestResult : resultCaseIdMap.keySet()) {
          // Create case objects with names for activity logging
          Map<CaseTestResult, List<FuncCase>> resultCaseMap = casesDb.stream()
              .peek(x -> x.setName(caseNameDbMap.get(x.getId())))
              .collect(Collectors.groupingBy(FuncCase::getTestResult));
          List<FuncCase> resultCases = resultCaseMap.get(caseTestResult);

          // Log activities for this test result group
          List<Activity> activities = toActivities(FUNC_CASE, resultCases,
              ActivityType.RESULT_UPDATE, caseTestResult);
          activityCmd.addAll(activities);

          // Send notification events for cases with this test result
          funcCaseQuery.assembleAndSendModifyNoticeEvent(resultCases.stream().map(
                  x -> new FuncCaseInfo().setId(x.getId()).setName(x.getName())
                      .setTesterId(x.getTesterId()))
              .toList(), activities);
        }
      }
    }.execute();
  }

  /**
   * Reset the test result of a batch of functional test cases.
   * <p>
   * Checks permission before resetting test results, logs activities, and sends notification events.
   *
   * Note: Manually modifying the results of API testing cases is also permitted.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void resultReset(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncCaseInfo> casesDb;

      @Override
      protected void checkParams() {
        // Ensure all cases to reset exist in DB
        casesDb = funcCaseQuery.checkAndFindInfo(ids);

        // Validate all cases belong to the same plan
        Set<Long> planIds = casesDb.stream().map(FuncCaseInfo::getPlanId)
            .collect(Collectors.toSet());
        assertTrue(planIds.size() == 1, "Only batch update cases with one plan is allowed");
        FuncPlan planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());

        // Check user permission to reset test results
        funcPlanAuthQuery.checkResetTestResultAuth(getUserId(), planDb.getId());
      }

      @Override
      protected Void process() {
        // Reset test results to initial state for all cases
        funcCaseRepo.updateTestResultToInitByIds(ids);

        // Log reset activities for audit
        List<Activity> activities = toActivities(FUNC_CASE, casesDb, ActivityType.RESULT_RESET);
        activityCmd.addAll(activities);

        // Send notification events for reset cases
        funcCaseQuery.assembleAndSendModifyNoticeEvent(casesDb, activities);
        return null;
      }
    }.execute();
  }

  /**
   * Retest a batch of functional test cases.
   * <p>
   * Checks permission and case status before resetting test results to pending.
   * <p>
   * Logs retest activities and sends notification events.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void retest(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncCase> casesDb;

      @Override
      protected void checkParams() {
        // Ensure all cases to retest exist in DB
        casesDb = funcCaseQuery.checkAndFind(ids);

        // Validate all cases belong to the same plan
        Set<Long> planIds = casesDb.stream().map(FuncCase::getPlanId)
            .collect(Collectors.toSet());
        assertTrue(planIds.size() == 1, "Only batch update cases with one plan is allowed");
        FuncPlan planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());

        // Check user permission to modify test results
        funcPlanAuthQuery.checkTestAuth(getUserId(), planDb.getId());

        // Ensure all cases have completed testing (only finished cases can be retested)
        FuncCase invalidCase = casesDb.stream().filter(x -> !x.getTestResult().isWideFinished())
            .findFirst().orElse(null);
        assertTrue(null == invalidCase, "Incomplete cases are not allowed to be retested");
      }

      @Override
      protected Void process() {
        // Set all cases to pending status for retesting
        for (FuncCase funcCase : casesDb) {
          funcCase.setTestResult(CaseTestResult.PENDING);
        }
        funcCaseRepo.saveAll(casesDb);

        // Log retest start activities for audit
        List<Activity> activities = toActivities(FUNC_CASE, casesDb, ActivityType.RETEST_START);
        activityCmd.addAll(activities);

        // Send notification events for retest cases
        funcCaseQuery.assembleAndSendModifyNoticeEvent(
            casesDb.stream().map(x -> new FuncCaseInfo().setId(x.getId())
                    .setName(x.getName()).setTesterId(x.getTesterId()))
                .toList(), activities);
        return null;
      }
    }.execute();
  }

  /**
   * Review a batch of functional test cases.
   * <p>
   * Checks permission, plan status, and review enablement before updating review status.
   * <p>
   * Logs review activities.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void review(List<FuncCase> cases) {
    new BizTemplate<Void>() {
      List<FuncCase> casesDb;

      @Override
      protected void checkParams() {
        // Ensure all cases to review exist in DB
        casesDb = funcCaseQuery.checkAndFind(cases.stream().map(FuncCase::getId)
            .toList());

        // Validate all cases belong to the same plan
        Set<Long> planIds = casesDb.stream().map(FuncCase::getPlanId).collect(Collectors.toSet());
        ProtocolAssert.assertTrue(planIds.size() == 1,
            "Only batch update cases with one plan is allowed");
        FuncPlan planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());

        // Check user permission to review cases
        funcPlanAuthQuery.checkReviewAuth(getUserId(), planDb.getId());

        // Ensure the plan has started (reviews can only be performed after plan starts)
        funcPlanQuery.checkHasStarted(planDb);

        // Ensure review is enabled for the plan
        funcPlanQuery.checkReviewEnabled(planDb);

        // Validate that all cases are eligible for review
        funcCaseQuery.checkCanReview(casesDb);
      }

      @Override
      protected Void process() {
        // Update review status and related info for each case
        Map<Long, FuncCase> reviewCaseMap = cases.stream()
            .collect(Collectors.toMap(FuncCase::getId, x -> x));
        for (FuncCase caseDb : casesDb) {
          FuncCase case0 = reviewCaseMap.get(caseDb.getId());
          setReviewInfoAndStatus(caseDb, case0);
        }
        funcCaseRepo.saveAll(casesDb);

        // Log review activities grouped by review status
        addReviewActivities(casesDb);
        return null;
      }
    }.execute();
  }

  /**
   * Reset the review result of a batch of functional test cases.
   * <p>
   * Checks permission before resetting review results, logs activities, and sends notification events.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void reviewReset(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncCaseInfo> casesDb;

      @Override
      protected void checkParams() {
        // Ensure all cases to reset review exist in DB
        casesDb = funcCaseQuery.checkAndFindInfo(ids);

        // Validate all cases belong to the same plan
        Set<Long> planIds = casesDb.stream().map(FuncCaseInfo::getPlanId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(planIds.size() == 1,
            "Only batch update cases with one plan is allowed");
        FuncPlan planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());

        // Check user permission to reset review results
        funcPlanAuthQuery.checkResetReviewResultAuth(getUserId(), planDb.getId());
      }

      @Override
      protected Void process() {
        // Reset review results to initial state for all cases
        funcCaseRepo.updateReviewResultToInitByIds(ids);

        // Log review reset activities for audit
        List<Activity> activities = toActivities(FUNC_CASE, casesDb, ActivityType.RESULT_RESET);
        activityCmd.addAll(activities);

        // Send notification events for review reset cases
        funcCaseQuery.assembleAndSendModifyNoticeEvent(casesDb, activities);
        return null;
      }
    }.execute();
  }

  /**
   * Add associated tasks to a functional test case.
   * <p>
   * Checks permission and existence before associating tasks and logging the activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void taskAssocAdd(Long id, HashSet<Long> assocTaskIds) {
    new BizTemplate<Void>() {
      FuncCaseInfo caseDb;
      List<TaskInfo> assocTasksDb;

      @Override
      protected void checkParams() {
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFindInfo(id);

        // Ensure all associated tasks exist
        assocTasksDb = taskQuery.checkAndFindInfo(assocTaskIds);

        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        // Add task associations to the case
        taskFuncCaseCmd.addAssoc(FUNC_CASE, id, assocTaskIds, null);

        // Log task association activity
        Activity activity = toActivity(FUNC_CASE, caseDb, ASSOC_TASK,
            assocTasksDb.stream().map(TaskInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  /**
   * Cancel associated tasks from a functional test case.
   * <p>
   * Checks permission and existence before removing task associations and logging the activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void taskAssocCancel(Long id, HashSet<Long> assocTaskIds) {
    new BizTemplate<Void>() {
      FuncCaseInfo caseDb;
      List<TaskInfo> assocTasksDb;

      @Override
      protected void checkParams() {
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFindInfo(id);

        // Ensure all associated tasks exist
        assocTasksDb = taskQuery.checkAndFindInfo(assocTaskIds);

        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        // Remove task associations from the case
        taskFuncCaseCmd.deleteAssoc(FUNC_CASE, id, assocTaskIds, null);

        // Log task association cancellation activity
        Activity activity = toActivity(FUNC_CASE, caseDb, ASSOC_TASK_CANCEL,
            assocTasksDb.stream().map(TaskInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  /**
   * Add associated cases to a functional test case.
   * <p>
   * Checks permission and existence before associating cases and logging the activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void caseAssocAdd(Long id, HashSet<Long> assocCaseIds) {
    new BizTemplate<Void>() {
      FuncCaseInfo caseDb;
      List<FuncCaseInfo> assocCasesDb;

      @Override
      protected void checkParams() {
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFindInfo(id);

        // Ensure all associated cases exist
        assocCasesDb = funcCaseQuery.checkAndFindInfo(assocCaseIds);

        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        // Add case associations to the case
        taskFuncCaseCmd.addAssoc(FUNC_CASE, id, null, assocCaseIds);

        // Log case association activity
        Activity activity = toActivity(FUNC_CASE, caseDb, ASSOC_CASE,
            assocCasesDb.stream().map(FuncCaseInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  /**
   * Cancel associated cases from a functional test case.
   * <p>
   * Checks permission and existence before removing case associations and logging the activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void caseAssocCancel(Long id, HashSet<Long> assocCaseIds) {
    new BizTemplate<Void>() {
      FuncCaseInfo caseDb;
      List<FuncCaseInfo> assocCasesDb;

      @Override
      protected void checkParams() {
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFindInfo(id);

        // Ensure all associated cases exist
        assocCasesDb = funcCaseQuery.checkAndFindInfo(assocCaseIds);

        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        // Remove case associations from the case
        taskFuncCaseCmd.deleteAssoc(FUNC_CASE, id, null, assocCaseIds);

        // Log case association cancellation activity
        Activity activity = toActivity(FUNC_CASE, caseDb, ASSOC_CASE_CANCEL,
            assocCasesDb.stream().map(FuncCaseInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }


  /**
   * Import functional test cases from an Excel file.
   * <p>
   * Checks permission, validates file content, and handles duplicate strategies.
   * <p>
   * Converts imported data to domain objects and saves them.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> imports(Long planId,
      StrategyWhenDuplicated strategyWhenDuplicated, MultipartFile file) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Ensure the plan exists and user has permission to add cases
        planDb = funcPlanQuery.checkAndFind(planId);
        funcPlanAuthQuery.checkAddCaseAuth(getUserId(), planId);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Parse Excel file content
        List<String[]> rows;
        try {
          rows = PoiUtils.readExcel(file.getInputStream());
        } catch (IOException e) {
          throw ProtocolException.of("Failed to read excel file, cause: " + e.getMessage());
        }
        assertNotEmpty(rows, "Read excel content is empty");

        // Validate header row has no empty values
        List<String> titles = Stream.of(rows.get(0)).map(x -> remove(stringSafe(x), "*")).toList();
        assertTrue(titles.stream().noneMatch(ObjectUtils::isEmpty), "Title has empty value name");

        // Ensure all required import columns are present
        String missingRequiredField = CASE_IMPORT_REQUIRED_COLUMNS.stream()
            .filter(x -> !titles.contains(x)).findFirst().orElse(null);
        assertTrue(isEmpty(missingRequiredField),
            String.format("The required field %s is missing", missingRequiredField));

        List<String[]> data = rows.subList(1, rows.size());
        assertNotEmpty(data, "Read case data is empty");

        // Map column indices for data extraction
        int nameIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(0));
        int moduleIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(1));
        int testerIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(2));
        int developerIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(3));
        int reviwerIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(4));
        int priorityIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(5));
        int deadlineIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(6));
        int preconditionIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(7));
        int stepIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(8));
        int descriptionIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(9));
        int evalWorkloadIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(10));
        int actualWorkloadIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(11));
        int testResultIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(12));
        int reviewStatusIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(13));
        int testProcessedDateIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(14));
        int reviewDateIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(15));
        int tagsIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(16));
        int tasksIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(17));
        int casesIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(18));
        int creatorIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(19));
        int createdDateIdx = titles.indexOf(CASE_IMPORT_COLUMNS.get(20));

        // Validate required field values and check for duplicates
        assertTrue(nameIdx != -1, "Case name is required");
        List<String> names = data.stream().map(x -> x[nameIdx]).toList();
        List<String> duplicateNames = names.stream().filter(duplicateByKey(x -> x)).toList();
        assertTrue(isEmpty(duplicateNames),
            String.format("There are duplicates in the import case, duplicate case name: %s",
                duplicateNames));
        boolean hasEmptyName = names.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyName, "The import case name cannot be empty");

        // Validate tester field
        assertTrue(testerIdx != -1, "Case tester is required");
        Set<String> testers = data.stream().map(x -> x[testerIdx]).collect(Collectors.toSet());
        boolean hasEmptyTester = testers.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyTester, "The import tester cannot be empty");
        Map<String, List<UserBase>> testerMap = userManager.checkValidAndFindUserBasesByName(
            testers);

        // Validate developer field
        assertTrue(developerIdx != -1, "Case developer is required");
        Set<String> developers = data.stream().map(x -> x[developerIdx])
            .collect(Collectors.toSet());
        boolean hasEmptyDeveloper = developers.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyDeveloper, "The import developer cannot be empty");
        Map<String, List<UserBase>> developerMap = userManager.checkValidAndFindUserBasesByName(
            developers);

        // Validate deadline field
        assertTrue(deadlineIdx != -1, "Case deadline date is required");
        List<String> deadlines = data.stream().map(x -> x[deadlineIdx]).toList();
        boolean hasEmptyDeadlines = deadlines.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyDeadlines, "The import deadline date cannot be empty");

        // Validate referenced entities exist
        Set<String> modules = data.stream()
            .filter(x -> moduleIdx != -1 && isNotEmpty(x[moduleIdx]))
            .map(x -> x[moduleIdx]).collect(Collectors.toSet());
        Map<String, Module> modulesMap = moduleQuery.checkAndFindByName(
            planDb.getProjectId(), modules);

        Set<String> reviwers = data.stream()
            .filter(x -> reviwerIdx != -1 && isNotEmpty(x[reviwerIdx]))
            .map(x -> x[reviwerIdx]).collect(Collectors.toSet());
        Map<String, List<UserBase>> reviwersMap = userManager.checkValidAndFindUserBasesByName(
            reviwers);

        Set<String> creators = data.stream()
            .filter(x -> creatorIdx != -1 && isNotEmpty(x[creatorIdx]))
            .map(x -> x[creatorIdx]).collect(Collectors.toSet());
        Map<String, List<UserBase>> creatorsMap = userManager.checkValidAndFindUserBasesByName(
            creators);

        // Validate associated tags exist
        Set<String> tags = data.stream().filter(x -> tagsIdx != -1 && isNotEmpty(x[tagsIdx]))
            .map(x -> List.of(x[tagsIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<Tag>> tagsMap = tagQuery.checkAndFindByName(planDb.getProjectId(), tags);

        // Validate associated tasks exist
        Set<String> taskNames = data.stream()
            .filter(x -> tasksIdx != -1 && isNotEmpty(x[tasksIdx]))
            .map(x -> List.of(x[tasksIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<TaskInfo>> tasksMap = taskQuery.checkAndFindByProjectAndName(
            planDb.getProjectId(), taskNames);

        // Validate associated cases exist
        Set<String> caseNames = data.stream()
            .filter(x -> casesIdx != -1 && isNotEmpty(x[casesIdx]))
            .map(x -> List.of(x[casesIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<FuncCaseInfo>> casesMap = funcCaseQuery.checkAndFindByPlanAndName(
            planId, caseNames);

        // Convert import data to domain objects
        List<FuncCase> cases = importToDomain(
            uidGenerator, planDb, data, nameIdx, moduleIdx, modulesMap, testerIdx, testerMap,
            developerIdx, developerMap, reviwerIdx, reviwersMap, priorityIdx,
            deadlineIdx, preconditionIdx, stepIdx, descriptionIdx, evalWorkloadIdx,
            actualWorkloadIdx, testResultIdx, reviewStatusIdx, testProcessedDateIdx, reviewDateIdx,
            creatorIdx, creatorsMap, createdDateIdx, tagsIdx, tagsMap, tasksIdx, tasksMap,
            casesIdx, casesMap);

        // Handle duplicate strategy: cover existing or skip duplicates
        Set<String> safePrefixNames = isEmpty(planDb.getCasePrefix())
            ? new HashSet<>(names) : names.stream().map(x -> planDb.getCasePrefix() + x)
            .collect(Collectors.toSet());
        if (strategyWhenDuplicated.isCover()) {
          // Delete existing cases with same names
          funcCaseRepo.deleteByPlanIdAndNameIn(planId, safePrefixNames);
        } else {
          // Filter out cases that already exist
          List<String> namesDb = funcCaseRepo.findNamesByNameInAndPlanId(safePrefixNames, planId);
          cases = cases.stream().filter(x -> !namesDb.contains(x.getName()))
              .toList();
        }

        if (isEmpty(cases)) {
          return null;
        }

        // Save imported cases using the add method
        return add(cases);
      }
    }.execute();
  }

  /**
   * Import example functional test cases, plans, reviews, and baselines for a project.
   * <p>
   * Used for initializing sample data for demonstration or onboarding.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {


      @Override
      protected List<IdKey<Long, Object>> process() {
        // Get all tenant users for sample data creation
        List<User> users = userManager.findValidByTenantId(getOptTenantId());
        Assert.assertNotEmpty(users, "Tenant users are empty");

        // Create sample test plan from template file
        URL resourceUrl = this.getClass().getResource("/samples/plan/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_FUNC_PLAN_FILE);
        FuncPlan plan = parseSample(requireNonNull(resourceUrl),
            new TypeReference<FuncPlan>() {
            }, SAMPLE_FUNC_PLAN_FILE);
        assembleExampleFuncPlan(projectId, plan, users);
        funcPlanCmd.add(plan);

        // Create sample test cases from template file
        resourceUrl = this.getClass().getResource("/samples/cases/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_FUNC_CASE_FILE);
        List<FuncCase> cases = parseSample(requireNonNull(resourceUrl),
            new TypeReference<List<FuncCase>>() {
            }, SAMPLE_FUNC_CASE_FILE);
        for (FuncCase case0 : cases) {
          assembleExampleFuncCase(projectId, case0, plan, users);
        }
        List<IdKey<Long, Object>> idKeys = funcCaseCmd.add(cases);

        // Create sample case review from template file
        resourceUrl = this.getClass().getResource("/samples/review/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_FUNC_REVIEW_FILE);
        FuncReview review = parseSample(requireNonNull(resourceUrl),
            new TypeReference<FuncReview>() {
            }, SAMPLE_FUNC_REVIEW_FILE);
        assembleExampleFuncReview(projectId, review, plan, users);
        funcReviewCmd.add(review);

        // Associate pending cases with the review
        funcReviewCaseCmd.add(review.getId(), cases.stream()
            .filter(x -> nonNull(x.getReviewStatus()) && x.getReviewStatus().isPending())
            .map(FuncCase::getId).collect(Collectors.toSet()));

        // Create sample case baseline from template file
        resourceUrl = this.getClass().getResource("/samples/baseline/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_FUNC_BASELINE_FILE);
        FuncBaseline baseline = parseSample(requireNonNull(resourceUrl),
            new TypeReference<FuncBaseline>() {
            }, SAMPLE_FUNC_BASELINE_FILE);
        assembleExampleFuncBaseline(projectId, baseline, plan, cases, users);
        funcBaselineCmd.add(baseline);

        return idKeys;
      }
    }.execute();
  }

  /**
   * Delete a batch of functional test cases and related data.
   * <p>
   * Updates delete status, logs activities, moves cases to trash, and sends notification events.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncCaseInfo> casesDb;

      @Override
      protected void checkParams() {
        // Get case info for permission checking
        casesDb = funcCaseInfoRepo.findAll0ByIdIn(ids);

        if (isNotEmpty(casesDb)) {
          // Check user permission to delete cases in all involved plans
          funcPlanAuthQuery.batchCheckPermission(casesDb.stream().map(FuncCaseInfo::getPlanId)
              .collect(Collectors.toSet()), FuncPlanPermission.DELETE_CASE);
        }
      }

      @Override
      protected Void process() {
        if (isEmpty(casesDb)) {
          return null;
        }

        // Mark cases as deleted (soft delete)
        funcCaseRepo.updateDeleteStatus(ids, true, getUserId(), LocalDateTime.now());

        // Log deletion activities for audit
        List<Activity> activities = toActivities(FUNC_CASE, casesDb, DELETED,
            activityParams(casesDb));
        activityCmd.addAll(activities);

        // Move deleted cases to trash for potential recovery
        funcTrashCmd.add0(casesDb.stream().map(FuncCaseConverter::toFuncCaseTrash)
            .toList());

        // Send notification events for deleted cases
        funcCaseQuery.assembleAndSendModifyNoticeEvent(casesDb, activities);
        return null;
      }
    }.execute();
  }

  @Override
  public void delete0(List<Long> caseIds) {
    // Permanently delete cases from database
    funcCaseRepo.deleteAllByIdIn(caseIds);

    // Clean up case favorites
    funcCaseFollowRepo.deleteByCaseIdIn(caseIds);

    // Clean up case follows
    funcCaseFavouriteRepo.deleteByCaseIdIn(caseIds);

    // Clean up case comments
    commentRepo.deleteByTargetIdInAndTargetType(caseIds, CommentTargetType.FUNC_CASE.getValue());

    // Clean up case tag associations
    tagTargetCmd.delete0ByCaseIds(caseIds);

    // Clean up case reference associations
    taskFuncCaseCmd.deleteByTargetIds(caseIds);

    // Clean up case review associations
    funcReviewCmd.deleteByCaseId(caseIds);
  }

  /**
   * Add review activities for a batch of functional test cases.
   * <p>
   * Groups cases by review status for batch activity logging
   */
  @Override
  public void addReviewActivities(List<FuncCase> casesDb) {
    // Group cases by review status for batch activity logging
    Map<ReviewStatus, List<FuncCase>> reviewStatusMap = casesDb.stream()
        .collect(Collectors.groupingBy(FuncCase::getReviewStatus));

    for (ReviewStatus reviewStatus : reviewStatusMap.keySet()) {
      List<FuncCase> reviewCasesDb = reviewStatusMap.get(reviewStatus);

      // Log review activities for this status group
      List<Activity> activities = toActivities(FUNC_CASE, reviewCasesDb,
          ActivityType.REVIEW_UPDATE, reviewStatus);
      activityCmd.addAll(activities);

      // Send notification events for cases with this review status
      funcCaseQuery.assembleAndSendModifyNoticeEvent(reviewCasesDb.stream().map(
              x -> new FuncCaseInfo().setId(x.getId()).setName(x.getName())
                  .setTesterId(x.getTesterId()))
          .toList(), activities);
    }
  }

  /**
   * Add modification activities and send notification events for updated cases.
   * <p>
   * Determines what fields have changed and logs corresponding activities.
   * <p>
   * Sends notification events for all updated cases.
   */
  private void addModifyActivitiesAndEvents(boolean replace, List<FuncCase> cases,
      List<FuncCase> updatedCasesDb) {
    // Create maps for efficient lookup of existing data and user info
    Map<Long, FuncCase> caseDbMap = updatedCasesDb.stream()
        .collect(Collectors.toMap(FuncCase::getId, x -> x));
    Map<Long, UserBase> testerMap = userManager.checkValidAndFindUserBase(
            cases.stream().map(FuncCase::getTesterId).filter(Objects::nonNull)
                .collect(Collectors.toSet())).stream()
        .collect(Collectors.toMap(UserBase::getId, x -> x));

    List<Activity> activities = new ArrayList<>();
    for (FuncCase case0 : cases) {
      // Detect what fields have actually changed
      boolean hasModifyTester = nonNull(case0.getTesterId()) &&
          !Objects.equals(caseDbMap.get(case0.getId()).getTesterId(), case0.getTesterId());
      boolean hasModifyTaskTags = tagQuery
          .hasModifyTag(case0.getId(), case0.getTagTargets());
      boolean hasModifyAttachments = funcCaseQuery
          .hasModifyAttachments(case0.getAttachments(), caseDbMap.get(case0.getId()));

      // Get tag info for activity logging if tags were modified
      List<Tag> caseTagsDb = null;
      if (isNotEmpty(case0.getTagTargets())) {
        caseTagsDb = tagQuery.checkAndFind(case0.getTagTargets().stream()
            .map(TagTarget::getTagId).toList());
      }

      // Create modification activity based on what changed
      activities.add(toModifyCaseActivity(replace, hasModifyTester, hasModifyTaskTags,
          hasModifyAttachments, case0, caseDbMap.get(case0.getId()),
          testerMap.get(case0.getTesterId()), caseTagsDb));
    }

    // Log activities (null activities are ignored)
    activityCmd.addAll(activities);

    // Send notification events for all updated cases
    funcCaseQuery.assembleAndSendModifyNoticeEvent(updatedCasesDb.stream().map(
            x -> new FuncCaseInfo().setId(x.getId()).setName(x.getName()).setTesterId(x.getTesterId()))
        .toList(), activities);
  }

  /**
   * Generate a unique case code using the BidGenerator.
   * <p>
   * Used for assigning unique identifiers to new cases.
   */
  public static String getCaseCode() {
    return requireNonNull(getBean(BidGenerator.class)).getId(FUNC_CASE_BID_KEY, getTenantId());
  }

  /**
   * Get the repository for functional test cases.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<FuncCase, Long> getRepository() {
    return this.funcCaseRepo;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceDeadline(Long id, LocalDateTime deadline) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        assertNotNull(deadline, "Deadline is required");
        // Ensure the case exists
        caseDb = funcCaseQuery.checkAndFind(id);
        // Check user permission to modify the case
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Only update if the deadline is actually changed
        if (!deadline.equals(caseDb.getDeadlineDate())) {
          caseDb.setDeadlineDate(deadline).setOverdue(deadline.isBefore(LocalDateTime.now()));
          funcCaseRepo.save(caseDb);
          // Log deadline change activity
          activityCmd.add(toActivity(FUNC_CASE, caseDb, DEADLINE, deadline.format(DATE_TIME_FMT)));
        }
        return null;
      }
    }.execute();
  }
}
