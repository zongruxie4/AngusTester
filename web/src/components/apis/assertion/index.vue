<script setup lang="ts">
// Vue core imports
import { computed, nextTick, onMounted, ref, watch } from 'vue';
// UI component imports
import { Checkbox, Switch } from 'ant-design-vue';
import { CheckboxChangeEvent } from 'ant-design-vue/lib/checkbox/interface';
import { Arrow, Icon, Input, SelectEnum, Select, Validate } from '@xcan-angus/vue-ui';
// Infrastructure imports
import { EnumMessage, AssertionCondition, AssertionType, HttpExtractionLocation, utils, enumUtils, ExtractionMethod } from '@xcan-angus/infra';
import elementResizeDetector from 'element-resize-detector';
import { useI18n } from 'vue-i18n';

// Component imports
import MatchItemPopover from './MacthItemPopover.vue';
import ExpectedPopover from './ExpectedPopover.vue';
import ConditionPopover from './ConditionPopover.vue';
// Utility imports
import expressionUtils from './utils/expression';
import jsonpath from './utils/jsonpath';
import xpath from './utils/xpath';
import regexp from './utils/regexp';
import { Extraction } from './utils/extract/PropsType';
import { FormItem } from './PropsType';

const { t } = useI18n();

// Component props interface
interface Props {
  id: string | undefined;
  value: FormItem[];
  num?: number;
  viewType?: boolean;
}

// Element resize detector instance
const elementResizeDetectorInstance = elementResizeDetector({ strategy: 'scroll' });

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  value: undefined,
  num: undefined,
  viewType: false
});

// Component events
const emit = defineEmits<{
  (e: 'update:num', num: number): void;
  (e: 'rendered', value: true);
}>();

// Template refs
const assertionWrapperRef = ref<HTMLElement>();
const assertionNameRefs = ref<any[]>([]);
const assertionConditionRefs = ref<any[]>([]);
const extractionExpressionRefs = ref<any[]>([]);

// Core data management
const assertionIdList = ref<string[]>([]);
const assertionDataMap = ref<{ [key: string]: FormItem }>({});

// UI state management
const expandedAssertionMap = ref<{ [key: string]: boolean }>({});

// Interaction state tracking
const focusedAssertionSet = ref<Set<string>>(new Set());
const previouslyCheckedAssertionSet = ref<Set<string>>(new Set()); // Previously checked assertions to prevent auto-checking when typing names
const checkedAssertionSet = ref<Set<string>>(new Set());
const extractValueAssertionSet = ref<Set<string>>(new Set());
const extractDisabledAssertionSet = ref<Set<string>>(new Set()); // Disabled extract value assertions

// Validation error tracking
const assertionNameErrorSet = ref<Set<string>>(new Set());
const assertionNameErrorMsgMap = ref<{ [key: string]: string | undefined }>({});
const assertionConditionErrorSet = ref<Set<string>>(new Set());
const assertionConditionErrorMsgMap = ref<{ [key: string]: string | undefined }>({});
const assertionTypeErrorSet = ref<Set<string>>(new Set());
const headerNameErrorSet = ref<Set<string>>(new Set());
const assertionConditionValidationErrorSet = ref<Set<string>>(new Set());
const expectedValueErrorSet = ref<Set<string>>(new Set());

const extractionExpressionErrorSet = ref<Set<string>>(new Set());
const extractionExpressionErrorMsgMap = ref<{ [key: string]: string }>({});

const extractionLocationErrorSet = ref<Set<string>>(new Set());
const extractionMethodErrorSet = ref<Set<string>>(new Set());
const extractionParameterNameErrorSet = ref<Set<string>>(new Set());

// Constants for validation and UI control
const EXTRACTION_LOCATIONS_WITHOUT_PARAMETER_NAME: readonly ['REQUEST_RAW_BODY', 'RESPONSE_BODY'] = ['REQUEST_RAW_BODY', 'RESPONSE_BODY'];

// Expected value display control - only show when assertion condition is not null/empty related
const showExpectedValueSet = ref<Set<string>>(new Set());
const NULL_EMPTY_CONDITIONS: readonly Partial<AssertionCondition>[] = ['IS_NULL', 'IS_EMPTY', 'NOT_EMPTY', 'NOT_NULL'];

// Expression display control - only show for regex, xpath, jsonpath expressions
const showExpressionSet = ref<Set<string>>(new Set());
const assertionExpressionErrorSet = ref<Set<string>>(new Set());
const assertionExpressionErrorMsgMap = ref<{ [key: string]: string }>({});

// Assertion condition options
const assertionConditionOptions = ref<{ message: string; value: string; }[]>([]);

/**
 * Load assertion condition options from enum
 */
const loadAssertionConditionOptions = async (): Promise<void> => {
  assertionConditionOptions.value = enumUtils.enumToMessages(AssertionCondition);
};

// Numeric comparison conditions
const NUMERIC_COMPARISON_CONDITIONS = ['EQUAL', 'NOT_EQUAL', 'GREATER_THAN', 'GREATER_THAN_EQUAL', 'LESS_THAN', 'LESS_THAN_EQUAL'];
// Computed assertion condition options map based on assertion type
const assertionConditionOptionsMap = computed(() => {
  const assertionData = assertionDataMap.value;
  const availableOptions = assertionConditionOptions.value;
  return assertionIdList.value.reduce((optionsMap, assertionId) => {
    const assertionType = assertionData[assertionId].type;
    if (assertionType) {
      if (assertionType === 'BODY' || assertionType === 'HEADER') {
        optionsMap[assertionId] = availableOptions;
      } else {
        optionsMap[assertionId] = availableOptions.filter((option) => NUMERIC_COMPARISON_CONDITIONS.includes(option.value));
      }
    } else {
      optionsMap[assertionId] = availableOptions;
    }

    return optionsMap;
  }, {} as { [key: string]: { message: string; value: string; }[] });
});

// Extraction location options
const extractionLocationOptions = ref<EnumMessage<HttpExtractionLocation>[]>([]);

/**
 * Load extraction location options from enum
 */
const loadExtractionLocationOptions = async (): Promise<void> => {
  extractionLocationOptions.value = enumUtils.enumToMessages(HttpExtractionLocation);
};

// Computed extraction location options map based on extraction method
const extractionLocationOptionsMap = computed(() => {
  const assertionData = assertionDataMap.value;
  const availableOptions = extractionLocationOptions.value;
  return assertionIdList.value.reduce((optionsMap, assertionId) => {
    const extractionMethod = assertionData[assertionId].extraction.method;
    if (extractionMethod && extractionMethod === 'EXACT_VALUE') {
      optionsMap[assertionId] = availableOptions.filter((option) =>
        !['RESPONSE_BODY', 'RESPONSE_HEADER', 'REQUEST_RAW_BODY'].includes(option.value)
      );
    } else {
      optionsMap[assertionId] = availableOptions;
    }

    return optionsMap;
  }, {} as { [key: string]: EnumMessage<HttpExtractionLocation>[] });
});

