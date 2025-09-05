/**
 * <p>Represents supported creator object types that can be assigned.</p>
 */
export type CreatorObjectType = 'USER' | 'GROUP' | 'DEPT';

/**
 * <p>Payload emitted when user confirms creator selection in the modal.</p>
 */
export interface CreatorSelectionPayload {
  creatorObjectId: string;
  creatorObjectType: CreatorObjectType;
  creatorObjectName?: string;
  avatar?: string;
}

/**
 * <p>Shape of option item returned by remote select APIs.</p>
 * <p>Allows both user and organization entities with optional avatar.</p>
 */
export interface CreatorSelectOption {
  id: string;
  name?: string;
  fullName?: string;
  avatar?: string;
}

/**
 * <p>Mapping for `Select` component to extract label and value from option.</p>
 */
export interface SelectFieldNames {
  label: string;
  value: string;
}

/**
 * <p>Props for the Select Creator Modal component.</p>
 */
export interface SelectCreatorModalProps {
  visible: boolean;
}
