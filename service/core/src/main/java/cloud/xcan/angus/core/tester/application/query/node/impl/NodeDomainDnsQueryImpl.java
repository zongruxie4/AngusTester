package cloud.xcan.angus.core.tester.application.query.node.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.DOMAIN_DNS_NAME_REPEATED_T;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.node.NodeDomainDnsQuery;
import cloud.xcan.angus.core.tester.domain.node.dns.NodeDomainDns;
import cloud.xcan.angus.core.tester.domain.node.dns.NodeDomainDnsRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Implementation of NodeDomainDnsQuery for managing domain DNS records.
 * 
 * <p>This class provides functionality to query and validate domain DNS records within the system.
 * It supports basic CRUD operations with proper validation and error handling.</p>
 * 
 * <p>The implementation includes business logic for checking name uniqueness within domains
 * and provides both transactional and non-transactional query methods.</p>
 */
@Biz
public class NodeDomainDnsQueryImpl implements NodeDomainDnsQuery {

  @Resource
  private NodeDomainDnsRepo nodeDomainDnsRepo;

  /**
   * Finds a domain DNS record by its unique identifier.
   * 
   * <p>This method retrieves a specific domain DNS record using its ID. If the record
   * is not found, it throws a ResourceNotFound exception.</p>
   * 
   * @param id the unique identifier of the domain DNS record
   * @return the NodeDomainDns object if found
   * @throws ResourceNotFound if the record with the given ID does not exist
   */
  @Override
  public NodeDomainDns find(Long id) {
    return new BizTemplate<NodeDomainDns>() {

      @Override
      protected NodeDomainDns process() {
        return nodeDomainDnsRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "NodeDomainDns"));
      }
    }.execute();
  }

  /**
   * Finds domain DNS records based on search criteria with pagination support.
   * 
   * <p>This method supports complex queries using JPA specifications and provides
   * paginated results for efficient data retrieval.</p>
   * 
   * @param spec the JPA specification for filtering records
   * @param pageable pagination parameters for result limiting
   * @return a paginated result containing matching domain DNS records
   */
  @Override
  public Page<NodeDomainDns> find(Specification<NodeDomainDns> spec, Pageable pageable) {
    return new BizTemplate<Page<NodeDomainDns>>() {

      @Override
      protected Page<NodeDomainDns> process() {
        return nodeDomainDnsRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * Checks for the existence of a domain DNS record and returns it if found.
   * 
   * <p>This method performs a direct database query without transaction management.
   * It throws a ResourceNotFound exception if the record does not exist.</p>
   * 
   * @param id the unique identifier of the domain DNS record
   * @return the NodeDomainDns object if found
   * @throws ResourceNotFound if the record with the given ID does not exist
   */
  @Override
  public NodeDomainDns checkAndFind(Long id) {
    return nodeDomainDnsRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "NodeDomainDns"));
  }

  /**
   * Validates that a DNS name does not already exist within a specific domain.
   * 
   * <p>This method checks for name uniqueness when adding a new DNS record to a domain.
   * It includes logic to handle soft-deleted records in the validation process.</p>
   * 
   * @param domainId the unique identifier of the domain
   * @param name the DNS name to validate
   * @throws IllegalArgumentException if the name already exists in the domain
   */
  @Override
  public void checkAddNameExists(Long domainId, String name) {
    // Count existing records including soft-deleted ones for validation
    Long nameCount = nodeDomainDnsRepo.countByDomainIdAndName(domainId, name);
    assertResourceExisted(nameCount < 1, DOMAIN_DNS_NAME_REPEATED_T, new Object[]{name});
  }

  /**
   * Validates that a DNS name does not already exist within a specific domain when updating.
   * 
   * <p>This method checks for name uniqueness when updating an existing DNS record,
   * excluding the current record from the validation check.</p>
   * 
   * @param id the unique identifier of the DNS record being updated
   * @param domainId the unique identifier of the domain
   * @param name the DNS name to validate
   * @throws IllegalArgumentException if the name already exists in the domain (excluding current record)
   */
  @Override
  public void checkUpdateNameExists(Long id, Long domainId, String name) {
    // Count existing records excluding the current record being updated
    Long nameCount = nodeDomainDnsRepo.countByDomainIdAndNameAndIdNot(domainId, name, id);
    assertResourceExisted(nameCount < 1, DOMAIN_DNS_NAME_REPEATED_T, new Object[]{name});
  }
}