/**
 * Handle assertion checkbox change event
 * @param event - Checkbox change event
 * @param assertionId - Assertion identifier
 */
const handleAssertionCheckboxChange = (event: CheckboxChangeEvent, assertionId: string): void => {
  previouslyCheckedAssertionSet.value.add(assertionId);
  const isChecked = event.target.checked;
  if (isChecked) {
    checkedAssertionSet.value.add(assertionId);
    return;
  }

  checkedAssertionSet.value.delete(assertionId);
};

/**
 * Handle extract value switch change event
 * @param isChecked - Switch checked state
 * @param assertionId - Assertion identifier
 */
const handleExtractValueSwitchChange = async (isChecked: boolean | number | string, assertionId: string): Promise<void> => {
  // Clear related validation errors
  expectedValueErrorSet.value.delete(assertionId);
  extractionParameterNameErrorSet.value.delete(assertionId);
  extractionLocationErrorSet.value.delete(assertionId);
  extractionMethodErrorSet.value.delete(assertionId);
  assertionDataMap.value[assertionId].expected = '';

  if (isChecked) {
    extractValueAssertionSet.value.add(assertionId);
    return;
  }

  extractValueAssertionSet.value.delete(assertionId);
};

/**
 * Validate assertion name for uniqueness and non-empty value
 * @param assertionId - Assertion identifier
 * @param assertionName - Assertion name to validate
 */
const validateAssertionName = (assertionId: string, assertionName: string | undefined): void => {
  const existingNames = Object.entries(assertionDataMap.value).reduce((nameSet, [currentId, { name: currentName }]) => {
    if (assertionId !== currentId && currentName) {
      nameSet.add(currentName);
    }
    return nameSet;
  }, new Set() as Set<string>);

  if (!assertionName) {
    assertionNameErrorSet.value.add(assertionId);
    assertionNameErrorMsgMap.value[assertionId] = t('xcan_apiAssert.nameCannotBeEmpty');
    return;
  }

  if (existingNames.has(assertionName)) {
    assertionNameErrorSet.value.add(assertionId);
    assertionNameErrorMsgMap.value[assertionId] = t('xcan_apiAssert.nameDuplicate');
    return;
  }

  assertionNameErrorSet.value.delete(assertionId);
  assertionNameErrorMsgMap.value[assertionId] = undefined;
};

/**
 * Handle input focus event
 * @param assertionId - Assertion identifier
 */
const handleInputFocus = (assertionId: string): void => {
  focusedAssertionSet.value.add(assertionId);
};

/**
 * Handle input blur event
 * @param assertionId - Assertion identifier
 */
const handleInputBlur = (assertionId: string): void => {
  focusedAssertionSet.value.delete(assertionId);
};

/**
 * Handle assertion name input blur event
 * @param event - Input blur event
 * @param assertionId - Assertion identifier
 */
const handleAssertionNameBlur = (event: { target: { value: string } }, assertionId: string): void => {
  handleInputBlur(assertionId);

  const trimmedValue = event.target.value?.trim();
  assertionDataMap.value[assertionId].name = trimmedValue;

  if (trimmedValue && !hasEmptyAssertionName(assertionId)) {
    addEmptyAssertionRecord();
  }

  if (hasAssertionContent(assertionId, assertionDataMap.value[assertionId])) {
    validateAssertionName(assertionId, trimmedValue);
  }
};

/**
 * Handle assertion name input enter key press
 * @param inputIndex - Input field index
 */
const handleAssertionNameEnterPress = (inputIndex: number): void => {
  if (!assertionNameRefs.value[inputIndex]) {
    return;
  }

  assertionNameRefs.value[inputIndex]?.blur?.();
};

/**
 * Handle assertion name input change event
 * @param assertionId - Assertion identifier
 */
const handleAssertionNameChange = (assertionId: string): void => {
  if (!previouslyCheckedAssertionSet.value.has(assertionId)) {
    checkedAssertionSet.value.add(assertionId);
    previouslyCheckedAssertionSet.value.add(assertionId);
  }

  assertionNameErrorSet.value.delete(assertionId);
  delete assertionNameErrorMsgMap.value[assertionId];
};

/**
 * Handle assertion condition input change event
 * @param assertionId - Assertion identifier
 */
const handleAssertionConditionChange = (assertionId: string): void => {
  assertionConditionErrorSet.value.delete(assertionId);
  assertionConditionErrorMsgMap.value[assertionId] = undefined;
};

/**
 * Handle assertion condition input blur event - validates expression format
 * @param event - Input blur event
 * @param assertionId - Assertion identifier
 */
const handleAssertionConditionBlur = async (event: { target: { value: string } }, assertionId: string): Promise<void> => {
  handleInputBlur(assertionId);

  const trimmedValue = event.target.value?.trim();
  assertionDataMap.value[assertionId].condition = trimmedValue;

  if (!trimmedValue) {
    return;
  }

  const expressionParts = expressionUtils.split(trimmedValue);
  if (!expressionParts) {
    assertionConditionErrorSet.value.add(assertionId);
    assertionConditionErrorMsgMap.value[assertionId] = t('xcan_apiAssert.conditionExpressionFormatError');
  }
};

/**
 * Handle assertion condition input enter key press
 * @param inputIndex - Input field index
 */
const handleAssertionConditionEnterPress = (inputIndex: number): void => {
  if (!assertionConditionRefs.value[inputIndex]) {
    return;
  }

  assertionConditionRefs.value[inputIndex]?.blur?.();
};

/**
 * Handle assertion type change event
 * @param assertionType - New assertion type
 * @param assertionId - Assertion identifier
 */
