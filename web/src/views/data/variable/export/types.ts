/**
 * <p>Export modal component props interface</p>
 * <p>Defines the properties that can be passed to the export modal component</p>
 */
export interface ExportModalProps {
  /** Whether the modal is visible */
  visible: boolean;
  /** Project identifier for the export operation */
  projectId: string;
  /** Array of variable IDs to export (optional) */
  id?: string[];
}

/**
 * <p>File format options for export</p>
 * <p>Supported export formats for variable data</p>
 */
export type ExportFileFormat = 'JSON' | 'YAML';

/**
 * <p>Export modal emit events interface</p>
 * <p>Defines the events that the component can emit to its parent</p>
 */
export interface ExportModalEmits {
  /** Event emitted when modal visibility changes */
  (event: 'update:visible', value: boolean): void;
  /** Event emitted when export operation is confirmed */
  (event: 'ok', value: string): void;
}

/**
 * <p>Export configuration interface</p>
 * <p>Contains all the configuration needed for the export operation</p>
 */
export interface ExportConfig {
  /** Selected file format for export */
  fileType: ExportFileFormat;
  /** Loading state during export operation */
  loading: boolean;
}
