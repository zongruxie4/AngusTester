package cloud.xcan.angus.core.tester.application.cmd.func.impl;

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
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.setTestInfoAndStatus;
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
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
import cloud.xcan.angus.core.tester.application.cmd.func.FuncBaselineCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncPlanCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncReviewCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncReviewCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncTrashCmd;
import cloud.xcan.angus.core.tester.application.cmd.tag.TagTargetCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskFuncCaseCmd;
import cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.application.query.module.ModuleQuery;
import cloud.xcan.angus.core.tester.application.query.tag.TagQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.version.SoftwareVersionQuery;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.comment.CommentRepo;
import cloud.xcan.angus.core.tester.domain.comment.CommentTargetType;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.func.favourite.FuncCaseFavouriteRepo;
import cloud.xcan.angus.core.tester.domain.func.follow.FuncCaseFollowRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.func.review.cases.FuncReviewCaseRepo;
import cloud.xcan.angus.core.tester.domain.module.Module;
import cloud.xcan.angus.core.tester.domain.tag.Tag;
import cloud.xcan.angus.core.tester.domain.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.extraction.utils.PoiUtils;
import cloud.xcan.angus.idgen.BidGenerator;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(List<FuncCase> cases) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Check the plan exists
        Set<Long> planIds = cases.stream().map(FuncCase::getPlanId).collect(Collectors.toSet());
        assertTrue(planIds.size() == 1,
            "Only batch adding cases with one plan is allowed");
        planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());

        // Check the module exists
        moduleQuery.checkAndFind(cases.stream()
            .map(FuncCase::getModuleId).collect(Collectors.toSet()));

        // Check the add permission
        funcPlanAuthQuery.checkAddCaseAuth(getUserId(), planDb.getId());

        // Check the deadline
        // funcCaseQuery.checkAndSafeCaseDeadline(cases, planDb.getDeadlineDate());

        // Check the names duplicate
        funcCaseQuery.checkAddCaseNameExists(planDb, cases);

        // Check the quota limit
        funcCaseQuery.checkCaseQuota(cases.size(), planDb.getId());
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Save cases
        FuncCaseConverter.assembleAddInfo(cases, planDb);
        List<IdKey<Long, Object>> idKeys = batchInsert(cases, "name");

        // Save tags and tag is not required
        tagTargetCmd.addCase(cases);

        // Save related tasks and use cases
        for (FuncCase case0 : cases) {
          taskFuncCaseCmd.addAssoc(FUNC_CASE, case0.getId(), case0.getRefTaskIds(),
              case0.getRefCaseIds());
        }

        // case.setActualWorkload(isNull(case.getEvalWorkload()) ? null : case.getActualWorkload()); -> Actual workload cannot be set when adding
        activityCmd.addAll(toActivities(FUNC_CASE, cases, ActivityType.CREATED));
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<FuncCase> cases) {
    new BizTemplate<Void>() {
      List<FuncCase> updatedCasesDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        updatedCasesDb = funcCaseQuery.checkAndFind(
            cases.stream().map(FuncCase::getId).collect(Collectors.toList()));

        // Check the plan exists
        FuncPlan planDb = funcPlanQuery.checkAndFind(updatedCasesDb.get(0).getPlanId());

        // Check the module exists
        moduleQuery.checkAndFind(cases.stream()
            .map(FuncCase::getModuleId).collect(Collectors.toSet()));

        // Check the update permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), planDb.getId());

        // Fix :: Check deadline -> Expired use cases are modifiable
        // funcCaseQuery.checkCaseDeadline(cases, planDb.getEndDate());

        // Check the names duplicate
        funcCaseQuery.checkAndSafeUpdateNameExists(planDb, cases);
      }

      @Override
      protected Void process() {
        // Save activities
        addModifyActivitiesAndEvents(false, cases, updatedCasesDb);

        // Update cases
        FuncCaseConverter.assembleUpdateInfo(cases, updatedCasesDb);
        batchUpdate0(updatedCasesDb);

        // Save tags and tag is not required
        tagTargetCmd.updateCase(cases);

        // Save related tasks and use cases
        for (FuncCase case0 : updatedCasesDb) {
          taskFuncCaseCmd.updateAssoc(FUNC_CASE, case0.getId(), case0.getRefTaskIds(),
              case0.getRefCaseIds());
        }

        // @DoInFuture("Add new review content change user and flag: story point, deadline, prerequisites, testing steps, description")

        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> replace(List<FuncCase> cases) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      List<FuncCase> updatedCases;
      List<FuncCase> updatedCasesDb;
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        updatedCases = cases.stream().filter(x -> nonNull(x.getId())).collect(Collectors.toList());
        if (isNotEmpty(updatedCases)) {
          // Check the cases exists
          List<Long> ids = updatedCases.stream().map(FuncCase::getId).collect(Collectors.toList());
          updatedCasesDb = funcCaseQuery.checkAndFind(ids);

          // Check the plan exists
          planDb = funcPlanQuery.checkAndFind(updatedCasesDb.get(0).getPlanId());

          // Check the module exists
          moduleQuery.checkAndFind(updatedCases.stream().map(FuncCase::getModuleId)
              .collect(Collectors.toSet()));

          // Check the update permission
          funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), planDb.getId());

          // Fix :: Check deadline -> Expired use cases are modifiable
          // funcCaseQuery.checkAndSafeCaseDeadline(updatedCases, planDb.getDeadlineDate());

          // Check the names duplicate
          funcCaseQuery.checkAndSafeUpdateNameExists(planDb, updatedCases);
        }
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = new ArrayList<>();
        List<FuncCase> addCases = cases.stream().filter(x -> isNull(x.getId()))
            .collect(Collectors.toList());
        if (isNotEmpty(addCases)) {
          idKeys.addAll(add(addCases));
        }

        if (isNotEmpty(updatedCases)) {
          // Save activities
          addModifyActivitiesAndEvents(true, updatedCases, updatedCasesDb);

          // Update cases
          Map<Long, FuncCase> updatedCasesMap = updatedCases.stream()
              .collect(Collectors.toMap(FuncCase::getId, x -> x));
          funcCaseRepo.batchUpdate(updatedCasesDb.stream()
              .map(x -> setReplaceInfo(x, updatedCasesMap.get(x.getId())))
              .collect(Collectors.toList()));

          // Replace tags
          tagTargetCmd.replaceCase(updatedCases);

          // Save related tasks and use cases
          for (FuncCase case0 : updatedCasesDb) {
            taskFuncCaseCmd.replaceAssoc(FUNC_CASE, case0.getId(), case0.getRefTaskIds(),
                case0.getRefCaseIds());
          }

          // @DoInFuture("Add new review content change user and flag: story point, deadline, prerequisites, testing steps, description")

          List<Activity> activities = toActivities(FUNC_CASE, updatedCasesDb, ActivityType.UPDATED);
          activityCmd.addAll(activities);

          idKeys.addAll(updatedCasesDb.stream()
              .map(x -> new IdKey<Long, Object>().setId(x.getId()).setKey(x.getName()))
              .collect(Collectors.toList()));

          // Add modification events
          funcCaseQuery.assembleAndSendModifyNoticeEvent(updatedCasesDb.stream()
              .map(x -> new FuncCaseInfo().setId(x.getId()).setName(x.getName())
                  .setTesterId(x.getTesterId()))
              .collect(Collectors.toList()), activities);
        }
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void rename(Long id, String name) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        assertNotNull(name, "Name is required");

        // Check and find case
        caseDb = funcCaseQuery.checkAndFind(id);

        // Check the add case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());

        if (!caseDb.getName().equals(name)) {
          // Check the update name exist
          funcCaseQuery.checkUpdateNameExists(caseDb.getPlanId(), name, id);
        }
      }

      @Override
      protected Void process() {
        if (!name.equals(caseDb.getName())) {
          caseDb.setName(name);
          funcCaseRepo.save(caseDb);

          funcReviewCaseRepo.updateNameByCaseId(name, id);

          Activity activity = toActivity(FUNC_CASE, caseDb, ActivityType.NAME_UPDATED, name);
          activityCmd.add(activity);

          // Add modification event
          funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> clone(Set<Long> ids) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      List<FuncCase> casesDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        casesDb = funcCaseQuery.checkAndFind(ids);

        // Check the plan exists
        List<FuncPlan> plansDb = funcPlanQuery.checkAndFind(casesDb.stream()
            .map(FuncCase::getPlanId).collect(Collectors.toSet()));

        // Check the update permission
        funcPlanAuthQuery.batchCheckPermission(plansDb.stream().map(FuncPlan::getId)
            .collect(Collectors.toSet()), FuncPlanPermission.ADD_CASE);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<FuncCase> newCases = new ArrayList<>();
        for (FuncCase caseDb : casesDb) {
          FuncCase newCase = FuncCaseConverter.toCloneCase(caseDb);
          funcCaseQuery.setSafeCloneName(newCase);
          newCases.add(newCase);
        }

        List<IdKey<Long, Object>> idKeys = batchInsert(newCases, "name");

        // Add clone activity
        activityCmd.addAll(toActivities(FUNC_CASE, newCases,
            ActivityType.CLONE, casesDb.stream().map(s -> new Object[]{s.getName()})
                .collect(Collectors.toList())));
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void move(Set<Long> ids, Long targetPlanId) {
    new BizTemplate<Void>() {
      FuncPlan targetPlanDb;
      List<FuncCaseInfo> casesDb;
      //FuncDir dirDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        casesDb = funcCaseQuery.checkAndFindInfo(ids);

        // Check the plan exists
        targetPlanDb = funcPlanQuery.checkAndFind(targetPlanId);

        // Check the dir exists
        Set<Long> dirIds = casesDb.stream().map(FuncCaseInfo::getProjectId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(dirIds.size() == 1,
            "Only batch move cases with one dir is allowed");
        //dirDb = funcDirQuery.checkAndFind(casesDb.get(0).getDirId());

        // Check the if the movement position has changed
        ProtocolAssert.assertTrue(!casesDb.get(0).getPlanId().equals(targetPlanId),
            "The moving position has not changed");

        // Check the to have permission to modify the case
        funcPlanAuthQuery.batchCheckPermission(dirIds, FuncPlanPermission.MODIFY_CASE);

        // Check the to have permission to add dir
        funcPlanAuthQuery.checkAddCaseAuth(getUserId(), targetPlanDb.getId());
      }

      @Override
      protected Void process() {
        // Note: After moving to the target directory, you have resource permissions under the directory.
        // Unlike scenarios and apis, you do not need to authorize the resource's permissions to target dir creators

        // Bug:: Grant permission
        // 1. Authorize new creator permissions for moving directories
        ////  2. @DoInFuture: Authorize the auth objects of the original directory to view the target directory
        //List<FuncDir> targetParentDirs = funcDirQuery.getTargetParentDir(targetPlanDb.getDirId());
        //Set<Long> newCreatorIds = funcDirQuery.getTargetParentCreatorIds(/*dirDb.getCreatedBy()*/
        //    casesDb.stream().map(FuncCaseInfo::getCreatedBy).collect(Collectors.toSet()),
        //    targetParentDirs);
        //funcDirAuthCmd.moveCreatorAuth(casesDb.get(0).getDirId(), newCreatorIds,
        //    targetParentDirs.stream().map(FuncDir::getId).collect(Collectors.toSet()));

        // Modify case projectId, planId and unplanned
        funcCaseRepo.updateProjectByIdIn(ids, targetPlanDb.getProjectId());
        funcCaseRepo.updatePlanByIdIn(ids, targetPlanId);
        funcCaseRepo.updateUnplannedByIdIn(ids, !targetPlanDb.getStatus().isStarted());

        // Add move case activity
        List<Activity> activities = toActivities(FUNC_CASE, casesDb, MOVED,
            casesDb.stream().map(s -> new Object[]{s.getName()}).collect(Collectors.toList()));
        activityCmd.addAll(activities);

        // Add modification event
        funcCaseQuery.assembleAndSendModifyNoticeEvent(casesDb, activities);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceTester(Long id, Long testerId) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;
      UserBase userDb = null;

      @Override
      protected void checkParams() {
        // Check and find case
        caseDb = funcCaseQuery.checkAndFind(id);

        // Check the modify case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());

        // Check the assignee
        userDb = userManager.checkValidAndFindUserBase(testerId);
      }

      @Override
      protected Void process() {
        if (!testerId.equals(caseDb.getTesterId())) {
          caseDb.setTesterId(testerId);
          funcCaseRepo.save(caseDb);

          Activity activity = toActivity(FUNC_CASE, caseDb, FUNC_TESTER, userDb.getFullName());
          activityCmd.add(activity);

          // Add modification event
          // funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
          funcCaseQuery.assembleAndSendModifyTesterNoticeEvent(caseDb);
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceDeadline(Long id, LocalDateTime deadline) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        assertNotNull(deadline, "Deadline is required");

        // Check and find case
        caseDb = funcCaseQuery.checkAndFind(id);

        // Check the modify case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());
      }

      @Override
      protected Void process() {
        if (!deadline.equals(caseDb.getDeadlineDate())) {
          caseDb.setDeadlineDate(deadline).setOverdue(deadline.isBefore(LocalDateTime.now()));
          funcCaseRepo.save(caseDb);

          activityCmd.add(toActivity(FUNC_CASE, caseDb, DEADLINE, deadline.format(DATE_TIME_FMT)));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replacePriority(Long id, Priority priority) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        assertNotNull(priority, "Priority is required");

        // Check and find case
        caseDb = funcCaseQuery.checkAndFind(id);

        // Check the modify case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());
      }

      @Override
      protected Void process() {
        if (!priority.equals(caseDb.getPriority())) {
          caseDb.setPriority(priority);
          funcCaseRepo.save(caseDb);

          Activity activity = toActivity(FUNC_CASE, caseDb, PRIORITY, priority.getMessage());
          activityCmd.add(activity);

          // Add modification event
          funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceSoftwareVersion(Long id, String version) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        // Check and find case
        caseDb = funcCaseQuery.checkAndFind(id);

        // Check the version not exists
        if (isNotEmpty(version)) {
          softwareVersionQuery.checkNotExits(caseDb.getProjectId(), version);
        }

        // Check the modify case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());
      }

      @Override
      protected Void process() {
        if (isEmpty(version)) {
          caseDb.setSoftwareVersion(null);
          funcCaseRepo.save(caseDb);

          Activity activity = toActivity(FUNC_CASE, caseDb, SOFTWARE_VERSION_CLEAR, version);
          activityCmd.add(activity);

          // Add modification event
          // caseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);

          return null;
        }
        if (!version.equals(caseDb.getSoftwareVersion())) {
          caseDb.setSoftwareVersion(version);
          funcCaseRepo.save(caseDb);

          Activity activity = toActivity(FUNC_CASE, caseDb, SOFTWARE_VERSION_UPDATE, version);
          activityCmd.add(activity);

          // Add modification event
          // caseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
        }
        return null;
      }
    }.execute();
  }


  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceEvalWorkload(Long id, BigDecimal evalWorkload) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        // Check and find case
        caseDb = funcCaseQuery.checkAndFind(id);

        // Check the modify case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Clear story point
        if (Objects.isNull(evalWorkload)) {
          if (nonNull(caseDb.getEvalWorkload())) {
            // Record activity before modifying caseDb.setStoryPoint(null)
            Activity activity = toActivity(FUNC_CASE, caseDb,
                ActivityType.EVAL_WORKLOAD_CLEAR, caseDb.getEvalWorkloadMethod());
            activityCmd.add(activity);

            caseDb.setEvalWorkload(null).setActualWorkload(null);
            funcCaseRepo.save(caseDb);

            // Add modification event
            funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
          }
          return null;
        }

        // Change value
        if (!evalWorkload.equals(caseDb.getEvalWorkload())) {
          caseDb.setEvalWorkload(evalWorkload);
          funcCaseRepo.save(caseDb);

          Activity activity = toActivity(FUNC_CASE, caseDb, EVAL_WORKLOAD,
              caseDb.getEvalWorkloadMethod(), evalWorkload);
          activityCmd.add(activity);

          // Add modification event
          funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
        }

        // Value has not changed -> noop
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceActualWorkload(Long id, BigDecimal actualWorkload) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        // Check and find case
        caseDb = funcCaseQuery.checkAndFind(id);

        // Check the modify case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());

        // Evaluation workload not set
        // ProtocolAssert.assertTrue(isNull(actualWorkload)
        //    || nonNull(caseDb.getEvalWorkload()), "Evaluation workload not set");
      }

      @Override
      protected Void process() {
        if (nonNull(actualWorkload) && isNull(caseDb.getEvalWorkload())) {
          caseDb.setEvalWorkload(actualWorkload);
        }

        // Clear story point
        if (Objects.isNull(actualWorkload)) {
          if (nonNull(caseDb.getActualWorkload())) {
            // Record activity before modifying caseDb.setStoryPoint(null)
            Activity activity = toActivity(FUNC_CASE, caseDb,
                ActivityType.ACTUAL_WORKLOAD_CLEAR, caseDb.getEvalWorkloadMethod());
            activityCmd.add(activity);

            caseDb.setActualWorkload(null);
            funcCaseRepo.save(caseDb);

            // Add modification event
            funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
          }
          return null;
        }

        // Change value
        if (!actualWorkload.equals(caseDb.getActualWorkload())) {
          caseDb.setActualWorkload(actualWorkload);
          funcCaseRepo.save(caseDb);

          Activity activity = toActivity(FUNC_CASE, caseDb,
              ActivityType.ACTUAL_WORKLOAD, caseDb.getEvalWorkloadMethod(), actualWorkload);
          activityCmd.add(activity);

          // Add modification event
          funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
        }

        // Value has not changed -> noop
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceAttachment(Long id, List<Attachment> attachments) {
    new BizTemplate<Void>() {
      FuncCase caseDb = null;

      @Override
      protected void checkParams() {
        // Check and find case
        caseDb = funcCaseQuery.checkAndFind(id);

        // Check the modify case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), caseDb.getPlanId());
      }

      @Override
      protected Void process() {
        // Clear attachments
        if (isEmpty(attachments)) {
          if (isNotEmpty(caseDb.getAttachments())) {
            // Record activity before modifying caseDb.setAttachmentsData(null)
            Activity activity = toActivity(FUNC_CASE, caseDb,
                ActivityType.ATTACHMENT_CLEAR, caseDb.getAttachments().stream().map(x ->
                    "<a data-type=\"attachment\" data-href=\"" + x.getUrl() + "\">" + x.getName()
                        + "</a>").collect(Collectors.joining(",")));
            activityCmd.add(activity);

            caseDb.setAttachments(null);
            funcCaseRepo.save(caseDb);

            // Add modification event
            funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
          }
          return null;
        }

        // Change value
        if (funcCaseQuery.hasModifyAttachments(attachments, caseDb)) {
          caseDb.setAttachments(attachments);
          funcCaseRepo.save(caseDb);

          Activity activity = toActivity(FUNC_CASE, caseDb,
              ActivityType.ATTACHMENT_UPDATED, attachments.stream().map(x ->
                  "<a data-type=\"attachment\" data-href=\"" + x.getUrl() + "\">" + x.getName()
                      + "</a>").collect(Collectors.joining(",")));
          activityCmd.add(activity);

          // Add modification event
          funcCaseQuery.assembleAndSendModifyNoticeEvent(caseDb, activity);
        }

        // Value has not changed -> noop
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void resultModify(List<FuncCase> cases, boolean replace) {
    new BizTemplate<Void>() {
      List<FuncCase> casesDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        casesDb = funcCaseQuery.checkAndFind(cases.stream().map(FuncCase::getId)
            .collect(Collectors.toList()));

        // Check the plan exists
        Set<Long> planIds = casesDb.stream().map(FuncCase::getPlanId).collect(Collectors.toSet());
        ProtocolAssert.assertTrue(planIds.size() == 1,
            "Only batch update cases with one plan is allowed");
        FuncPlan planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());

        // Check the test permission
        funcPlanAuthQuery.checkTestAuth(getUserId(), planDb.getId());

        // Check the plan has started
        funcPlanQuery.checkHasStarted(planDb);

        // Check the review passed
        if (planDb.getReview()) {
          funcCaseQuery.checkReviewPassed(casesDb);
        }
      }

      @Override
      protected Void process() {
        Map<Long, FuncCase> casesMap = cases.stream()
            .collect(Collectors.toMap(FuncCase::getId, x -> x));
        for (FuncCase caseDb : casesDb) {
          setTestInfoAndStatus(caseDb, casesMap.get(caseDb.getId()), replace);
        }
        funcCaseRepo.saveAll(casesDb);

        addActivitiesAndEvents();
        return null;
      }

      private void addActivitiesAndEvents() {
        Map<CaseTestResult, List<Long>> resultCaseIdMap = casesDb.stream()
            .collect(Collectors.groupingBy(FuncCase::getTestResult,
                Collectors.mapping(FuncCase::getId, Collectors.toList())));
        Map<Long, String> caseNameDbMap = casesDb.stream()
            .collect(Collectors.toMap(FuncCase::getId, FuncCase::getName));
        for (CaseTestResult caseTestResult : resultCaseIdMap.keySet()) {
          Map<CaseTestResult, List<FuncCase>> resultCaseMap = casesDb.stream()
              .peek(x -> x.setName(caseNameDbMap.get(x.getId())))
              .collect(Collectors.groupingBy(FuncCase::getTestResult));
          List<FuncCase> resultCases = resultCaseMap.get(caseTestResult);
          List<Activity> activities = toActivities(FUNC_CASE, resultCases,
              ActivityType.RESULT_UPDATE, caseTestResult);
          activityCmd.addAll(activities);

          // Add modification events
          funcCaseQuery.assembleAndSendModifyNoticeEvent(resultCases.stream().map(
                  x -> new FuncCaseInfo().setId(x.getId()).setName(x.getName())
                      .setTesterId(x.getTesterId()))
              .collect(Collectors.toList()), activities);
        }
      }
    }.execute();
  }

  /**
   * Note: Manually modifying the results of API testing cases is also permitted.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void resultReset(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncCaseInfo> casesDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        casesDb = funcCaseQuery.checkAndFindInfo(ids);

        // Check the plan exists
        Set<Long> planIds = casesDb.stream().map(FuncCaseInfo::getPlanId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(planIds.size() == 1,
            "Only batch update cases with one plan is allowed");
        FuncPlan planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());

        // Check the test permission
        funcPlanAuthQuery.checkResetTestResultAuth(getUserId(), planDb.getId());
      }

      @Override
      protected Void process() {
        funcCaseRepo.updateTestResultToInitByIds(ids);

        List<Activity> activities = toActivities(FUNC_CASE, casesDb, ActivityType.RESULT_RESET);
        activityCmd.addAll(activities);

        // Add modification events
        funcCaseQuery.assembleAndSendModifyNoticeEvent(casesDb, activities);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void retest(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncCase> casesDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        casesDb = funcCaseQuery.checkAndFind(ids);

        // Check the plan exists
        Set<Long> planIds = casesDb.stream().map(FuncCase::getPlanId)
            .collect(Collectors.toSet());
        assertTrue(planIds.size() == 1,
            "Only batch update cases with one plan is allowed");
        FuncPlan planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());

        // Check the test permission
        funcPlanAuthQuery.checkTestAuth(getUserId(), planDb.getId());

        // Check the case status
        FuncCase invalidCase = casesDb.stream().filter(x -> !x.getTestResult().isWideFinished())
            .findFirst().orElse(null);
        assertTrue(null == invalidCase,
            "Incomplete cases are not allowed to be retested");
      }

      @Override
      protected Void process() {
        for (FuncCase funcCase : casesDb) {
          funcCase.setTestResult(CaseTestResult.PENDING);
        }
        funcCaseRepo.saveAll(casesDb);

        List<Activity> activities = toActivities(FUNC_CASE, casesDb, ActivityType.RETEST_START);
        activityCmd.addAll(activities);

        // Add modification events
        funcCaseQuery.assembleAndSendModifyNoticeEvent(
            casesDb.stream().map(x -> new FuncCaseInfo().setId(x.getId())
                    .setName(x.getName()).setTesterId(x.getTesterId()))
                .collect(Collectors.toList()), activities);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void review(List<FuncCase> cases) {
    new BizTemplate<Void>() {
      List<FuncCase> casesDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        casesDb = funcCaseQuery.checkAndFind(cases.stream().map(FuncCase::getId)
            .collect(Collectors.toList()));

        // Check the plan exists
        Set<Long> planIds = casesDb.stream().map(FuncCase::getPlanId).collect(Collectors.toSet());
        ProtocolAssert.assertTrue(planIds.size() == 1,
            "Only batch update cases with one plan is allowed");
        FuncPlan planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());
        // Check the review permission
        funcPlanAuthQuery.checkReviewAuth(getUserId(), planDb.getId());
        // Check the plan has started
        funcPlanQuery.checkHasStarted(planDb);
        // Check the plan review is enabled
        funcPlanQuery.checkReviewEnabled(planDb);
        // Check the case can review
        funcCaseQuery.checkCanReview(casesDb);
        // Check the review status -> Do in web !!
      }

      @Override
      protected Void process() {
        Map<Long, FuncCase> reviewCaseMap = cases.stream()
            .collect(Collectors.toMap(FuncCase::getId, x -> x));
        for (FuncCase caseDb : casesDb) {
          FuncCase case0 = reviewCaseMap.get(caseDb.getId());
          setReviewInfoAndStatus(caseDb, case0);
        }
        funcCaseRepo.saveAll(casesDb);

        // Add review case activity
        addReviewActivities(casesDb);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void reviewReset(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncCaseInfo> casesDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        casesDb = funcCaseQuery.checkAndFindInfo(ids);

        // Check the plan exists
        Set<Long> planIds = casesDb.stream().map(FuncCaseInfo::getPlanId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(planIds.size() == 1,
            "Only batch update cases with one plan is allowed");
        FuncPlan planDb = funcPlanQuery.checkAndFind(planIds.iterator().next());

        // Check the reset review result permission
        funcPlanAuthQuery.checkResetReviewResultAuth(getUserId(), planDb.getId());
      }

      @Override
      protected Void process() {
        funcCaseRepo.updateReviewResultToInitByIds(ids);

        List<Activity> activities = toActivities(FUNC_CASE, casesDb, ActivityType.RESULT_RESET);
        activityCmd.addAll(activities);

        // Add modification events
        funcCaseQuery.assembleAndSendModifyNoticeEvent(casesDb, activities);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void taskAssocAdd(Long id, HashSet<Long> assocTaskIds) {
    new BizTemplate<Void>() {
      FuncCaseInfo caseDb;
      List<TaskInfo> assocTasksDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        caseDb = funcCaseQuery.checkAndFindInfo(id);
        // Check the association tasks exists
        assocTasksDb = taskQuery.checkAndFindInfo(assocTaskIds);
        // Check the update case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        taskFuncCaseCmd.addAssoc(FUNC_CASE, id, assocTaskIds, null);

        Activity activity = toActivity(FUNC_CASE, caseDb, ASSOC_TASK,
            assocTasksDb.stream().map(TaskInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void taskAssocCancel(Long id, HashSet<Long> assocTaskIds) {
    new BizTemplate<Void>() {
      FuncCaseInfo caseDb;
      List<TaskInfo> assocTasksDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        caseDb = funcCaseQuery.checkAndFindInfo(id);
        // Check the association tasks exists
        assocTasksDb = taskQuery.checkAndFindInfo(assocTaskIds);
        // Check the update case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        taskFuncCaseCmd.deleteAssoc(FUNC_CASE, id, assocTaskIds, null);

        Activity activity = toActivity(FUNC_CASE, caseDb, ASSOC_TASK_CANCEL,
            assocTasksDb.stream().map(TaskInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void caseAssocAdd(Long id, HashSet<Long> assocCaseIds) {
    new BizTemplate<Void>() {
      FuncCaseInfo caseDb;
      List<FuncCaseInfo> assocCasesDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        caseDb = funcCaseQuery.checkAndFindInfo(id);
        // Check the association cases exists
        assocCasesDb = funcCaseQuery.checkAndFindInfo(assocCaseIds);
        // Check the update case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        taskFuncCaseCmd.addAssoc(FUNC_CASE, id, null, assocCaseIds);

        Activity activity = toActivity(FUNC_CASE, caseDb, ASSOC_CASE,
            assocCasesDb.stream().map(FuncCaseInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void caseAssocCancel(Long id, HashSet<Long> assocCaseIds) {
    new BizTemplate<Void>() {
      FuncCaseInfo caseDb;
      List<FuncCaseInfo> assocCasesDb;

      @Override
      protected void checkParams() {
        // Check the case exists
        caseDb = funcCaseQuery.checkAndFindInfo(id);
        // Check the association cases exists
        assocCasesDb = funcCaseQuery.checkAndFindInfo(assocCaseIds);
        // Check the update case permission
        funcPlanAuthQuery.checkModifyCaseAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        taskFuncCaseCmd.deleteAssoc(FUNC_CASE, id, null, assocCaseIds);

        Activity activity = toActivity(FUNC_CASE, caseDb, ASSOC_CASE_CANCEL,
            assocCasesDb.stream().map(FuncCaseInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }


  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> imports(Long planId,
      StrategyWhenDuplicated strategyWhenDuplicated, MultipartFile file) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Check and find func plan
        planDb = funcPlanQuery.checkAndFind(planId);
        // Check the add case permission
        funcPlanAuthQuery.checkAddCaseAuth(getUserId(), planId);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Parsing imported file
        List<String[]> rows;
        try {
          rows = PoiUtils.readExcel(file.getInputStream());
        } catch (IOException e) {
          throw ProtocolException.of("Failed to read excel file, cause: " + e.getMessage());
        }
        assertNotEmpty(rows, "Read excel content is empty");

        // Check the for empty header fields
        List<String> titles = Stream.of(rows.get(0))
            .map(x -> StringUtils.remove(stringSafe(x), "*")).collect(Collectors.toList());
        assertTrue(titles.stream().noneMatch(ObjectUtils::isEmpty), "Title has empty value name");

        // Check the if the required import columns exist
        String missingRequiredField = CASE_IMPORT_REQUIRED_COLUMNS.stream()
            .filter(x -> !titles.contains(x)).findFirst().orElse(null);
        assertTrue(isEmpty(missingRequiredField),
            String.format("The required field %s is missing", missingRequiredField));

        List<String[]> data = rows.subList(1, rows.size());
        assertNotEmpty(data, "Read case data is empty");

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

        // Check the if the required import column values exist

        // Check the for duplicate case names
        assertTrue(nameIdx != -1, "Case name is required");
        List<String> names = data.stream().map(x -> x[nameIdx]).collect(Collectors.toList());
        List<String> duplicateNames = names.stream().filter(ObjectUtils.duplicateByKey(x -> x))
            .collect(Collectors.toList());
        assertTrue(isEmpty(duplicateNames),
            String.format("There are duplicates in the import case, duplicate case name: %s",
                duplicateNames));
        boolean hasEmptyName = names.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyName, "The import case name cannot be empty");

        assertTrue(testerIdx != -1, "Case tester is required");
        Set<String> testers = data.stream().map(x -> x[testerIdx])
            .collect(Collectors.toSet());
        boolean hasEmptyTester = testers.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyTester, "The import tester cannot be empty");
        // Check the if the tester exist
        Map<String, List<UserBase>> testerMap = userManager.checkValidAndFindUserBasesByName(
            testers);

        assertTrue(developerIdx != -1, "Case developer is required");
        Set<String> developers = data.stream().map(x -> x[developerIdx])
            .collect(Collectors.toSet());
        boolean hasEmptyDeveloper = developers.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyDeveloper, "The import developer cannot be empty");
        // Check the if the tester exist
        Map<String, List<UserBase>> developerMap = userManager.checkValidAndFindUserBasesByName(
            developers);

        assertTrue(deadlineIdx != -1, "Case deadline date is required");
        List<String> deadlines = data.stream().map(x -> x[deadlineIdx])
            .collect(Collectors.toList());
        boolean hasEmptyDeadlines = deadlines.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyDeadlines, "The import deadline date cannot be empty");

        // Check the if the modules exist
        Set<String> modules = data.stream()
            .filter(x -> moduleIdx != -1 && isNotEmpty(x[moduleIdx]))
            .map(x -> x[moduleIdx]).collect(Collectors.toSet());
        Map<String, Module> modulesMap = moduleQuery.checkAndFindByName(
            planDb.getProjectId(), modules);
        // Check the if the creators exist
        Set<String> reviwers = data.stream()
            .filter(x -> reviwerIdx != -1 && isNotEmpty(x[reviwerIdx]))
            .map(x -> x[reviwerIdx]).collect(Collectors.toSet());
        Map<String, List<UserBase>> reviwersMap = userManager.checkValidAndFindUserBasesByName(
            reviwers);
        // Check the if the creators exist
        Set<String> creators = data.stream()
            .filter(x -> creatorIdx != -1 && isNotEmpty(x[creatorIdx]))
            .map(x -> x[creatorIdx]).collect(Collectors.toSet());
        Map<String, List<UserBase>> creatorsMap = userManager.checkValidAndFindUserBasesByName(
            creators);
        // Check the if the associated tags exist
        Set<String> tags = data.stream().filter(x -> tagsIdx != -1 && isNotEmpty(x[tagsIdx]))
            .map(x -> List.of(x[tagsIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<Tag>> tagsMap = tagQuery.checkAndFindByName(
            planDb.getProjectId(), tags);
        // Check the if the associated tasks exist
        Set<String> taskNames = data.stream()
            .filter(x -> tasksIdx != -1 && isNotEmpty(x[tasksIdx]))
            .map(x -> List.of(x[tasksIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<TaskInfo>> tasksMap = taskQuery.checkAndFindByProjectAndName(
            planDb.getProjectId(), taskNames);
        // Check the if the associated cases exist
        Set<String> caseNames = data.stream()
            .filter(x -> casesIdx != -1 && isNotEmpty(x[casesIdx]))
            .map(x -> List.of(x[casesIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<FuncCaseInfo>> casesMap = funcCaseQuery.checkAndFindByPlanAndName(
            planId, caseNames);

        // Check the if the associated testing targets exist

        // Format import fields and convert them into case domains
        List<FuncCase> cases = importToDomain(
            uidGenerator, planDb, data, nameIdx, moduleIdx, modulesMap, testerIdx, testerMap,
            developerIdx, developerMap, reviwerIdx, reviwersMap, priorityIdx,
            deadlineIdx, preconditionIdx, stepIdx, descriptionIdx, evalWorkloadIdx,
            actualWorkloadIdx, testResultIdx, reviewStatusIdx, testProcessedDateIdx, reviewDateIdx,
            creatorIdx, creatorsMap, createdDateIdx, tagsIdx, tagsMap, tasksIdx, tasksMap,
            casesIdx, casesMap);

        // When using an `COVER` strategy, delete existing cases, otherwise ignore duplicate import cases
        Set<String> safePrefixNames = isEmpty(planDb.getCasePrefix())
            ? new HashSet<>(names) : names.stream().map(x -> planDb.getCasePrefix() + x)
            .collect(Collectors.toSet());
        if (strategyWhenDuplicated.isCover()) {
          funcCaseRepo.deleteByPlanIdAndNameIn(planId, safePrefixNames);
        } else {
          List<String> namesDb = funcCaseRepo.findNamesByNameInAndPlanId(safePrefixNames, planId);
          cases = cases.stream().filter(x -> !namesDb.contains(x.getName()))
              .collect(Collectors.toList());
        }

        if (isEmpty(cases)) {
          return null;
        }

        // Save imported cases
        return add(cases);
      }
    }.execute();
  }

  /**
   * Note: When API calls that are not user-action, tenant and user information must be injected
   * into the PrincipalContext.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {


      @Override
      protected List<IdKey<Long, Object>> process() {
        // 0. Query all tenant users
        List<User> users = userManager.findValidByTenantId(getOptTenantId());
        Assert.assertNotEmpty(users, "Tenant users are empty");

        // 1. Create test plan by sample file
        URL resourceUrl = this.getClass().getResource("/samples/plan/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_FUNC_PLAN_FILE);
        FuncPlan plan = parseSample(Objects.requireNonNull(resourceUrl),
            new TypeReference<FuncPlan>() {
            }, SAMPLE_FUNC_PLAN_FILE);
        assembleExampleFuncPlan(projectId, uidGenerator.getUID(), plan, users);
        funcPlanCmd.add(plan);

        // 2. Create test case by sample file
        resourceUrl = this.getClass().getResource("/samples/cases/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_FUNC_CASE_FILE);
        List<FuncCase> cases = parseSample(Objects.requireNonNull(resourceUrl),
            new TypeReference<List<FuncCase>>() {
            }, SAMPLE_FUNC_CASE_FILE);
        for (FuncCase case0 : cases) {
          assembleExampleFuncCase(projectId, uidGenerator.getUID(), case0, plan, users);
        }
        List<IdKey<Long, Object>> idKeys = funcCaseCmd.add(cases);

        // 3. Create case review by sample file
        resourceUrl = this.getClass().getResource("/samples/review/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_FUNC_REVIEW_FILE);
        FuncReview review = parseSample(Objects.requireNonNull(resourceUrl),
            new TypeReference<FuncReview>() {
            },  SAMPLE_FUNC_REVIEW_FILE);
        assembleExampleFuncReview(projectId, uidGenerator.getUID(), review, plan, users);
        funcReviewCmd.add(review);
        funcReviewCaseCmd.add(review.getId(), cases.stream()
            .filter(x -> nonNull(x.getReviewStatus()) && x.getReviewStatus().isPending())
            .map(FuncCase::getId).collect(Collectors.toSet()));

        // 4. Create case baseline by sample file
        resourceUrl = this.getClass().getResource("/samples/baseline/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_FUNC_BASELINE_FILE);
        FuncBaseline baseline = parseSample(Objects.requireNonNull(resourceUrl),
            new TypeReference<FuncBaseline>() {
            }, SAMPLE_FUNC_BASELINE_FILE);
        assembleExampleFuncBaseline(projectId, uidGenerator.getUID(), baseline, plan, cases, users);
        funcBaselineCmd.add(baseline);

        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncCaseInfo> casesDb;

      @Override
      protected void checkParams() {
        // Published api are not allowed to be modified
        casesDb = funcCaseInfoRepo.findAll0ByIdIn(ids);

        if (isNotEmpty(casesDb)) {
          // Check the whether you have the permission to modify the apis
          funcPlanAuthQuery.batchCheckPermission(casesDb.stream().map(FuncCaseInfo::getPlanId)
              .collect(Collectors.toSet()), FuncPlanPermission.DELETE_CASE);
        }
      }

      @Override
      protected Void process() {
        if (isEmpty(casesDb)) {
          return null;
        }

        // Update delete status
        funcCaseRepo.updateDeleteStatus(ids, true, getUserId(), LocalDateTime.now());

        // Add delete activity
        List<Activity> activities = toActivities(FUNC_CASE, casesDb, DELETED,
            activityParams(casesDb));
        activityCmd.addAll(activities);

        // Add deleted api to trash
        funcTrashCmd.add0(casesDb.stream().map(FuncCaseConverter::toFuncCaseTrash)
            .collect(Collectors.toList()));

        // Add modification events
        funcCaseQuery.assembleAndSendModifyNoticeEvent(casesDb, activities);
        return null;
      }
    }.execute();
  }

  @Override
  public void delete0(List<Long> caseIds) {
    // Delete cases
    funcCaseRepo.deleteAllByIdIn(caseIds);
    // Delete cases favorite
    funcCaseFollowRepo.deleteByCaseIdIn(caseIds);
    // Delete cases follow
    funcCaseFavouriteRepo.deleteByCaseIdIn(caseIds);
    // Delete cases comment
    commentRepo.deleteByTargetIdInAndTargetType(caseIds, CommentTargetType.FUNC_CASE.getValue());
    // Delete cases tag targets
    tagTargetCmd.delete0ByCaseIds(caseIds);
    // Delete cases reference
    taskFuncCaseCmd.deleteByTargetIds(caseIds);
    // Delete case review
    funcReviewCmd.deleteByCaseId(caseIds);
  }

  @Override
  public void addReviewActivities(List<FuncCase> casesDb) {
    Map<ReviewStatus, List<FuncCase>> reviewStatusMap = casesDb.stream()
        .collect(Collectors.groupingBy(FuncCase::getReviewStatus));
    for (ReviewStatus reviewStatus : reviewStatusMap.keySet()) {

      List<FuncCase> reviewCasesDb = reviewStatusMap.get(reviewStatus);
      List<Activity> activities = toActivities(FUNC_CASE, reviewCasesDb,
          ActivityType.REVIEW_UPDATE, reviewStatus);
      activityCmd.addAll(activities);

      // Add modification events
      funcCaseQuery.assembleAndSendModifyNoticeEvent(reviewCasesDb.stream().map(
              x -> new FuncCaseInfo().setId(x.getId()).setName(x.getName())
                  .setTesterId(x.getTesterId()))
          .collect(Collectors.toList()), activities);
    }
  }

  private void addModifyActivitiesAndEvents(boolean replace, List<FuncCase> cases,
      List<FuncCase> updatedCasesDb) {
    // Get existed status before replace
    Map<Long, FuncCase> caseDbMap = updatedCasesDb.stream()
        .collect(Collectors.toMap(FuncCase::getId, x -> x));
    Map<Long, UserBase> testerMap = userManager.checkValidAndFindUserBase(
            cases.stream().map(FuncCase::getTesterId).filter(Objects::nonNull)
                .collect(Collectors.toSet())).stream()
        .collect(Collectors.toMap(UserBase::getId, x -> x));
    List<Activity> activities = new ArrayList<>();
    for (FuncCase case0 : cases) {
      boolean hasModifyTester = nonNull(case0.getTesterId()) &&
          !Objects.equals(caseDbMap.get(case0.getId()).getTesterId(), case0.getTesterId());
      boolean hasModifyTaskTags = tagQuery
          .hasModifyTag(case0.getId(), case0.getTagTargets());
      boolean hasModifyAttachments = funcCaseQuery
          .hasModifyAttachments(case0.getAttachments(), caseDbMap.get(case0.getId()));
      List<Tag> caseTagsDb = null;
      if (isNotEmpty(case0.getTagTargets())) {
        caseTagsDb = tagQuery.checkAndFind(case0.getTagTargets().stream()
            .map(TagTarget::getTagId).collect(Collectors.toList()));
      }
      activities.add(toModifyCaseActivity(replace, hasModifyTester, hasModifyTaskTags,
          hasModifyAttachments, case0, caseDbMap.get(case0.getId()),
          testerMap.get(case0.getTesterId()), caseTagsDb));
    }

    // Do not log when parameter has not changed !!!  Null activity will ignored.
    activityCmd.addAll(activities);

    // Add modification events
    funcCaseQuery.assembleAndSendModifyNoticeEvent(updatedCasesDb.stream().map(
            x -> new FuncCaseInfo().setId(x.getId()).setName(x.getName()).setTesterId(x.getTesterId()))
        .collect(Collectors.toList()), activities);
  }

  public static String getCaseCode() {
    return getBean(BidGenerator.class).getId(FUNC_CASE_BID_KEY, getTenantId());
  }

  @Override
  protected BaseRepository<FuncCase, Long> getRepository() {
    return this.funcCaseRepo;
  }
}