const handleAssertionTypeChange = (assertionType: AssertionType, assertionId: string): void => {
  assertionTypeErrorSet.value.delete(assertionId);

  if (assertionType !== 'HEADER') {
    assertionDataMap.value[assertionId].parameterName = '';
    headerNameErrorSet.value.delete(assertionId);
  }

  const currentCondition = assertionDataMap.value[assertionId].assertionCondition || '';
  // Handle numeric assertion types
  if (['STATUS', 'BODY_SIZE', 'SIZE', 'DURATION'].includes(assertionType)) {
    if (showExpressionSet.value.has(assertionId)) {
      showExpressionSet.value.delete(assertionId);
      assertionDataMap.value[assertionId].expression = undefined;
      assertionDataMap.value[assertionId].matchItem = undefined;
      assertionDataMap.value[assertionId].expected = undefined;
    }

    if (!NUMERIC_COMPARISON_CONDITIONS.includes(currentCondition)) {
      assertionDataMap.value[assertionId].assertionCondition = 'EQUAL';
    }

    // Disable extract value selection
    extractValueAssertionSet.value.delete(assertionId);
    extractDisabledAssertionSet.value.add(assertionId);
    return;
  }

  if (!['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(currentCondition)) {
    extractDisabledAssertionSet.value.delete(assertionId);
  }
};

/**
 * Handle header name input change event
 * @param assertionId - Assertion identifier
 */
const handleHeaderNameChange = (assertionId: string): void => {
  headerNameErrorSet.value.delete(assertionId);
};

/**
 * Handle assertion condition change event
 * @param conditionValue - New assertion condition value
 * @param assertionId - Assertion identifier
 */
const handleAssertionConditionSelectionChange = (conditionValue: typeof NULL_EMPTY_CONDITIONS[number], assertionId: string): void => {
  assertionConditionValidationErrorSet.value.delete(assertionId);
  showExpressionSet.value.delete(assertionId);
  showExpectedValueSet.value.add(assertionId);

  if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(conditionValue)) {
    extractValueAssertionSet.value.delete(assertionId);
    extractDisabledAssertionSet.value.add(assertionId);
    showExpressionSet.value.add(assertionId);
    return;
  }

  if (NULL_EMPTY_CONDITIONS.includes(conditionValue)) {
    extractDisabledAssertionSet.value.add(assertionId);
    showExpectedValueSet.value.delete(assertionId);
    return;
  }

  extractDisabledAssertionSet.value.delete(assertionId);
};

/**
 * Handle assertion expression input change event
 * @param assertionId - Assertion identifier
 */
const handleAssertionExpressionChange = (assertionId: string): void => {
  assertionExpressionErrorSet.value.delete(assertionId);
  delete assertionExpressionErrorMsgMap.value[assertionId];
};

/**
 * Handle extraction method change event
 * @param assertionId - Assertion identifier
 */
const handleExtractionMethodChange = (assertionId: string): void => {
  extractionMethodErrorSet.value.delete(assertionId);
  if (assertionDataMap.value[assertionId].extraction.method === 'EXACT_VALUE') {
    assertionDataMap.value[assertionId].extraction.expression = '';
    extractionExpressionErrorSet.value.delete(assertionId);
    delete extractionExpressionErrorMsgMap.value[assertionId];

    const extractionLocation = assertionDataMap.value[assertionId].extraction.location;
    if (extractionLocation && ['RESPONSE_BODY', 'RESPONSE_HEADER'].includes(extractionLocation)) {
      assertionDataMap.value[assertionId].extraction.location = undefined;
    }

    return;
  }

  const extractionExpression = assertionDataMap.value[assertionId].extraction.expression;
  if (extractionExpression) {
    validateExtractionExpression(extractionExpression, assertionId);
  }
};

/**
 * Handle extraction location change event
 * @param assertionId - Assertion identifier
 */
const handleExtractionLocationChange = (assertionId: string): void => {
  extractionLocationErrorSet.value.delete(assertionId);

  const extractionLocation = assertionDataMap.value[assertionId].extraction.location || '';
  if (!extractionLocation) {
    extractionParameterNameErrorSet.value.delete(assertionId);
  }

  if (EXTRACTION_LOCATIONS_WITHOUT_PARAMETER_NAME.includes(extractionLocation as any)) {
    assertionDataMap.value[assertionId].extraction.parameterName = undefined;
    extractionParameterNameErrorSet.value.delete(assertionId);
  }
};

/**
 * Handle extraction parameter name input change event
 * @param assertionId - Assertion identifier
 */
const handleExtractionParameterNameChange = (assertionId: string): void => {
  extractionParameterNameErrorSet.value.delete(assertionId);
};

/**
 * Handle extraction expression input change event
 * @param assertionId - Assertion identifier
 */
const handleExtractionExpressionChange = (assertionId: string): void => {
  extractionExpressionErrorSet.value.delete(assertionId);
  delete extractionExpressionErrorMsgMap.value[assertionId];
};

/**
 * Handle extraction expression input blur event
 * @param event - Input blur event
 * @param assertionId - Assertion identifier
 */
const handleExtractionExpressionBlur = (event: { target: { value: string } }, assertionId: string): void => {
  handleInputBlur(assertionId);

  const trimmedValue = event.target.value?.trim();
  if (!trimmedValue) {
    return;
  }

  validateExtractionExpression(trimmedValue, assertionId);
};

/**
 * Handle extraction expression input enter key press
 * @param inputIndex - Input field index
 */
const handleExtractionExpressionEnterPress = (inputIndex: number): void => {
  if (!extractionExpressionRefs.value[inputIndex]) {
    return;
  }

  extractionExpressionRefs.value[inputIndex]?.blur?.();
};

/**
 * Handle expected value input change event
 * @param assertionId - Assertion identifier
 */
const handleExpectedValueChange = (assertionId: string): void => {
  expectedValueErrorSet.value.delete(assertionId);
};

/**
 * Check if there is an empty assertion name slot available
 * @param excludedAssertionId - Assertion ID to exclude from check
 * @returns True if there is an empty name slot available
 */
const hasEmptyAssertionName = (excludedAssertionId?: string): boolean => {
  const assertionNames = Object.entries(assertionDataMap.value).reduce((nameList, [currentId, { name }]) => {
    if (excludedAssertionId) {
      if (name && excludedAssertionId !== currentId) {
        nameList.push(name);
      }
    } else {
      if (name) {
        nameList.push(name);
      }
    }
    return nameList;
  }, [] as string[]);

  const totalAssertionCount = assertionIdList.value.length;
  return assertionNames.length < (excludedAssertionId ? (totalAssertionCount - 1) : totalAssertionCount);
};

/**
 * Handle assertion deletion - automatically adds empty record if last one is deleted
 * @param assertionIndex - Index of assertion to delete
 * @param assertionId - ID of assertion to delete
 */
