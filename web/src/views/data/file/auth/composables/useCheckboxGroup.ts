import { computed } from 'vue';

/**
 * <p>
 * Checkbox group management composable for handling checkbox selection logic
 * </p>
 * <p>
 * This composable provides methods to manage checkbox state changes and
 * handles special logic for 'VIEW' permission when other permissions are selected
 * </p>
 */
export function useCheckboxGroup (
  props: { value: string[]; options: CheckboxOption[] },
  emit: (event: 'change', value: string[]) => void
) {
  /**
   * <p>
   * Computed property to check if a specific option is currently selected
   * </p>
   */
  const isOptionSelected = computed(() => (value: string) =>
    props.value.includes(value)
  );

  /**
   * <p>
   * Handles checkbox change events with special logic for 'VIEW' permission
   * </p>
   * <p>
   * When a non-VIEW permission is selected, VIEW permission is automatically added.
   * When VIEW permission is deselected, all other permissions are also removed.
   * </p>
   *
   * @param event - The checkbox change event containing checked state
   * @param value - The value of the checkbox that changed
   */
  const handleCheckboxChange = (event: CheckboxChangeEvent, value: string) => {
    const { checked } = event.target;

    if (checked && !props.value.includes(value)) {
      // Add the selected permission and VIEW permission if not already present
      const newValue = value !== 'VIEW' && !props.value.includes('VIEW')
        ? [...props.value, value, 'VIEW']
        : [...props.value, value];

      emit('change', newValue);
      return;
    }

    // Remove the deselected permission
    if (!checked) {
      const filteredValue = value !== 'VIEW'
        ? props.value.filter(item => item !== value)
        : [];

      emit('change', filteredValue);
    }
  };

  return {
    isOptionSelected,
    handleCheckboxChange
  };
}

/**
 * <p>
 * Interface for checkbox option configuration
 * </p>
 */
export interface CheckboxOption {
  /** Display label for the checkbox */
  label: string;
  /** Value associated with the checkbox */
  value: string;
}

/**
 * <p>
 * Interface for checkbox change event
 * </p>
 */
export interface CheckboxChangeEvent {
  target: {
    /** Whether the checkbox is checked */
    checked: boolean;
  };
}
