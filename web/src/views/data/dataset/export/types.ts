/**
 * Export modal component props interface
 * <p>
 * Defines the properties that can be passed to the export modal component
 * </p>
 */
export interface ExportModalProps {
  /** Whether the modal is visible */
  visible: boolean;
  /** Project identifier for the export operation */
  projectId: string;
  /** Array of dataset IDs to export */
  id: string[];
}

/**
 * Export file format options
 * <p>
 * Supported export formats for dataset export functionality
 * </p>
 */
export type ExportFileFormat = 'JSON' | 'YAML';

/**
 * Export modal emit events interface
 * <p>
 * Defines the events that the export modal component can emit
 * </p>
 */
export interface ExportModalEmits {
  /** Event emitted when modal visibility changes */
  (event: 'update:visible', value: boolean): void;
  /** Event emitted when export operation is confirmed */
  (event: 'ok', value: string): void;
}

/**
 * Export configuration interface
 * <p>
 * Contains all the necessary configuration for the export operation
 * </p>
 */
export interface ExportConfig {
  /** Selected file format for export */
  fileType: ExportFileFormat;
  /** Whether the export operation is in progress */
  loading: boolean;
}
