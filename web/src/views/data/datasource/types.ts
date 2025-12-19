import { DatabaseType } from '@xcan-angus/infra';

/**
 * <p>Data source form state for create/edit operations</p>
 * <p>Contains all form fields for data source configuration</p>
 */
export interface DataSourceFormState {
  id?: string;
  name: string;
  database: DatabaseType;
  driverClassName?: string;
  jdbcUrl: string;
  username?: string;
  password?: string;
  projectId?: string;
}

/**
 * <p>Data source item with full information</p>
 * <p>Represents a complete data source record with all properties</p>
 */
export interface DataSourceDetail {
  id: string;
  name: string;
  database: DatabaseType;
  username: string;
  password: string;
  jdbcUrl: string;
  driverClassName: string;
  connSuccessFlag: string;
  lastConnDate: string;
  auth: string;
  tenantId: string;
  createdBy: string;
  createdDate: string;
  modifiedBy: string;
  modifiedDate: string;
  modifier?: string;
  avatar?: string;
}

/**
 * <p>Enhanced data source item with UI state</p>
 * <p>Extends DataSourceItem with additional UI-related properties</p>
 */
export interface EnhancedDataSourceItem extends DataSourceDetail {
  connSuccess?: boolean;
  connFailureMessage?: string;
  testLoading: boolean;
}

/**
 * <p>Search option configuration for search panel</p>
 * <p>Defines the structure of individual search field options</p>
 */
export interface SearchOption {
  valueKey: string;
  type: string;
  placeholder?: string | string[];
  allowClear?: boolean;
  maxlength?: number;
  enumKey?: any;
  showTime?: boolean;
}

/**
 * <p>Connection test result from API</p>
 * <p>Contains the result of testing a data source connection</p>
 */
export interface ConnectionTestResult {
  connSuccess: boolean;
  connFailureMessage?: string;
}

/**
 * <p>Component props interface for main datasource component</p>
 * <p>Defines the properties passed to the main datasource component</p>
 */
export interface DataSourceProps {
  projectId: string;
}

/**
 * <p>Component emits interface for main datasource component</p>
 * <p>Defines all possible events that can be emitted</p>
 */
export interface DataSourceEmits {
  (e: 'refresh'): void;
}

/**
 * <p>Component props interface for Add/Edit modal component</p>
 * <p>Defines the properties passed to the modal component</p>
 */
export interface AddModalProps {
  visible: boolean;
  editData?: DataSourceDetail;
  projectId: string;
}

/**
 * <p>Component emits interface for Add/Edit modal component</p>
 * <p>Defines all possible events that can be emitted by the modal</p>
 */
export interface AddModalEmits {
  (e: 'update:visible', value: boolean): void;
  (e: 'refresh'): void;
}

/**
 * <p>Database configuration template</p>
 * <p>Contains default values for different database types</p>
 */
export interface DatabaseConfig {
  driverClassName: string;
  jdbcUrl: string;
}
