package cloud.xcan.angus.core.tester.domain;

import cloud.xcan.angus.spec.experimental.Entity;

public interface ResourceFavouriteAndFollow<T extends Entity<T, ID>, ID>{

  Long getId();

  T setId(Long id);

  Boolean getFavourite();

  T setFavourite(Boolean favourite);

  Boolean getFollow();

  T setFollow(Boolean follow);
}
