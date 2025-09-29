import { ref } from 'vue';
import type { VariableDetail } from '../../types';
import { variable } from '@/api/tester';

/**
 * Composable function for managing variable value preview
 *
 * @returns Object containing value preview functions and state
 */
export function useValuePreview () {
  // State for managing visibility and error messages
  const visibilityIdSet = ref<Set<string>>(new Set());
  const errorMessageMap = ref<Map<string, string>>(new Map());

  /**
   * Check if a variable value contains mock functions
   *
   * @param value - Variable value to check
   * @returns True if value contains mock functions
   */
  const hasMockFunctions = (value: string): boolean => {
    return /@\w+\w*\([^)]*\)/.test(value);
  };

  /**
   * Check if a variable should be previewable
   *
   * @param data - Variable item to check
   * @returns True if variable can be previewed
   */
  const isPreviewable = (data: VariableDetail): boolean => {
    // Password variables are always previewable
    if (data.passwordValue) {
      return true;
    }

    // Check if variable has preview flag
    if (!data.preview) {
      return false;
    }

    // For static variables, check if they contain mock functions
    if (!data.extracted && !hasMockFunctions(data.value)) {
      return false;
    }
    return true;
  };

  /**
   * Load variable value for preview
   *
   * @param data - Variable item to preview
   */
  const loadVariableValue = async (data: VariableDetail) => {
    const id = data.id;

    try {
      const [error, res] = await variable.previewVariableValue(
        { id: data.id },
        { silence: false }
      );

      if (error) {
        errorMessageMap.value.set(id, error.message);
        return;
      }

      // Clear any previous errors and update value
      errorMessageMap.value.delete(id);
      if (res?.data) {
        data.value = res.data;
      }
    } catch (err) {
      errorMessageMap.value.set(id, 'Failed to load variable value');
    }
  };

  /**
   * Show variable value (make it visible)
   *
   * @param data - Variable item to show
   */
  const showVariableValue = (data: VariableDetail) => {
    const { id } = data;

    // For password variables, always show
    if (data.passwordValue) {
      visibilityIdSet.value.add(id);
      return;
    }

    // For other variables, check if they can be previewed
    if (!isPreviewable(data)) {
      return;
    }

    // Load value if needed
    loadVariableValue(data);
  };

  /**
   * Hide variable value (make it invisible)
   *
   * @param data - Variable item to hide
   */
  const hideVariableValue = (data: VariableDetail) => {
    visibilityIdSet.value.delete(data.id);
  };

  /**
   * Check if a variable value is currently visible
   *
   * @param id - Variable ID to check
   * @returns True if variable value is visible
   */
  const isValueVisible = (id: string): boolean => {
    return visibilityIdSet.value.has(id);
  };

  /**
   * Get error message for a variable
   *
   * @param id - Variable ID to get error for
   * @returns Error message or undefined if no error
   */
  const getErrorMessage = (id: string): string | undefined => {
    return errorMessageMap.value.get(id);
  };

  /**
   * Clear error message for a variable
   *
   * @param id - Variable ID to clear error for
   */
  const clearErrorMessage = (id: string) => {
    errorMessageMap.value.delete(id);
  };

  /**
   * Clear all error messages
   */
  const clearAllErrorMessages = () => {
    errorMessageMap.value.clear();
  };

  /**
   * Clear all visibility states
   */
  const clearAllVisibilityStates = () => {
    visibilityIdSet.value.clear();
  };

  /**
   * Reset all preview-related state
   */
  const resetPreviewState = () => {
    clearAllErrorMessages();
    clearAllVisibilityStates();
  };

  return {
    // State
    visibilityIdSet,
    errorMessageMap,

    // Utility functions
    hasMockFunctions,
    isPreviewable,
    isValueVisible,
    getErrorMessage,

    // Action functions
    loadVariableValue,
    showVariableValue,
    hideVariableValue,
    clearErrorMessage,
    clearAllErrorMessages,
    clearAllVisibilityStates,
    resetPreviewState
  };
}
