import type { ExportModalEmits } from '../types';

/**
 * <p>Composable for managing modal state and events</p>
 * <p>Handles modal visibility, cancellation, and confirmation events</p>
 *
 * @param emit - Function to emit events to parent component
 * @returns Object containing modal management methods
 */
export function useModal (emit: ExportModalEmits) {
  /**
   * <p>Handle modal cancellation</p>
   * <p>Emits the update:visible event to close the modal</p>
   */
  const handleCancel = (): void => {
    emit('update:visible', false);
  };

  /**
   * <p>Handle modal confirmation</p>
   * <p>Emits the ok event with the current operation result and closes the modal</p>
   *
   * @param result - The result value to pass to the parent component
   */
  const handleOk = (result: string): void => {
    emit('ok', result);
    emit('update:visible', false);
  };

  /**
   * <p>Close the modal</p>
   * <p>Emits the update:visible event to hide the modal</p>
   */
  const closeModal = (): void => {
    emit('update:visible', false);
  };

  return {
    handleCancel,
    handleOk,
    closeModal
  };
}
