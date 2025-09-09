// Base data item interface for scenario items
export interface DataItem {
  id: string;
  name: string;
  auth: boolean;
}

// Permission configuration interface
export interface Permission {
  value: string;
  label: string;
}

// Auth object type enumeration
export type AuthObjectType = 'user' | 'dept' | 'group';

// Permission map item interface
export interface PermissionMapItem {
  id: string;
  creatorFlag: boolean;
  permissions: string[];
}

// Component props interfaces
export interface AuthSetProps {
  projectId: string;
  authObjectId: string;
  type: AuthObjectType;
  permissions: Permission[];
}

export interface CheckboxGroupProps {
  options: Permission[];
  disabled: boolean;
  value: string[];
}

export interface GroupSetProps {
  type: AuthObjectType;
  loaded: boolean;
  checkedId?: string;
  appId?: string;
}

// Search and pagination interfaces
export interface SearchParams {
  pageNo: number;
  pageSize: number;
  projectId: string;
  hasPermission: string;
  admin: boolean;
  filters?: Array<{
    key: 'name';
    op: 'MATCH_END';
    value: string;
  }>;
}

export interface AuthSearchParams {
  pageSize: number;
  authObjectId: string;
  authObjectType: AuthObjectType;
  filters?: Array<{
    key: 'scenarioId';
    op: 'IN';
    value: string[];
  }>;
}

// Event handler types
export interface CheckboxChangeEvent {
  target: {
    checked: boolean;
  };
}
