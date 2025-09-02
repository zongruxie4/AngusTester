import type { ExportModalEmits } from '../types';

/**
 * Composable for managing modal state and events
 * <p>
 * Handles modal visibility, confirmation, and cancellation operations
 * </p>
 */
export function useModal (emit: ExportModalEmits) {
  /**
   * Closes the modal and resets its state
   * <p>
   * Emits the update:visible event with false to close the modal
   * </p>
   */
  const closeModal = (): void => {
    emit('update:visible', false);
  };

  /**
   * Handles the modal confirmation action
   * <p>
   * Emits the ok event to notify parent component of successful confirmation
   * </p>
   * @param value - Optional value to pass with the confirmation event
   */
  const confirmModal = (value?: string): void => {
    emit('ok', value || 'confirmed');
    closeModal();
  };

  /**
   * Handles the modal cancellation action
   * <p>
   * Closes the modal without emitting confirmation
   * </p>
   */
  const cancelModal = (): void => {
    closeModal();
  };

  return {
    closeModal,
    confirmModal,
    cancelModal
  };
}
