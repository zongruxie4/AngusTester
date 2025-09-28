/**
 * Filter parameters for API requests
 * @interface Params
 */
export interface Params {
  /**
   * Array of filter conditions
   */
  filters: {
    /**
     * The field to filter on
     */
    key: string;

    /**
     * The operation to perform (e.g., MATCH)
     */
    op: string;

    /**
     * The value to filter by
     */
    value: string
  }[]
}

/**
 * Data structure for table rows in member list
 * @interface TableData
 */
export interface TableData {
  /**
   * Array of policy names associated with this member
   */
  policyNames: string[];

  /**
   * Avatar URL for user members
   */
  avatar?: string;

  /**
   * Creation date of the membership
   */
  createdDate: string;

  /**
   * Name for department or group members
   */
  name?: string;

  /**
   * Full name for user members
   */
  fullName?: string;

  /**
   * Unique identifier for the member
   */
  id: string;
}

/**
 * Policy information structure
 * @interface Policy
 */
export interface Policy {
  /**
   * Unique identifier for the policy
   */
  id: string;

  /**
   * Display name of the policy
   */
  name: string;

  /**
   * Code identifier for the policy
   */
  code: string;

  /**
   * Organization type information
   */
  orgType: {
    /**
     * Type value (e.g., USER, TENANT)
     */
    value: string;

    /**
     * Display message for the organization type
     */
    message: string;
  };

  /**
   * Creation date of the policy association
   */
  createdDate: string;
}

/**
 * Data structure for policy items in the policy modal
 * @interface PolicyItem
 */
export interface PolicyItem {
  /**
   * Unique identifier for the policy
   */
  id: string;

  /**
   * Display name of the policy
   */
  name: string;

  /**
   * Code identifier for the policy
   */
  code: string;

  /**
   * Description of the policy
   */
  description: string;

  /**
   * Name of the application this policy belongs to
   */
  appName: string;

  /**
   * Whether the policy is enabled
   */
  enabled: boolean;
}

/**
 * Quota information structure
 * @interface QuotaInfo
 */
export interface QuotaInfo {
  /**
   * Display name of the resource
   */
  name: {
    /**
     * Display message for the resource name
     */
    message: string;

    /**
     * Key value for the resource name
     */
    value: string;
  };

  /**
   * Key identifier for the quota
   */
  key: string;

  /**
   * Current quota value
   */
  quota: number;

  /**
   * Default quota value
   */
  default0: number;

  /**
   * Maximum quota value
   */
  max: number;
}
