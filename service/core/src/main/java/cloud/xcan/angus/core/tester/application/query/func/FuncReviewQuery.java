package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReview;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncReviewQuery {

  FuncReview detail(Long id);

  Page<FuncReview> find(GenericSpecification<FuncReview> spec, PageRequest pageable
      , boolean fullTextSearch, String[] match);

  List<FuncReview> findByPlanIds(HashSet<Long> planIds);

  FuncReview checkAndFind(Long id);

  List<FuncReview> checkAndFind(Collection<Long> ids);

  void checkHasStarted(FuncReview reviewDb);

  void checkNameExists(Long projectId, String name);

  boolean hasPendingCaseInReview(Long reviewId);

  void setCaseNum(List<FuncReview> reviews, Set<Long> reviewIds);

  void setProgress(List<FuncReview> reviews, Set<Long> reviewIds);

  void setParticipants(List<FuncReview> reviews, Set<Long> reviewIds);

  void setSafeCloneName(FuncReview review);

  List<FuncReview> getReviewCreatedSummaries(Long projectId, Long planId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId);

}
