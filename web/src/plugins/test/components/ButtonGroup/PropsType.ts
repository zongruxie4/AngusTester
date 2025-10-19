/**
 * ButtonGroup Component Type Definitions
 * 
 * This module defines the types used by the ButtonGroup component
 * for menu item configuration and action handling.
 */

/**
 * Available action keys for button group menu items
 * 
 * @description Defines all possible button actions in the button group
 * - select: Select/generate test script
 * - test: Create execution/test
 * - debug: Debug mode
 * - export: Export functionality
 * - import: Import functionality
 * - codeView: Switch to code view
 * - pageView: Switch to page view
 * - permission: Manage permissions
 * - follow: Add to follow list
 * - cancelFollow: Remove from follow list
 * - favourite: Add to favourites
 * - cancelFavourite: Remove from favourites
 * - refresh: Refresh data
 * - save: Save changes
 */
export type ButtonGroupMenuKey = 
  | 'select'
  | 'test'
  | 'debug'
  | 'export'
  | 'import'
  | 'codeView'
  | 'pageView'
  | 'permission'
  | 'follow'
  | 'cancelFollow'
  | 'favourite'
  | 'cancelFavourite'
  | 'refresh'
  | 'save';

/**
 * Button group menu item interface
 * 
 * @property name - Display name of the menu item (localized)
 * @property icon - Icon identifier for the menu item (icon font class)
 * @property key - Unique identifier for the menu action
 */
export interface ButtonGroupMenuItem {
  name: string;
  icon: string;
  key: ButtonGroupMenuKey;
}
