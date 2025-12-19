package cloud.xcan.angus.core.tester.application.query.config.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.DOMAIN_NAME_REPEATED_T;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Collections.singletonList;

import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.config.NodeDomainQuery;
import cloud.xcan.angus.core.tester.domain.config.node.dns.DomainDnsNum;
import cloud.xcan.angus.core.tester.domain.config.node.dns.NodeDomainDnsRepo;
import cloud.xcan.angus.core.tester.domain.config.node.domain.NodeDomain;
import cloud.xcan.angus.core.tester.domain.config.node.domain.NodeDomainRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Implementation of NodeDomainQuery for managing node domains and their DNS configurations.
 *
 * <p>This class provides comprehensive functionality for querying and managing node domains,
 * including domain information retrieval, name validation, and DNS record counting.</p>
 *
 * <p>The implementation supports both transactional and non-transactional operations,
 * with proper error handling and business rule validation.</p>
 */
@Service
public class NodeDomainQueryImpl implements NodeDomainQuery {

  @Resource
  private NodeDomainRepo nodeDomainRepo;
  @Resource
  private NodeDomainDnsRepo nodeDomainDnsRepo;

  /**
   * Finds a node domain by its unique identifier and populates DNS count information.
   *
   * <p>This method retrieves a specific domain and automatically calculates the number
   * of DNS records associated with it for complete domain information.</p>
   *
   * @param id the unique identifier of the node domain
   * @return the NodeDomain object with DNS count populated
   * @throws ResourceNotFound if the domain with the given ID does not exist
   */
  @Override
  public NodeDomain find(Long id) {
    return new BizTemplate<NodeDomain>() {

      @Override
      protected NodeDomain process() {
        NodeDomain domain = nodeDomainRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "NodeDomain"));
        setDnsNum(singletonList(domain));
        return domain;
      }
    }.execute();
  }

  /**
   * Finds node domains based on search criteria with pagination support and DNS count population.
   *
   * <p>This method supports complex queries using JPA specifications and automatically
   * populates DNS record counts for all returned domains.</p>
   *
   * @param spec     the JPA specification for filtering domains
   * @param pageable pagination parameters for result limiting
   * @return a paginated result containing matching domains with DNS counts populated
   */
  @Override
  public Page<NodeDomain> find(Specification<NodeDomain> spec, Pageable pageable) {
    return new BizTemplate<Page<NodeDomain>>() {

      @Override
      protected Page<NodeDomain> process() {
        Page<NodeDomain> page = nodeDomainRepo.findAll(spec, pageable);
        if (page.hasContent()) {
          setDnsNum(page.getContent());
        }
        return page;
      }
    }.execute();
  }

  /**
   * Checks for the existence of a node domain and returns it if found.
   *
   * <p>This method performs a direct database query without transaction management
   * and DNS count population.</p>
   *
   * @param id the unique identifier of the node domain
   * @return the NodeDomain object if found
   * @throws ResourceNotFound if the domain with the given ID does not exist
   */
  @Override
  public NodeDomain checkAndFind(Long id) {
    return nodeDomainRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "NodeDomain"));
  }

  /**
   * Checks for the existence of a node domain by name and returns it if found.
   *
   * <p>This method performs a direct database query using the domain name as the search
   * criteria.</p>
   *
   * @param name the name of the node domain
   * @return the NodeDomain object if found
   * @throws ResourceNotFound if the domain with the given name does not exist
   */
  @Override
  public NodeDomain checkAndFind(String name) {
    return nodeDomainRepo.findByName(name)
        .orElseThrow(() -> ResourceNotFound.of(name, "NodeDomain"));
  }

  /**
   * Validates that a domain name does not already exist in the system.
   *
   * <p>This method checks for name uniqueness when adding a new domain.
   * It ensures no duplicate domain names exist across the entire system.</p>
   *
   * @param name the domain name to validate
   * @throws IllegalArgumentException if the name already exists
   */
  @Override
  public void checkAddNameExists(String name) {
    long nameCount = nodeDomainRepo.countByName(name);
    assertResourceExisted(nameCount < 1, DOMAIN_NAME_REPEATED_T, new Object[]{name});
  }

  /**
   * Validates that a domain name does not already exist when updating an existing domain.
   *
   * <p>This method checks for name uniqueness when updating a domain, excluding
   * the current domain from the validation check.</p>
   *
   * @param id   the unique identifier of the domain being updated
   * @param name the domain name to validate
   * @throws IllegalArgumentException if the name already exists (excluding current domain)
   */
  @Override
  public void checkUpdateNameExists(Long id, String name) {
    long nameCount = nodeDomainRepo.countByNameAndIdNot(name, id);
    assertResourceExisted(nameCount < 1, DOMAIN_NAME_REPEATED_T, new Object[]{name});
  }

  /**
   * Populates DNS record counts for a list of node domains.
   *
   * <p>This method efficiently calculates and sets the number of DNS records
   * associated with each domain in the provided list. It uses batch querying to minimize database
   * calls for better performance.</p>
   *
   * @param domains the list of NodeDomain objects to populate with DNS counts
   */
  @Override
  public void setDnsNum(List<NodeDomain> domains) {
    if (isEmpty(domains)) {
      return;
    }

    // Batch query DNS counts for all domains to minimize database calls
    Map<Long, DomainDnsNum> domainDnsNumMap = nodeDomainDnsRepo
        .selectDnsNumByDomainIdIn(domains.stream().map(NodeDomain::getId)
            .collect(Collectors.toSet())).stream()
        .collect(Collectors.toMap(DomainDnsNum::getDomainId, x -> x));

    // Set DNS count for each domain, defaulting to 0 if no DNS records exist
    for (NodeDomain domain : domains) {
      DomainDnsNum domainDnsNum = domainDnsNumMap.get(domain.getId());
      domain.setDnsNum(Objects.isNull(domainDnsNum) ? 0 : domainDnsNum.getDnsNum());
    }
  }
}
