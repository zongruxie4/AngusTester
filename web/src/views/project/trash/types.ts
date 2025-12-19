/**
 * <p>Represents a single item in the project trash.</p>
 * <p>Contains information about the deleted item and its metadata.</p>
 */
export type TrashItem = {
  /** Unique identifier for the trash item */
  id: string;

  /** ID of the user who originally created the item */
  createdBy: string;

  /** ID of the user who deleted the item */
  deletedBy: string;

  /** Avatar URL of the user who created the item */
  creatorAvatar: string;

  /** Avatar URL of the user who deleted the item */
  deletedByAvatar: string;

  /** Display name of the user who created the item */
  creator: string;

  /** Display name of the user who deleted the item */
  deletedByName: string;

  /** Name of the deleted target item */
  targetName: string;

  /** ID of the deleted target item */
  targetId: string;

  /** Date when the item was originally created */
  createdDate: string;

  /** Date when the item was moved to trash */
  deletedDate: string;

  /**
   * <p>Temporary field indicating whether the current user can perform actions on this item.</p>
   * <p>Set to false if user is admin, creator, or deleter; true otherwise.</p>
   */
  disabled?: boolean;
}

/**
 * <p>Props interface for the Trash component.</p>
 * <p>Defines the required properties for component initialization and operation.</p>
 */
export type TrashProps = {
  /** ID of the current project context */
  projectId: string;

  /** Information about the current user */
  userInfo: {
    /** Unique identifier of the current user */
    id: string;
  };

  /** Notification string to trigger list refresh */
  notify: string;
}