const handleAssertionDelete = (assertionIndex: number, assertionId: string): void => {
  assertionIdList.value.splice(assertionIndex, 1);
  assertionNameRefs.value.splice(assertionIndex, 1);
  focusedAssertionSet.value.delete(assertionId);
  previouslyCheckedAssertionSet.value.delete(assertionId);
  checkedAssertionSet.value.delete(assertionId);
  extractValueAssertionSet.value.delete(assertionId);
  extractDisabledAssertionSet.value.delete(assertionId);
  delete assertionDataMap.value[assertionId];
  delete expandedAssertionMap.value[assertionId];

  // Clear all validation errors for this assertion
  assertionNameErrorSet.value.delete(assertionId);
  delete assertionNameErrorMsgMap.value[assertionId];
  assertionConditionErrorSet.value.delete(assertionId);
  delete assertionConditionErrorMsgMap.value[assertionId];
  assertionTypeErrorSet.value.delete(assertionId);
  headerNameErrorSet.value.delete(assertionId);
  assertionConditionValidationErrorSet.value.delete(assertionId);
  expectedValueErrorSet.value.delete(assertionId);

  extractionExpressionErrorSet.value.delete(assertionId);
  delete extractionExpressionErrorMsgMap.value[assertionId];

  extractionParameterNameErrorSet.value.delete(assertionId);
  extractionLocationErrorSet.value.delete(assertionId);
  extractionMethodErrorSet.value.delete(assertionId);

  showExpectedValueSet.value.delete(assertionId);
  showExpressionSet.value.delete(assertionId);

  if (!hasEmptyAssertionName()) {
    addEmptyAssertionRecord();
  }
};

/**
 * Generate default extraction configuration
 * @returns Default extraction object
 */
const generateDefaultExtraction = (): Extraction => {
  return {
    defaultValue: undefined,
    expression: undefined,
    failureMessage: undefined,
    finalValue: undefined,
    location: undefined,
    matchItem: undefined,
    method: undefined,
    name: undefined,
    parameterName: undefined,
    value: undefined
  };
};

/**
 * Generate default assertion form item
 * @returns Default form item object
 */
const generateDefaultAssertionData = (): FormItem => {
  return {
    assertionCondition: undefined,
    condition: undefined,
    description: undefined,
    expression: undefined,
    matchItem: undefined,
    enabled: false,
    expected: undefined,
    extraction: generateDefaultExtraction(),
    parameterName: undefined,
    name: undefined,
    type: undefined
  };
};

/**
 * Add empty assertion record
 */
const addEmptyAssertionRecord = (): void => {
  if (props.viewType) { // Don't auto-add in readonly mode
    return;
  }
  const assertionId = utils.uuid('a');
  assertionIdList.value.push(assertionId);
  expandedAssertionMap.value[assertionId] = true;
  assertionDataMap.value[assertionId] = generateDefaultAssertionData();
  showExpectedValueSet.value.add(assertionId);
};

/**
 * Reset all validation errors
 */
const resetValidationErrors = (): void => {
  assertionNameErrorSet.value.clear();
  assertionNameErrorMsgMap.value = {};
  assertionConditionErrorSet.value.clear();
  assertionConditionErrorMsgMap.value = {};
  assertionTypeErrorSet.value.clear();
  headerNameErrorSet.value.clear();
  assertionConditionValidationErrorSet.value.clear();
  expectedValueErrorSet.value.clear();

  extractionExpressionErrorSet.value.clear();
  extractionExpressionErrorMsgMap.value = {};

  assertionExpressionErrorSet.value.clear();
  assertionExpressionErrorMsgMap.value = {};

  extractionParameterNameErrorSet.value.clear();
  extractionLocationErrorSet.value.clear();
  extractionMethodErrorSet.value.clear();
};

/**
 * Reset component state to initial values
 */
const resetComponentState = (): void => {
  assertionIdList.value = [];
  assertionNameRefs.value = [];
  focusedAssertionSet.value.clear();
  previouslyCheckedAssertionSet.value.clear();
  checkedAssertionSet.value.clear();
  extractValueAssertionSet.value.clear();
  extractDisabledAssertionSet.value.clear();
  showExpectedValueSet.value.clear();
  showExpressionSet.value.clear();

  assertionDataMap.value = {};
  expandedAssertionMap.value = {};

  resetValidationErrors();
};

/**
 * Check if assertion has any content/data
 * @param assertionId - Assertion identifier
 * @param assertionData - Assertion data object
 * @returns True if assertion has content
 */
const hasAssertionContent = (assertionId: string, assertionData: { [key: string]: any }): boolean => {
  const isExtractMode = extractValueAssertionSet.value.has(assertionId);
  const dataEntries = Object.entries(assertionData);
  for (let i = 0, len = dataEntries.length; i < len; i++) {
    const [key, value] = dataEntries[i];
    if (key === 'extraction') {
      if (isExtractMode && hasAssertionContent(assertionId, value)) {
        return true;
      }
    } else if (key !== 'result') {
      if (value) {
        return true;
      }
    }
  }

  return false;
};

/**
 * Validate extraction expression based on method type
 * @param expressionValue - Expression value to validate
 * @param assertionId - Assertion identifier
 * @returns True if expression is valid
 */
const validateExtractionExpression = (expressionValue: string, assertionId: string): boolean => {
  const extractionMethod = assertionDataMap.value[assertionId].extraction.method;
  let validationResult: { valid: boolean; message: string; } = { valid: false, message: '' };

  if (extractionMethod === 'JSON_PATH') {
    validationResult = jsonpath.isValid(expressionValue);
  } else if (extractionMethod === 'X_PATH') {
    validationResult = xpath.isValid(expressionValue);
  } else {
    validationResult = regexp.isValid(expressionValue);
  }

  if (validationResult.valid) {
    extractionExpressionErrorSet.value.delete(assertionId);
    delete extractionExpressionErrorMsgMap.value[assertionId];
    return true;
  }

  extractionExpressionErrorSet.value.add(assertionId);
  extractionExpressionErrorMsgMap.value[assertionId] = validationResult.message;
  expandedAssertionMap.value[assertionId] = true;
  return false;
};

/**
 * Validate assertion expression based on condition type
 * @param expressionValue - Expression value to validate
 * @param assertionId - Assertion identifier
 * @returns True if expression is valid
 */
const validateAssertionExpression = (expressionValue: string, assertionId: string): boolean => {
  const assertionCondition = assertionDataMap.value[assertionId].assertionCondition;
  let validationResult: { valid: boolean; message: string; } = { valid: false, message: '' };

  if (assertionCondition === 'JSON_PATH_MATCH') {
    validationResult = jsonpath.isValid(expressionValue);
  } else if (assertionCondition === 'XPATH_MATCH') {
    validationResult = xpath.isValid(expressionValue);
  } else {
    validationResult = regexp.isValid(expressionValue);
  }

  if (validationResult.valid) {
    assertionExpressionErrorSet.value.delete(assertionId);
    delete assertionExpressionErrorMsgMap.value[assertionId];
    return true;
  }

  assertionExpressionErrorSet.value.add(assertionId);
  assertionExpressionErrorMsgMap.value[assertionId] = validationResult.message;
  expandedAssertionMap.value[assertionId] = true;
  return false;
};

