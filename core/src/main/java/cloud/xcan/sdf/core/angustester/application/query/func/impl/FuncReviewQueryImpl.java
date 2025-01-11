package cloud.xcan.sdf.core.angustester.application.query.func.impl;

import static cloud.xcan.sdf.core.angustester.application.converter.FuncCaseConverter.getCommonCreatorResourcesFilter;
import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginMessage.FUNC_REVIEW_NAME_REPEATED_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginMessage.REVIEW_NOT_STARTED;
import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginMessage.REVIEW_NOT_STARTED_CODE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReview;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReviewRepo;
import cloud.xcan.sdf.core.angustester.domain.func.review.ReviewCaseNum;
import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCaseRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.exception.BizException;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class FuncReviewQueryImpl implements FuncReviewQuery {

  @Resource
  private FuncReviewRepo funcReviewRepo;

  @Resource
  private FuncReviewCaseRepo funcReviewCaseRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

  @Override
  public FuncReview detail(Long id) {
    return new BizTemplate<FuncReview>() {
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        reviewDb = checkAndFind(id);
      }

      @Override
      protected FuncReview process() {
        List<FuncReview> reviews = List.of(reviewDb);
        Set<Long> ids = Set.of(id);
        setCaseNum(reviews, ids);
        setProgress(reviews, ids);
        setParticipants(reviews, ids);
        // Set user name and avatar
        userManager.setUserNameAndAvatar(List.of(reviewDb), "ownerId", "ownerName", "ownerAvatar");
        return reviewDb;
      }
    }.execute();
  }

  @Override
  public Page<FuncReview> find(GenericSpecification<FuncReview> spec, Pageable pageable) {
    return new BizTemplate<Page<FuncReview>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(spec.getCriterias());
      }

      @Override
      protected Page<FuncReview> process() {
        Page<FuncReview> page = funcReviewRepo.findAll(spec, pageable);
        if (page.hasContent()) {
          Set<Long> reviewIds = page.getContent().stream().map(FuncReview::getId)
              .collect(Collectors.toSet());
          setCaseNum(page.getContent(), reviewIds);
          setProgress(page.getContent(), reviewIds);
          setParticipants(page.getContent(), reviewIds);
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "ownerId", "ownerName",
              "ownerAvatar");
        }
        return page;
      }
    }.execute();
  }

  @Override
  public List<FuncReview> findByPlanIds(HashSet<Long> planIds) {
    return funcReviewRepo.findByPlanIdIn(planIds);
  }

  @Override
  public FuncReview checkAndFind(Long id) {
    return funcReviewRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "FuncReview"));
  }

  @Override
  public List<FuncReview> checkAndFind(Collection<Long> ids) {
    List<FuncReview> reviews = funcReviewRepo.findAllById(ids);
    ProtocolAssert.assertResourceNotFound(isNotEmpty(reviews), ids.iterator().next(),
        "FuncReview");
    if (ids.size() != reviews.size()) {
      for (FuncReview review : reviews) {
        ProtocolAssert.assertResourceNotFound(ids.contains(review.getId()), review.getId(),
            "FuncReview");
      }
    }
    return reviews;
  }

  @Override
  public void checkHasStarted(FuncReview reviewDb) {
    if (reviewDb.getStatus().isNotInProcess()) {
      throw BizException.of(REVIEW_NOT_STARTED_CODE, REVIEW_NOT_STARTED);
    }
  }

  @Override
  public void checkNameExists(Long projectId, String name) {
    long count = funcReviewRepo.countByProjectIdAndName(projectId, name);
    if (count > 0) {
      throw ResourceExisted.of(FUNC_REVIEW_NAME_REPEATED_T, new Object[]{name});
    }
  }

  @Override
  public boolean hasPendingCaseInReview(Long reviewId) {
    return !funcReviewCaseRepo.findPendingCaseIdByReviewId(reviewId).isEmpty();
  }

  @Override
  public void setCaseNum(List<FuncReview> reviews, Set<Long> reviewIds) {
    if (isNotEmpty(reviews)) {
      Map<Long, Long> caseNumsMap = funcReviewCaseRepo.findReviewCaseNumsGroupByReviewId(reviewIds)
          .stream().collect(toMap(ReviewCaseNum::getReviewId, ReviewCaseNum::getCaseNum));
      for (FuncReview review : reviews) {
        review.setCaseNum(caseNumsMap.containsKey(review.getId())
            ? caseNumsMap.get(review.getId()) : 0);
      }
    }
  }

  /**
   * Note: Must be executed after setCaseNum().
   */
  @Override
  public void setProgress(List<FuncReview> reviews, Set<Long> reviewIds) {
    if (isNotEmpty(reviews)) {
      Map<Long, Long> reviewPassedNumsMap =
          funcReviewCaseRepo.findReviewPassedCaseNumsGroupByReviewId(reviewIds)
              .stream().collect(toMap(ReviewCaseNum::getReviewId, ReviewCaseNum::getCaseNum));
      for (FuncReview review : reviews) {
        if (reviewPassedNumsMap.containsKey(review.getId())) {
          review.setProgress(new Progress().setTotal(review.getCaseNum())
              .setCompleted(reviewPassedNumsMap.get(review.getId()))
              .setCompletedRate(review.getCaseNum() > 0 ?
                  BigDecimal.valueOf(reviewPassedNumsMap.get(review.getId()))
                      .divide(BigDecimal.valueOf(review.getCaseNum()), 4,
                          RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) // X 100%
                      .setScale(2, RoundingMode.HALF_UP)
                  : BigDecimal.ZERO));
        } else {
          review.setProgress(new Progress().setTotal(review.getCaseNum())
              .setCompleted(0)
              .setCompletedRate(BigDecimal.ZERO));
        }
      }
    }
  }

  @Override
  public void setParticipants(List<FuncReview> reviews, Set<Long> reviewIds) {
    if (isNotEmpty(reviews)) {
      Set<Long> participantIds = new HashSet<>();
      for (FuncReview review : reviews) {
        if (isNotEmpty(review.getParticipantIds())) {
          participantIds.addAll(review.getParticipantIds());
        }
      }
      if (isEmpty(participantIds)) {
        return;
      }
      Map<Long, UserInfo> userMap = userManager.getUserBaseMap(participantIds).entrySet().stream()
          .collect(toMap(Entry::getKey, x -> new UserInfo().setId(x.getValue().getId())
              .setFullname(x.getValue().getFullname()).setAvatar(x.getValue().getAvatar())));
      for (FuncReview review : reviews) {
        if (isNotEmpty(review.getParticipantIds())) {
          List<UserInfo> participants = new ArrayList<>();
          for (Long userId : review.getParticipantIds()) {
            participants.add(userMap.getOrDefault(userId, new UserInfo().setId(userId)));
          }
          review.setParticipants(participants);
        }
      }
    }
  }

  @Override
  public void setSafeCloneName(FuncReview review) {
    String saltName = randomAlphanumeric(3);
    String clonedName = funcReviewRepo.existsByProjectIdAndName(
        review.getProjectId(), review.getName() + "-Copy")
        ? review.getName() + "-Copy." + saltName : review.getName() + "-Copy";
    clonedName = clonedName.length() > DEFAULT_NAME_LENGTH ? clonedName.substring(0,
        DEFAULT_NAME_LENGTH_X2 - 3) + saltName : clonedName;
    review.setName(clonedName);
  }

  @Override
  public List<FuncReview> getReviewCreatedSummaries(Long projectId, Long planId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> creatorIds = Objects.isNull(creatorOrgType) ? null
        : userManager.getUserIdByOrgType0(creatorOrgType, creatorOrgId);
    Set<SearchCriteria> allFilters = getCommonCreatorResourcesFilter(projectId, planId,
        createdDateStart, createdDateEnd, creatorIds);
    return funcReviewRepo.findAllByFilters(allFilters);
  }

}
