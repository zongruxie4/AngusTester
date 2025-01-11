package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReview;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FuncReviewQuery {

  FuncReview detail(Long id);

  Page<FuncReview> find(GenericSpecification<FuncReview> spec, Pageable pageable);

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