/**
 * Validate all assertions - returns true if validation passes, false if validation fails
 * @returns True if validation passes, false if validation fails
 */
const validateAllAssertions = (): boolean => {
  resetValidationErrors();
  const assertionIds = assertionIdList.value;
  const seenNames: Set<string> = new Set();

  for (let i = 0, len = assertionIds.length; i < len; i++) {
    const assertionId = assertionIds[i];
    const currentAssertionData = assertionDataMap.value[assertionId];

    if (hasAssertionContent(assertionId, currentAssertionData)) {
      // Validate assertion name
      if (!currentAssertionData.name) {
        assertionNameErrorSet.value.add(assertionId);
        assertionNameErrorMsgMap.value[assertionId] = t('xcan_apiAssert.nameCannotBeEmpty');
      } else if (seenNames.has(currentAssertionData.name)) {
        assertionNameErrorSet.value.add(assertionId);
        assertionNameErrorMsgMap.value[assertionId] = t('xcan_apiAssert.nameDuplicate');
      } else {
        seenNames.add(currentAssertionData.name);
      }

      // Validate assertion condition expression
      if (currentAssertionData.condition && !expressionUtils.isValid(currentAssertionData.condition)) {
        assertionConditionErrorSet.value.add(assertionId);
        assertionConditionErrorMsgMap.value[assertionId] = t('xcan_apiAssert.conditionExpressionFormatError');
      }

      // Validate assertion type
      if (!currentAssertionData.type) {
        assertionTypeErrorSet.value.add(assertionId);
      } else {
        // Header name is required when assertion type is HEADER
        if (currentAssertionData.type === 'HEADER') {
          if (!currentAssertionData.parameterName) {
            headerNameErrorSet.value.add(assertionId);
          }
        }
      }

      // Validate assertion condition
      if (!currentAssertionData.assertionCondition) {
        assertionConditionValidationErrorSet.value.add(assertionId);
      }

      // Handle expression-based conditions
      if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(currentAssertionData.assertionCondition!)) {
        if (!currentAssertionData.expression) {
          assertionExpressionErrorSet.value.add(assertionId);
          expandedAssertionMap.value[assertionId] = true;
        } else {
          validateAssertionExpression(currentAssertionData.expression!, assertionId);
        }
      } else if (showExpectedValueSet.value.has(assertionId)) {
        // Handle expected value validation
        if (!extractValueAssertionSet.value.has(assertionId)) {
          if (!currentAssertionData.expected) {
            expectedValueErrorSet.value.add(assertionId);
            expandedAssertionMap.value[assertionId] = true;
          }
        } else { // Handle extraction expected value
          if (currentAssertionData.extraction.method !== 'EXACT_VALUE') {
            if (!currentAssertionData.extraction.expression) {
              extractionExpressionErrorSet.value.add(assertionId);
              expandedAssertionMap.value[assertionId] = true;
            } else {
              validateExtractionExpression(currentAssertionData.extraction.expression!, assertionId);
            }
          }

          // Raw request body and response body don't need parameter name validation
          if (!EXTRACTION_LOCATIONS_WITHOUT_PARAMETER_NAME.includes(currentAssertionData.extraction.location! as any)) {
            if (!currentAssertionData.extraction.parameterName) {
              extractionParameterNameErrorSet.value.add(assertionId);
              expandedAssertionMap.value[assertionId] = true;
            }
          }

          if (!currentAssertionData.extraction.location) {
            extractionLocationErrorSet.value.add(assertionId);
            expandedAssertionMap.value[assertionId] = true;
          }

          if (!currentAssertionData.extraction.method) {
            extractionMethodErrorSet.value.add(assertionId);
            expandedAssertionMap.value[assertionId] = true;
          }
        }
      }
    }
  }

  return !(assertionNameErrorSet.value.size ||
    assertionTypeErrorSet.value.size ||
    headerNameErrorSet.value.size ||
    assertionConditionValidationErrorSet.value.size ||
    assertionExpressionErrorSet.value.size ||
    expectedValueErrorSet.value.size ||
    extractionExpressionErrorSet.value.size ||
    extractionParameterNameErrorSet.value.size ||
    extractionLocationErrorSet.value.size ||
    extractionMethodErrorSet.value.size
  );
};

/**
 * Get processed assertion data and extracted variables
 * @returns Object containing assertion data array and variable names array
 */
const getProcessedAssertionData = (): { data: FormItem[], variables: string[] } => {
  const assertionIds = assertionIdList.value;
  const assertionData = assertionDataMap.value;
  const extractValueSet = extractValueAssertionSet.value;
  const checkedSet = checkedAssertionSet.value;
  const processedAssertions: FormItem[] = [];
  const variableNameSet = new Set<string>();

  for (let i = 0, len = assertionIds.length; i < len; i++) {
    const assertionId = assertionIds[i];
    if (assertionData[assertionId].name) {
      const processedAssertion = JSON.parse(JSON.stringify(assertionData[assertionId]));
      processedAssertion.enabled = checkedSet.has(assertionId);

      // Handle null/empty conditions and empty expected values
      if (NULL_EMPTY_CONDITIONS.includes(processedAssertion.assertionCondition) ||
          processedAssertion.expected === '' ||
          processedAssertion.expected === undefined) {
        processedAssertion.expected = null;
      }

      // Handle extraction data
      if (!extractValueSet.has(assertionId)) {
        processedAssertion.extraction = null;
      } else {
        for (const key in processedAssertion.extraction) {
          if (processedAssertion.extraction[key] === '' || processedAssertion.extraction[key] === undefined) {
            processedAssertion.extraction[key] = null;
          }
        }
      }

      // Handle condition and extract variables
      if (processedAssertion.condition === '' || processedAssertion.condition === undefined) {
        processedAssertion.condition = null;
      } else {
        const conditionParts = expressionUtils.split(processedAssertion.condition);
        if (conditionParts?.length) {
          variableNameSet.add(conditionParts[0]);
        }
      }

      // Handle parameter name
      if (processedAssertion.parameterName === '' || processedAssertion.parameterName === undefined) {
        processedAssertion.parameterName = null;
      }

      // Handle description
      if (processedAssertion.description === '' || processedAssertion.description === undefined) {
        processedAssertion.description = null;
      }

      processedAssertions.push(processedAssertion);
    }
  }

  return { data: processedAssertions, variables: Array.from(variableNameSet) };
};

// Layout control for responsive design
const shouldWrapFormItems = ref(false);

/**
 * Reset form layout based on container width
 */
