package cloud.xcan.angus.core.tester.interfaces.version.facade.internal.assembler;

import static java.util.Objects.isNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersion;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersionStatus;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionAddDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionFindDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionSearchDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.vo.SoftwareVersionDetailVo;
import cloud.xcan.angus.core.tester.interfaces.version.facade.vo.SoftwareVersionVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class SoftwareVersionAssembler {

  public static SoftwareVersion toAddDomain(SoftwareVersionAddDto dto) {
    return new SoftwareVersion()
        .setProjectId(dto.getProjectId()).setName(dto.getName())
        .setStartDate(dto.getStartDate()).setReleaseDate(dto.getReleaseDate())
        .setStatus(SoftwareVersionStatus.DEFAULT).setDescription(dto.getDescription());
  }

  public static SoftwareVersion toUpdateDomain(SoftwareVersionUpdateDto dto) {
    return new SoftwareVersion().setId(dto.getId()).setName(dto.getName())
        .setStartDate(dto.getStartDate()).setReleaseDate(dto.getReleaseDate())
        .setDescription(dto.getDescription());
  }

  public static SoftwareVersion toReplaceDomain(SoftwareVersionReplaceDto dto) {
    return new SoftwareVersion()
        .setProjectId(isNull(dto.getId()) ? dto.getProjectId() : null)
        .setName(dto.getName())
        .setStartDate(dto.getStartDate()).setReleaseDate(dto.getReleaseDate())
        .setStatus(isNull(dto.getId()) ? SoftwareVersionStatus.DEFAULT : null)
        .setDescription(dto.getDescription());
  }

  public static SoftwareVersionDetailVo toDetailVo(SoftwareVersion version) {
    return new SoftwareVersionDetailVo().setId(version.getId()).setProjectId(version.getProjectId())
        .setName(version.getName()).setStartDate(version.getStartDate())
        .setReleaseDate(version.getReleaseDate()).setStatus(version.getStatus())
        .setDescription(version.getDescription())
        .setProgress(version.getProgress()).setTaskByStatus(version.getTaskByStatus())
        .setCreatedBy(version.getCreatedBy()).setCreatedDate(version.getCreatedDate())
        .setLastModifiedBy(version.getLastModifiedBy())
        .setLastModifiedDate(version.getLastModifiedDate());
  }

  public static SoftwareVersionVo toVo(SoftwareVersion version) {
    return new SoftwareVersionVo().setId(version.getId()).setProjectId(version.getProjectId())
        .setName(version.getName()).setStartDate(version.getStartDate())
        .setReleaseDate(version.getReleaseDate()).setStatus(version.getStatus())
        .setDescription(version.getDescription()).setProgress(version.getProgress())
        .setCreatedBy(version.getCreatedBy()).setCreatedDate(version.getCreatedDate())
        .setLastModifiedBy(version.getLastModifiedBy())
        .setLastModifiedDate(version.getLastModifiedDate());
  }

  public static GenericSpecification<SoftwareVersion> getSpecification(SoftwareVersionFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "createdDate")
        .inAndNotFields("id", "status", "name")
        .matchSearchFields("name", "description")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(SoftwareVersionSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "createdDate")
        .inAndNotFields("id", "status", "name")
        .matchSearchFields("name", "description")
        .build();
  }

}
