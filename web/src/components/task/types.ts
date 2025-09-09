import { Priority } from '@xcan-angus/infra';
import { TestType } from '@/enums/enums';

/**
 * <p>
 * Interface representing the form data structure for test task creation.
 * <p>
 * Contains all the necessary fields required for creating different types of test tasks.
 * <p>
 * This interface is used across multiple components for consistent data handling.
 */
export interface FormData {
  /** The deadline date for the test task in string format */
  deadlineDate: string;

  /** The priority level of the test task */
  priority: Priority;

  /** The ID of the user assigned to the test task, can be undefined if not assigned */
  assigneeId: string | undefined;

  /** The type of test to be performed (optional, defaults based on context) */
  testType?: TestType;

  /** Optional array of select options for dropdown components */
  selectOptions?: { id: string; name: string }[];

  /** Optional duration for performance tests */
  duration?: string;

  /** Optional number of threads for performance tests */
  threads?: number;
}
