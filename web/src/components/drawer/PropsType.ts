/**
 * Navigation item interface for drawer component
 */
export interface NavItem {
  icon: string;
  name: string;
  value: string;
  disabled?: boolean;
  auth?: string;
}
