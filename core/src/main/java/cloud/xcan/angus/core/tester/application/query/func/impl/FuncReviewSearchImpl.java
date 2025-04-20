package cloud.xcan.angus.core.tester.application.query.func.impl;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.func.FuncReviewQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncReviewSearch;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReviewSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class FuncReviewSearchImpl implements FuncReviewSearch {

  @Resource
  private FuncReviewSearchRepo funcReviewSearchRepo;

  @Resource
  private FuncReviewQuery funcReviewQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<FuncReview> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<FuncReview> clz, String... matches) {
    return new BizTemplate<Page<FuncReview>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<FuncReview> process() {
        Page<FuncReview> page = funcReviewSearchRepo.find(criteria, pageable, clz, matches);
        if (page.hasContent()) {
          Set<Long> reviewIds = page.getContent().stream().map(FuncReview::getId)
              .collect(Collectors.toSet());
          funcReviewQuery.setCaseNum(page.getContent(), reviewIds);
          funcReviewQuery.setProgress(page.getContent(), reviewIds);
          funcReviewQuery.setParticipants(page.getContent(), reviewIds);
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "ownerId", "ownerName",
              "ownerAvatar");
        }
        return page;
      }
    }.execute();
  }

}
