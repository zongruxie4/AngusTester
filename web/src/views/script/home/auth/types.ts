/**
 * <p>
 * Represents permission information with value and label.
 * </p>
 */
export interface Permission {
  value: string;
  label: string;
}

/**
 * <p>
 * Type for authentication object types.
 * </p>
 */
export type AuthObjectType = 'user' | 'dept' | 'group';

/**
 * <p>
 * Type for ID keys used in different entity types.
 * </p>
 */
export type IdKey = 'deptId' | 'groupId' | 'userId' | 'id';

/**
 * <p>
 * Props interface for AuthSet component.
 * </p>
 */
export interface AuthSetProps {
  projectId: string;
  authObjectId: string | undefined;
  type: AuthObjectType;
  permissions: Permission[];
}

/**
 * <p>
 * Props interface for GroupSet component.
 * </p>
 */
export interface GroupSetProps {
  type: AuthObjectType;
  loaded: boolean;
  visible: boolean;
  checkedId?: string;
  appId?: string;
}

/**
 * <p>
 * Props interface for CheckboxGroup component.
 * </p>
 */
export interface CheckboxGroupProps {
  options: Permission[];
  disabled: boolean;
  value: string[];
}

/**
 * <p>
 * Props interface for main index component.
 * </p>
 */
export interface IndexProps {
  projectId: string;
  appId: string;
  visible: boolean;
}