const resetFormLayout = (): void => {
  const containerWidth = assertionWrapperRef.value?.clientWidth;
  if (containerWidth > 1000) {
    shouldWrapFormItems.value = false;
  } else {
    shouldWrapFormItems.value = true;
  }
};

// Expose component methods
defineExpose({
  getData: getProcessedAssertionData,
  validate: validateAllAssertions,
  addItem: () => {
    const assertionId = utils.uuid('a');
    assertionIdList.value.push(assertionId);
  }
});

onMounted(() => {
  // Load assertion condition options
  loadAssertionConditionOptions();

  // Load extraction location options
  loadExtractionLocationOptions();

  // Watch for props value changes
  watch(() => props.value, async (newValue) => {
    resetComponentState();
    if (newValue?.length) {
      const assertionList = JSON.parse(JSON.stringify(newValue));
      for (let i = 0, len = assertionList.length; i < len; i++) {
        const assertionId = utils.uuid('a');
        const assertionData = assertionList[i] as FormItem;
        const extractionData = assertionData.extraction;
        const assertionCondition = assertionData.assertionCondition?.value || assertionData.assertionCondition;

        assertionIdList.value.push(assertionId);
        expandedAssertionMap.value[assertionId] = true;
        
        assertionDataMap.value[assertionId] = {
          assertionCondition,
          condition: assertionData.condition,
          description: assertionData.description,
          expression: assertionData.expression,
          matchItem: assertionData.matchItem,
          enabled: assertionData.enabled,
          expected: assertionData.expected,
          extraction: assertionData.extraction || generateDefaultExtraction(),
          parameterName: assertionData.parameterName,
          name: assertionData.name,
          type: assertionData.type?.value || assertionData.type
        };

        previouslyCheckedAssertionSet.value.add(assertionId);

        if (assertionList[i].enabled) {
          checkedAssertionSet.value.add(assertionId);
        }

        if (extractionData) {
          extractValueAssertionSet.value.add(assertionId);
        }

        // Set expected value display based on assertion condition
        if (!NULL_EMPTY_CONDITIONS.includes(assertionCondition)) {
          showExpectedValueSet.value.add(assertionId);
          if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
            extractValueAssertionSet.value.delete(assertionId);
            showExpressionSet.value.add(assertionId);
          }
        }

        // Disable extraction for specific conditions
        if (['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL', 'REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
          extractDisabledAssertionSet.value.add(assertionId);
        }
      }
    }

    addEmptyAssertionRecord();
  }, { immediate: true });

  // Watch for assertion count changes
  watch(() => assertionIdList.value, (newValue) => {
    emit('update:num', newValue.length - 1);
  }, { immediate: true, deep: true });

  // Listen for container resize events
  elementResizeDetectorInstance.listenTo(assertionWrapperRef.value, resetFormLayout);

  nextTick(() => {
    emit('rendered', true);
  });
});

// Computed styles for expandable assertion sections
const assertionSectionStyleMap = computed(() => {
  return Object.entries(expandedAssertionMap.value).reduce((styleMap, [assertionId, isExpanded]) => {
    if (isExpanded) {
      styleMap[assertionId] = {
        height: 'auto',
        marginTop: '8px',
        opacity: '100'
      };
    } else {
      styleMap[assertionId] = {
        height: '0',
        marginTop: '0',
        opacity: '0'
      };
    }

    return styleMap;
  }, {} as { [key: string]: { height: string; marginTop: string; opacity: string; } });
});

// Computed expression placeholders based on assertion condition type
const assertionExpressionPlaceholderMap = computed(() => {
  const assertionIds = assertionIdList.value;
  const assertionData = assertionDataMap.value;
  const placeholderMap: { [key: string]: string } = {};

  for (let i = 0, len = assertionIds.length; i < len; i++) {
    const assertionId = assertionIds[i];
    switch (assertionData[assertionId].assertionCondition) {
      case 'REG_MATCH':
        placeholderMap[assertionId] = t('xcan_apiAssert.regexMatchExpression');
        break;
      case 'XPATH_MATCH':
        placeholderMap[assertionId] = t('xcan_apiAssert.xpathMatchExpression');
        break;
      case 'JSON_PATH_MATCH':
        placeholderMap[assertionId] = t('xcan_apiAssert.jsonpathMatchExpression');
        break;
      default:
        placeholderMap[assertionId] = t('xcan_apiAssert.regexMatchExpression');
    }
  }

  return placeholderMap;
});

