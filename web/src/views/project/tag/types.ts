// Type definitions
export type TagItem = {
  id: string;
  name: string;
  showName: string;
  showTitle: string;
  hasEditPermission: boolean;
}
export type TagProps = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  disabled: boolean;
}

// Type definitions
export interface AddTagProps {
  projectId: string;
  visible: boolean;
}
