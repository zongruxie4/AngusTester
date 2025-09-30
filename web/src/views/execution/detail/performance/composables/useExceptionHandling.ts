import { ref } from 'vue';
import { ExceptionInfo } from '../types';

/**
 * Composable for managing exception information and error handling
 * <p>
 * Handles exception derivation from scheduling results and meter status messages
 * for displaying error information to users.
 */
export function useExceptionHandling () {
  /**
   * Current exception information for display
   */
  const exception = ref<ExceptionInfo | undefined>();

  /**
   * Derive exception info from scheduling or meter status/messages
   * <p>
   * Processes execution detail to extract meaningful error information
   * from scheduling results and meter status messages.
   */
  const setException = (detail: Record<string, any>) => {
    const lastSchedulingResult = detail?.lastSchedulingResult;
    const meterMessage = detail?.meterMessage;

    // Check for scheduling failures first
    if (lastSchedulingResult?.length) {
      const foundItem = lastSchedulingResult.find((f: any) => !f.success);
      if (foundItem) {
        exception.value = {
          code: foundItem.exitCode,
          message: foundItem.message,
          codeName: 'Exit Code',
          messageName: 'Failure Reason'
        };
        return;
      }

      // Check for meter message if no scheduling failure
      if (meterMessage) {
        exception.value = {
          code: detail?.meterStatus || '',
          message: meterMessage,
          codeName: 'Sampling Status',
          messageName: 'Failure Reason'
        };
        return;
      }
      return;
    }

    // Check for meter message if no scheduling result
    if (meterMessage) {
      exception.value = {
        code: detail?.meterStatus || '',
        message: meterMessage,
        codeName: 'Sampling Status',
        messageName: 'Failure Reason'
      };
      return;
    }

    // No exception found
    exception.value = undefined;
  };

  /**
   * Clear current exception information
   * <p>
   * Resets exception state when execution completes successfully
   */
  const clearException = () => {
    exception.value = undefined;
  };

  /**
   * Check if there is an active exception
   * <p>
   * Returns true if exception information is available
   */
  const hasException = () => {
    return exception.value !== undefined;
  };

  /**
   * Get current exception information
   * <p>
   * Returns the current exception object or undefined if none exists
   */
  const getException = () => {
    return exception.value;
  };

  return {
    // State
    exception,

    // Methods
    setException,
    clearException,
    hasException,
    getException
  };
}