// Constants for UI configuration
const TEXTAREA_AUTO_SIZE_CONFIG = { minRows: 1, maxRows: 5 };
const ENUM_FIELD_NAMES_CONFIG = { label: 'message', value: 'value' };
</script>
<template>
  <div ref="assertionWrapperRef" class="space-y-3 pb-3">
    <div
      v-for="(item, index) in assertionIdList"
      :key="item"
      :class="{ 'not-enabled': !checkedAssertionSet.has(item) && previouslyCheckedAssertionSet.has(item) && !focusedAssertionSet.has(item) }"
      class="flex items-start">
      <div class="flex items-center h-7 mr-2">
        <Checkbox
          :disabled="!assertionDataMap[item].name?.length || props.viewType"
          :checked="checkedAssertionSet.has(item)"
          @change="handleAssertionCheckboxChange($event, item)" />
      </div>
      <div class="flex-1 mr-3">
        <div v-if="!shouldWrapFormItems" class="flex items-start space-x-2">
          <Validate style="flex: 0 0 calc((100% - 40px)/6*2 + 8px);" :text="assertionNameErrorMsgMap[item]">
            <Input
              :ref="el => { assertionNameRefs[index] = el }"
              v-model:value="assertionDataMap[item].name"
              :placeholder="t('xcan_apiAssert.assertionName')"
              :title="t('xcan_apiAssert.assertionName')"
              :maxlength="200"
              :readonly="props.viewType"
              :error="assertionNameErrorSet.has(item)"
              @focus="handleInputFocus(item)"
              @blur="handleAssertionNameBlur($event, item)"
              @pressEnter="handleAssertionNameEnterPress(index)"
              @change="handleAssertionNameChange(item)" />
          </Validate>
          <Validate style="flex: 0 0 calc((100% - 40px)/6);" :text="assertionConditionErrorMsgMap[item]">
            <div class="flex items-center space-x-2">
              <Input
                :ref="el => { assertionConditionRefs[index] = el }"
                v-model:value="assertionDataMap[item].condition"
                :placeholder="t('xcan_apiAssert.executionConditionExpression')"
                :title="t('xcan_apiAssert.executionConditionExpression')"
                class="flex-1"
                :readonly="!props.id || props.viewType"
                :error="assertionConditionErrorSet.has(item)"
                @focus="handleInputFocus(item)"
                @blur="handleAssertionConditionBlur($event, item)"
                @change="handleAssertionConditionChange(item)"
                @pressEnter="handleAssertionConditionEnterPress(index)" />
              <ConditionPopover />
            </div>
          </Validate>
          <SelectEnum
            v-model:value="assertionDataMap[item].type"
            style="flex: 0 0 calc((100% - 40px)/6);"
            :enumKey="AssertionType"
            :placeholder="t('xcan_apiAssert.assertionType')"
            :title="t('xcan_apiAssert.assertionType')"
            :readonly="props.viewType"
            :error="assertionTypeErrorSet.has(item)"
            @change="handleAssertionTypeChange($event, item)" />
          <Input
            v-model:value="assertionDataMap[item].parameterName"
            :disabled="assertionDataMap[item].type !== 'HEADER'"
            :readonly="props.viewType"
            :error="headerNameErrorSet.has(item)"
            :maxlength="400"
            style="flex: 0 0 calc((100% - 40px)/6);"
            :placeholder="t('xcan_apiAssert.responseHeaderName')"
            title="t('xcan_apiAssert.responseHeaderName')"
            @focus="handleInputFocus(item)"
            @blur="handleInputBlur(item)"
            @change="handleHeaderNameChange(item)" />
          <div class="flex items-center flex-nowrap space-x-2" style="flex: 1;min-width: 100px;">
            <Select
              v-model:value="assertionDataMap[item].assertionCondition"
              :placeholder="t('xcan_apiAssert.assertionCondition')"
              :title="t('xcan_apiAssert.assertionCondition')"
              class="flex-1 min-w-0"
              :disabled="!assertionDataMap[item].type"
              :readonly="props.viewType"
              :options="assertionConditionOptionsMap[item]"
              :fieldNames="ENUM_FIELD_NAMES_CONFIG"
              :error="assertionConditionValidationErrorSet.has(item)"
              @change="handleAssertionConditionSelectionChange($event, item)" />
            <Switch
              class="flex-shrink-0 flex-grow-0"
              :disabled="extractDisabledAssertionSet.has(item) || props.viewType"
              :checked="extractValueAssertionSet.has(item)"
              :checkedChildren="t('xcan_apiAssert.extractExpectedValue')"
              :unCheckedChildren="t('xcan_apiAssert.expectedValue')"
              @change="handleExtractValueSwitchChange($event, item)" />
          </div>
        </div>
        <template v-else>
          <div class="flex space-x-2">
            <Validate style="flex: 0 0 calc((100% - 16px)/3);" :text="assertionNameErrorMsgMap[item]">
              <Input
                :ref="el => { assertionNameRefs[index] = el }"
                v-model:value="assertionDataMap[item].name"
                :placeholder="t('xcan_apiAssert.assertionName')"
                :title="t('xcan_apiAssert.assertionName')"
                :maxlength="200"
                :readonly="props.viewType"
                :error="assertionNameErrorSet.has(item)"
                @focus="handleInputFocus(item)"
                @blur="handleAssertionNameBlur($event, item)"
                @pressEnter="handleAssertionNameEnterPress(index)"
                @change="handleAssertionNameChange(item)" />
            </Validate>
            <Validate style="flex: 0 0 calc((100% - 16px)/3);" :text="assertionConditionErrorMsgMap[item]">
              <div class="flex items-center space-x-2">
                <Input
                  :ref="el => { assertionConditionRefs[index] = el }"
                  v-model:value="assertionDataMap[item].condition"
                  :placeholder="t('xcan_apiAssert.executionConditionExpression')"
                  :title="t('xcan_apiAssert.executionConditionExpression')"
                  class="flex-1"
                  :readonly="!props.id || props.viewType"
                  :error="assertionConditionErrorSet.has(item)"
                  @focus="handleInputFocus(item)"
                  @blur="handleAssertionConditionBlur($event, item)"
                  @change="handleAssertionConditionChange(item)"
                  @pressEnter="handleAssertionConditionEnterPress(index)" />
                <ConditionPopover />
              </div>
            </Validate>
            <div class="flex-1 inline-flex justify-end items-center">
              <Switch
                class="flex-shrink-0 flex-grow-0"
                :disabled="extractDisabledAssertionSet.has(item) || props.viewType"
                :checked="extractValueAssertionSet.has(item)"
                :checkedChildren="t('xcan_apiAssert.extractExpectedValue')"
                :unCheckedChildren="t('xcan_apiAssert.expectedValue')"
                @change="handleExtractValueSwitchChange($event, item)" />
            </div>
          </div>
          <div class="mt-2 flex space-x-2">
            <SelectEnum
              v-model:value="assertionDataMap[item].type"
              style="flex: 0 0 calc((100% - 16px)/3);"
              :enumKey="AssertionType"
              :placeholder="t('xcan_apiAssert.assertionType')"
              :title="t('xcan_apiAssert.assertionType')"
              :readonly="props.viewType"
              :error="assertionTypeErrorSet.has(item)"
              @change="handleAssertionTypeChange($event, item)" />
            <Input
              v-model:value="assertionDataMap[item].parameterName"
              :disabled="assertionDataMap[item].type !== 'HEADER'"
              :readonly="props.viewType"
              :error="headerNameErrorSet.has(item)"
              :maxlength="400"
              style="flex: 0 0 calc((100% - 16px)/3);"
              :placeholder="t('xcan_apiAssert.responseHeaderName')"
              :title="t('xcan_apiAssert.responseHeaderName')"
              @focus="handleInputFocus(item)"
              @blur="handleInputBlur(item)"
              @change="handleHeaderNameChange(item)" />
            <Select
              v-model:value="assertionDataMap[item].assertionCondition"
              :placeholder="t('xcan_apiAssert.assertionCondition')"
              :title="t('xcan_apiAssert.assertionCondition')"
              style="flex: 0 0 calc((100% - 16px)/3);"
              class="min-w-0"
              :disabled="!assertionDataMap[item].type"
              :readonly="props.viewType"
              :options="assertionConditionOptionsMap[item]"
              :fieldNames="ENUM_FIELD_NAMES_CONFIG"
              :error="assertionConditionValidationErrorSet.has(item)"
              @change="handleAssertionConditionSelectionChange($event, item)" />
          </div>
        </template>
        <div :style="assertionSectionStyleMap[item]" class="transition-all space-y-2 overflow-hidden">
          <template v-if="showExpectedValueSet.has(item)">
            <template v-if="extractValueAssertionSet.has(item)">
              <div class="flex items-start space-x-2">
                <SelectEnum
                  v-model:value="assertionDataMap[item].extraction.method"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :enumKey="ExtractionMethod"
                  :placeholder="t('xcan_apiAssert.extractionMethod')"
                  :title="t('xcan_apiAssert.extractionMethod')"
                  :readonly="props.viewType"
                  :error="extractionMethodErrorSet.has(item)"
                  @change="handleExtractionMethodChange(item)" />
                <Select
                  v-model:value="assertionDataMap[item].extraction.location"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :placeholder="t('xcan_apiAssert.extractionLocation')"
                  :title="t('xcan_apiAssert.extractionLocation')"
                  :readonly="props.viewType"
                  :fieldNames="ENUM_FIELD_NAMES_CONFIG"
                  :options="extractionLocationOptionsMap[item]"
                  :error="extractionLocationErrorSet.has(item)"
                  @change="handleExtractionLocationChange(item)" />
                <Input
                  v-model:value="assertionDataMap[item].extraction.parameterName"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :placeholder="t('xcan_apiAssert.extractionParameterName')"
                  :title="t('xcan_apiAssert.extractionParameterName')"
                  :maxlength="400"
                  :disabled="EXTRACTION_LOCATIONS_WITHOUT_PARAMETER_NAME.includes(assertionDataMap[item].extraction.location as any)"
                  :readonly="props.viewType"
                  :error="extractionParameterNameErrorSet.has(item)"
                  @focus="handleInputFocus(item)"
                  @blur="handleInputBlur(item)"
                  @change="handleExtractionParameterNameChange(item)" />
                <Input
                  v-model:value="assertionDataMap[item].extraction.defaultValue"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :placeholder="t('xcan_apiAssert.extractionDefaultValue')"
                  :title="t('xcan_apiAssert.extractionDefaultValue')"
                  :readonly="props.viewType"
                  @focus="handleInputFocus(item)"
                  @blur="handleInputBlur(item)" />
                <Validate style="flex: 0 0 calc((100% - 40px)/6);" :text="extractionExpressionErrorMsgMap[item]">
                  <Input
                    :ref="el => { extractionExpressionRefs[index] = el }"
                    v-model:value="assertionDataMap[item].extraction.expression"
                    :placeholder="t('xcan_apiAssert.extractionExpression')"
                    :title="t('xcan_apiAssert.extractionExpression')"
                    :disabled="assertionDataMap[item].extraction.method === 'EXACT_VALUE'"
                    :readonly="props.viewType"
                    :error="extractionExpressionErrorSet.has(item)"
                    @focus="handleInputFocus(item)"
                    @blur="handleExtractionExpressionBlur($event, item)"
                    @change="handleExtractionExpressionChange(item)"
                    @pressEnter="handleExtractionExpressionEnterPress(index)" />
                </Validate>
                <div class="flex items-center space-x-2" style="flex: 0 0 calc((100% - 40px)/6);">
                  <Input
                    v-model:value="assertionDataMap[item].extraction.matchItem"
                    :placeholder="t('xcan_apiAssert.matchItemRange')"
                    :title="t('xcan_apiAssert.matchItem')"
                    :disabled="assertionDataMap[item].extraction.method === 'EXACT_VALUE'"
                    :readonly="props.viewType"
                    :max="2000"
                    :maxlength="4"
                    dataType="number"
                    @focus="handleInputFocus(item)"
                    @blur="handleInputBlur(item)" />
                  <MatchItemPopover />
                </div>
              </div>
            </template>
            <template v-else>
              <div class="flex items-start space-x-2">
                <template v-if="showExpressionSet.has(item)">
                  <Validate style="flex: 0 0 calc((100% - 40px)/6*2 + 8px);" :text="assertionExpressionErrorMsgMap[item]">
                    <Input
                      v-model:value="assertionDataMap[item].expression"
                      :placeholder="assertionExpressionPlaceholderMap[item]"
                      :title="assertionExpressionPlaceholderMap[item]"
                      :error="assertionExpressionErrorSet.has(item)"
                      :maxlength="400"
                      :readonly="props.viewType"
                      @focus="handleInputFocus(item)"
                      @blur="handleInputBlur(item)"
                      @change="handleAssertionExpressionChange(item)" />
                  </Validate>

                  <Input
                    v-model:value="assertionDataMap[item].matchItem"
                    :placeholder="t('xcan_apiAssert.matchItemRange')"
                    :title="t('xcan_apiAssert.matchItem')"
                    style="flex: 0 0 calc((100% - 40px)/6);"
                    :max="2000"
                    :maxlength="4"
                    :readonly="props.viewType"
                    dataType="number"
                    @focus="handleInputFocus(item)"
                    @blur="handleInputBlur(item)" />

                  <div class="flex items-center space-x-2" style="flex: 1;">
                    <Input
                      v-model:value="assertionDataMap[item].expected"
                      placeholder="t('xcan_apiAssert.expectedValuePlaceholder')"
                      title="t('xcan_apiAssert.expectedValueTitle')"
                      :readonly="props.viewType"
                      :error="expectedValueErrorSet.has(item)"
                      @focus="handleInputFocus(item)"
                      @blur="handleInputBlur(item)"
                      @change="handleExpectedValueChange(item)" />
                    <ExpectedPopover class="flex-shrink-0" />
                  </div>
                </template>
                <template v-else>
                  <Input
                    v-model:value="assertionDataMap[item].expected"
                    type="textarea"
                    :placeholder="t('xcan_apiAssert.expectedValue')"
                    :title="t('xcan_apiAssert.expectedValue')"

                    :readonly="props.viewType"
                    :autoSize="TEXTAREA_AUTO_SIZE_CONFIG"
                    :error="expectedValueErrorSet.has(item)"
                    @focus="handleInputFocus(item)"
                    @blur="handleInputBlur(item)"
                    @change="handleExpectedValueChange(item)" />
                </template>
              </div>
            </template>
          </template>
          <Input
            v-model:value="assertionDataMap[item].description"
            :placeholder="t('common.description')"
            :title="t('common.description')"

            type="textarea"
            :readonly="props.viewType"
            :autoSize="TEXTAREA_AUTO_SIZE_CONFIG"
            @focus="handleInputFocus(item)"
            @blur="handleInputBlur(item)" />
        </div>
      </div>
      <div class="flex items-center h-7 space-x-2">
        <Icon
          v-show="!props.viewType"
          class="cursor-pointer text-theme-sub-content hover:text-text-link-hover"
          icon="icon-shanchuguanbi"
          @click="handleAssertionDelete(index, item)" />
        <Arrow v-model:open="expandedAssertionMap[item]" class="hover:text-text-link-hover" />
      </div>
    </div>
  </div>
</template>
<style scoped>
.not-enabled {
  opacity: 0.5;
}
</style>
