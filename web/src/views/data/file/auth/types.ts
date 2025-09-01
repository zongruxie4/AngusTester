/**
 * <p>
 * Space item interface representing a file storage space
 * </p>
 */
export interface SpaceItem {
  /** Unique identifier for the space */
  id: string;
  /** Display name of the space */
  name: string;
  /** Whether the space has authentication enabled */
  auth: boolean;
  /** Additional remarks or description */
  remark: string;
  /** Business key identifier */
  bizKey: string;
  /** Size of the space in human-readable format */
  size: string;
  /** Number of subdirectories */
  subDirectoryNum: string;
  /** Number of subfiles */
  subFileNum: string;
  /** ID of the user who created the space */
  createdBy: string;
  /** Name of the user who created the space */
  createdByName: string;
  /** Date when the space was created */
  createdDate: string;
}

/**
 * <p>
 * Checkbox option interface for permission selection
 * </p>
 */
export interface CheckboxOption {
  /** Display label for the checkbox */
  label: string;
  /** Value associated with the checkbox */
  value: string;
}

/**
 * <p>
 * Checkbox change event interface
 * </p>
 */
export interface CheckboxChangeEvent {
  target: {
    /** Whether the checkbox is checked */
    checked: boolean;
  };
}

/**
 * <p>
 * Props interface for the CheckboxGroup component
 * </p>
 */
export interface CheckboxGroupProps {
  /** Array of checkbox options to display */
  options: CheckboxOption[];
  /** Whether the checkboxes are disabled */
  disabled: boolean;
  /** Currently selected values */
  value: string[];
}

/**
 * <p>
 * Emits interface for the CheckboxGroup component
 * </p>
 */
export interface CheckboxGroupEmits {
  /** Emitted when checkbox selection changes */
  (e: 'change', value: string[]): void;
}

/**
 * <p>
 * Props interface for the AuthSet component
 * </p>
 */
export interface AuthSetProps {
  /** ID of the authentication object (user, dept, or group) */
  authObjectId: string | undefined;
  /** Type of authentication object */
  type: 'user' | 'dept' | 'group';
  /** Available permissions for selection */
  permissions: CheckboxOption[];
}

/**
 * <p>
 * Props interface for the GroupSet component
 * </p>
 */
export interface GroupSetProps {
  /** Type of entity (user, dept, or group) */
  type: 'user' | 'dept' | 'group';
  /** Whether data has been loaded */
  loaded: boolean;
  /** Whether the component is visible */
  visible: boolean;
  /** ID of the selected item */
  checkedId?: string;
  /** Application ID */
  appId?: string;
}

/**
 * <p>
 * Emits interface for the GroupSet component
 * </p>
 */
export interface GroupSetEmits {
  /** Emitted when selected ID changes */
  (e: 'update:checkedId', id: string | undefined): void;
  /** Emitted when loaded state changes */
  (e: 'update:loaded', value: boolean): void;
}

/**
 * <p>
 * Props interface for the main index component
 * </p>
 */
export interface IndexProps {
  /** Application ID */
  appId: string;
  /** Whether the modal is visible */
  visible: boolean;
}

/**
 * <p>
 * Emits interface for the main index component
 * </p>
 */
export interface IndexEmits {
  /** Emitted when visibility changes */
  (e: 'update:visible', value: boolean): void;
}

/**
 * <p>
 * List item information interface
 * </p>
 */
export interface ListInfo {
  /** Dynamic key-value pairs */
  [key: string]: string;
  /** Avatar image URL */
  avatar: string;
}

/**
 * <p>
 * Permission item interface
 * </p>
 */
export interface PermissionItem {
  /** Permission value */
  value: string;
  /** Permission label */
  label: string;
}
