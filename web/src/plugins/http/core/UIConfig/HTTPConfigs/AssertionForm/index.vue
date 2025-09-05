<script setup lang="ts">
import { computed, onMounted, ref, watch, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox, Switch } from 'ant-design-vue';
import { Arrow, Icon, Input, SelectEnum, Select, Tooltip } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';

import MatchItemPopover from './MacthItemPopover.vue';
import ExpectedPopover from './ExpectedPopover.vue';
import ConditionPopover from './ConditionPopover.vue';
import expressionUtils from './utils/expression';
import jsonpath from './utils/jsonpath';
import xpath from './utils/xpath';
import regexp from './utils/regexp';
import { Extraction } from './utils/extract/PropsType';
import { FormItem } from './PropsType';
import { AssertionCondition, AssertionType, EnumMessage, ExtractionMethod, HttpExtractionLocation, utils, duration, enumUtils } from '@xcan-angus/infra';

const { t } = useI18n();

interface Props {
  defaultValue: FormItem[];
  errorNum: number;
  enabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  defaultValue: undefined,
  errorNum: 0,
  enabled: true
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: FormItem[]): void;
  (e: 'update:errorNum', value: number): void;
}>();

const nameRefs = ref<any[]>([]);
const conditionRefs = ref<any[]>([]);
const extractionExpressionRefs = ref<any[]>([]);

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: FormItem }>({});// 数据集合

const openMap = ref<{ [key: string]: boolean }>({});

const hadCheckedSet = ref<Set<string>>(new Set());// 已经勾选过的断言，用于控制在输入名称时，不会自动勾选该条断言
const checkedSet = ref<Set<string>>(new Set());
const extractSet = ref<Set<string>>(new Set());
const extractDisabledSet = ref<Set<string>>(new Set());// 禁止选择提取值集合

const repeatNameSet = ref<Set<string>>(new Set());
const nameErrorSet = ref<Set<string>>(new Set());
const conditionErrorSet = ref<Set<string>>(new Set());
const typeErrorSet = ref<Set<string>>(new Set());
const headerNameeErrorSet = ref<Set<string>>(new Set());
const assertionConditionErrorSet = ref<Set<string>>(new Set());
const expectedErrorSet = ref<Set<string>>(new Set());

const extractionExpressionErrorSet = ref<Set<string>>(new Set());

const locationErrorSet = ref<Set<string>>(new Set());
const methodErrorSet = ref<Set<string>>(new Set());
const parameterNameErrorSet = ref<Set<string>>(new Set());

// 不允许输入提取参数名称
const NOT_PARAMETER_NAME: readonly ['REQUEST_RAW_BODY', 'RESPONSE_BODY'] = ['REQUEST_RAW_BODY', 'RESPONSE_BODY'];

// 是否显示期望值或提取值的输入框，只有断言条件不是['为空','不为空','为null','不为null']才显示
const showExpectedSet = ref<Set<string>>(new Set());
const NOT_SHOW_CONDITION: readonly Partial<AssertionCondition>[] = ['IS_NULL', 'IS_EMPTY', 'NOT_EMPTY', 'NOT_NULL'];

// 只有断言条件为正则表达式、xpath表达式、jsonpath表达式才显示
const expressionShowSet = ref<Set<string>>(new Set());
const expressionErrorSet = ref<Set<string>>(new Set());

// 断言条件枚举
const assertionConditionOptions = ref<EnumMessage<AssertionCondition>[]>([]);
const loadAssertionConditionOptions = () => {
  assertionConditionOptions.value = enumUtils.enumToMessages(AssertionCondition);
};

const NUMBER_CONDITION = ['EQUAL', 'NOT_EQUAL', 'GREATER_THAN', 'GREATER_THAN_EQUAL', 'LESS_THAN', 'LESS_THAN_EQUAL'];
const optionsMap = computed(() => {
  const map = dataMap.value;
  const options = assertionConditionOptions.value;
  return idList.value.reduce((prev, cur) => {
    const type = map[cur].type;
    if (type) {
      if (type === 'BODY' || type === 'HEADER') {
        prev[cur] = options;
      } else {
        prev[cur] = options.filter((item) => NUMBER_CONDITION.includes(item.value));
      }
    } else {
      prev[cur] = options;
    }

    return prev;
  }, {}) || {};
});

// 提取位置枚举
const locationOptions = ref<EnumMessage<HttpExtractionLocation>[]>([]);
const loadLocationOptions = () => {
  locationOptions.value = enumUtils.enumToMessages(HttpExtractionLocation);
};

const locationOptionsMap = computed(() => {
  const map = dataMap.value;
  const options = locationOptions.value;
  return idList.value.reduce((prev, cur) => {
    const method = map[cur].extraction?.method;
    if (method && method === 'EXACT_VALUE') {
      prev[cur] = options.filter((item) => !['RESPONSE_BODY', 'RESPONSE_HEADER', 'REQUEST_RAW_BODY'].includes(item.value));
    } else {
      prev[cur] = options;
    }
    return prev;
  }, {}) || {};
});

const emitChange = () => {
  const data = getData();
  emit('change', data);
};

const checkboxChange = (event: {target:{checked:boolean;}}, id: string) => {
  hadCheckedSet.value.add(id);
  const checked = event.target.checked;
  if (checked) {
    checkedSet.value.add(id);
  } else {
    checkedSet.value.delete(id);
  }

  emitChange();
};

const switchChange = async (checked: boolean | number | string, id: string) => {
  expectedErrorSet.value.delete(id);
  parameterNameErrorSet.value.delete(id);
  locationErrorSet.value.delete(id);
  methodErrorSet.value.delete(id);
  expectedErrorSet.value.delete(id);
  dataMap.value[id].expected = '';
  if (checked) {
    extractSet.value.add(id);
  } else {
    extractSet.value.delete(id);
  }

  emitChange();
};

const nameChange = debounce(duration.delay, (event: { target: { value: string; } }, id: string, index: number) => {
  const value = event.target.value;
  dataMap.value[id].name = value;
  nameErrorSet.value.delete(id);
  if (!hadCheckedSet.value.has(id)) {
    checkedSet.value.add(id);
    hadCheckedSet.value.add(id);
  }

  // 校验名称是否重复
  const duplicates = [];
  const uniqueNames = new Set();
  const names = Object.values(dataMap.value).map(item => item.name).filter(item => item);
  for (let i = 0, len = names.length; i < len; i++) {
    const name = names[i];
    if (uniqueNames.has(name)) {
      duplicates.push(name);
    } else {
      uniqueNames.add(name);
    }
  }

  const ids = idList.value;
  const data = dataMap.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const _id = ids[i];
    if (duplicates.includes(data[_id].name)) {
      nameErrorSet.value.add(_id);
      repeatNameSet.value.add(_id);
    } else {
      nameErrorSet.value.delete(_id);
      repeatNameSet.value.delete(_id);
    }
  }

  if (index === idList.value.length - 1 && event.target.value) {
    addEmptyRecord();
  }

  emitChange();
});

const conditionChange = (id: string) => {
  conditionErrorSet.value.delete(id);
  emitChange();
};

// 非必选项有值才校验
const conditionBlur = async (event: { target: { value: string } }, id: string) => {
  const value = event.target.value;
  dataMap.value[id].condition = value;
  if (!value) {
    return;
  }

  const matchs = expressionUtils.split(value);
  if (!matchs) {
    conditionErrorSet.value.add(id);
  }
};

const typeChange = (value: AssertionType, id: string) => {
  typeErrorSet.value.delete(id);
  dataMap.value[id].type = value;
  if (value !== 'HEADER') {
    dataMap.value[id].parameterName = '';
    headerNameeErrorSet.value.delete(id);
  }

  const _condition = dataMap.value[id].assertionCondition || '';
  // 如果断言类型是
  if (['STATUS', 'BODY_SIZE', 'SIZE', 'DURATION'].includes(value)) {
    if (expressionShowSet.value.has(id)) {
      expressionShowSet.value.delete(id);
      dataMap.value[id].expression = undefined;
      dataMap.value[id].matchItem = undefined;
      dataMap.value[id].expected = undefined;
    }

    if (!NUMBER_CONDITION.includes(_condition)) {
      dataMap.value[id].assertionCondition = 'EQUAL';
    }

    // 禁止选择提取值
    extractSet.value.delete(id);
    extractDisabledSet.value.add(id);
  } else {
    if (!['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(_condition)) {
      extractDisabledSet.value.delete(id);
    }
  }

  emitChange();
};

const headerNameChange = (id: string) => {
  headerNameeErrorSet.value.delete(id);
  emitChange();
};

const assertionConditionChange = (value: typeof NOT_SHOW_CONDITION[number], id: string) => {
  assertionConditionErrorSet.value.delete(id);
  expressionShowSet.value.delete(id);
  showExpectedSet.value.add(id);
  dataMap.value[id].assertionCondition = value;

  if (!['STATUS', 'BODY_SIZE', 'SIZE', 'DURATION'].includes(dataMap.value[id].type)) {
    if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(value)) {
      extractSet.value.delete(id);
      extractDisabledSet.value.add(id);
      expressionShowSet.value.add(id);
    } else if (NOT_SHOW_CONDITION.includes(value)) {
      extractDisabledSet.value.add(id);
      showExpectedSet.value.delete(id);
    } else {
      extractDisabledSet.value.delete(id);
    }
  }

  emitChange();
};

const expressionChange = (id: string) => {
  expressionErrorSet.value.delete(id);
  emitChange();
};

const expressionMatchItemChange = () => {
  emitChange();
};

const methodChange = (value:ExtractionMethod, id: string) => {
  dataMap.value[id].extraction.method = value;
  methodErrorSet.value.delete(id);
  if (dataMap.value[id].extraction.method === 'EXACT_VALUE') {
    dataMap.value[id].extraction.expression = '';
    extractionExpressionErrorSet.value.delete(id);
    const location = dataMap.value[id].extraction.location;
    if (location && ['RESPONSE_BODY', 'RESPONSE_HEADER'].includes(location)) {
      dataMap.value[id].extraction.location = undefined;
    }
  } else {
    const expression = dataMap.value[id].extraction.expression;
    if (expression) {
      validateExtractionExpression(expression, id);
    }
  }

  emitChange();
};

const locationChange = (value:string, id: string) => {
  dataMap.value[id].extraction.location = value;
  locationErrorSet.value.delete(id);
  const _location = dataMap.value[id].extraction.location || '';
  if (!_location) {
    parameterNameErrorSet.value.delete(id);
  }

  if (['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(_location)) {
    dataMap.value[id].extraction.parameterName = undefined;
    parameterNameErrorSet.value.delete(id);
  }

  emitChange();
};

const parameterNameChange = (event: { target: { value: string } }, id: string) => {
  const value = event.target.value;
  dataMap.value[id].extraction.parameterName = value;
  parameterNameErrorSet.value.delete(id);
  emitChange();
};

const defaultValueChange = (event: { target: { value: string } }, id: string) => {
  const value = event.target.value;
  dataMap.value[id].extraction.defaultValue = value;
  emitChange();
};

const extractionExpressionChange = (event: { target: { value: string } }, id: string) => {
  const value = event.target.value;
  dataMap.value[id].extraction.expression = value;
  extractionExpressionErrorSet.value.delete(id);
  emitChange();
};

const expressionBlur = (event: { target: { value: string } }, id: string) => {
  const value = event.target.value;
  if (!value) {
    return;
  }

  validateExtractionExpression(value, id);
};

const matchItemChange = (event: { target: { value: string } }, id: string) => {
  const value = event.target.value;
  dataMap.value[id].extraction.matchItem = value;
  emitChange();
};

const expectedChange = (event:{target:{value:string}}, id: string) => {
  const value = event.target.value;
  dataMap.value[id].expected = value;
  expectedErrorSet.value.delete(id);
  emitChange();
};

const descriptionChange = (event:{target:{value:string}}, id: string) => {
  const value = event.target.value;
  dataMap.value[id].description = value;
  emitChange();
};

const hasEmptyName = (id?: string) => {
  const names = Object.entries(dataMap.value).reduce((prev, [_id, { name }]) => {
    if (id) {
      if (name && id !== _id) {
        prev.push(name);
      }
    } else {
      if (name) {
        prev.push(name);
      }
    }

    return prev;
  }, [] as string[]);

  const len = idList.value.length;
  return names.length < (id ? (len - 1) : len);
};

/**
 * @description 如果删除的是最后一条，则自动添加一条空记录
 * @param index 删除的下标
 * @param id 删除的id
 */
const deleteHandler = (index: number, id: string) => {
  idList.value.splice(index, 1);
  nameRefs.value.splice(index, 1);
  hadCheckedSet.value.delete(id);
  checkedSet.value.delete(id);
  extractSet.value.delete(id);
  extractDisabledSet.value.delete(id);
  delete dataMap.value[id];
  delete openMap.value[id];

  repeatNameSet.value.delete(id);
  nameErrorSet.value.delete(id);
  conditionErrorSet.value.delete(id);
  typeErrorSet.value.delete(id);
  headerNameeErrorSet.value.delete(id);
  assertionConditionErrorSet.value.delete(id);
  expectedErrorSet.value.delete(id);

  extractionExpressionErrorSet.value.delete(id);

  parameterNameErrorSet.value.delete(id);
  locationErrorSet.value.delete(id);
  methodErrorSet.value.delete(id);

  showExpectedSet.value.delete(id);
  expressionShowSet.value.delete(id);

  if (!hasEmptyName()) {
    addEmptyRecord();
  }

  emitChange();
};

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

const generateDefaultData = (): FormItem => {
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

const addEmptyRecord = () => {
  const id = utils.uuid('a');
  idList.value.push(id);
  openMap.value[id] = true;
  dataMap.value[id] = generateDefaultData();
  showExpectedSet.value.add(id);
};

const resetError = () => {
  repeatNameSet.value.clear();
  nameErrorSet.value.clear();
  conditionErrorSet.value.clear();
  typeErrorSet.value.clear();
  headerNameeErrorSet.value.clear();
  assertionConditionErrorSet.value.clear();
  expectedErrorSet.value.clear();
  extractionExpressionErrorSet.value.clear();
  expressionErrorSet.value.clear();
  parameterNameErrorSet.value.clear();
  locationErrorSet.value.clear();
  methodErrorSet.value.clear();
};

const reset = () => {
  idList.value = [];
  nameRefs.value = [];
  hadCheckedSet.value.clear();
  checkedSet.value.clear();
  extractSet.value.clear();
  extractDisabledSet.value.clear();
  showExpectedSet.value.clear();
  expressionShowSet.value.clear();

  dataMap.value = {};
  openMap.value = {};

  resetError();
};

onMounted(() => {
  // 加载断言条件枚举选项
  loadAssertionConditionOptions();

  // 加载提取位置枚举选项
  loadLocationOptions();

  watch(() => props.defaultValue, async (newValue) => {
    reset();
    if (newValue?.length) {
      const list = JSON.parse(JSON.stringify(newValue));
      for (let i = 0, len = list.length; i < len; i++) {
        const id = utils.uuid();
        const data = list[i] as FormItem;
        const extraction = data.extraction;
        const assertionCondition = data.assertionCondition?.value || data.assertionCondition;
        const type = data.type?.value || data.type;
        idList.value.push(id);
        openMap.value[id] = true;
        dataMap.value[id] = {
          type,
          assertionCondition,
          condition: data.condition,
          description: data.description,
          expression: data.expression,
          matchItem: data.matchItem,
          enabled: data.enabled,
          expected: data.expected,
          extraction: data.extraction || generateDefaultExtraction(),
          parameterName: data.parameterName,
          name: data.name
        };

        hadCheckedSet.value.add(id);

        if (list[i].enabled) {
          checkedSet.value.add(id);
        }

        if (extraction) {
          extractSet.value.add(id);
        }

        // @TODO 设置showExpectedSet，只有断言条件不为['为空','不为空','为null','不为null']才显示
        if (!NOT_SHOW_CONDITION.includes(assertionCondition)) {
          showExpectedSet.value.add(id);
          if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
            extractSet.value.delete(id);
            expressionShowSet.value.add(id);
          }
        }

        if (['STATUS', 'BODY_SIZE', 'SIZE', 'DURATION'].includes(type) || ['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL', 'REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
          extractDisabledSet.value.add(id);
        }
      }
    }

    addEmptyRecord();
  }, { immediate: true });

  watchEffect(() => {
    const size = nameErrorSet.value.size +
    typeErrorSet.value.size +
    headerNameeErrorSet.value.size +
    assertionConditionErrorSet.value.size +
    expressionErrorSet.value.size +
    expectedErrorSet.value.size +
    extractionExpressionErrorSet.value.size +
    parameterNameErrorSet.value.size +
    locationErrorSet.value.size +
    methodErrorSet.value.size +
    conditionErrorSet.value.size;
    emit('update:errorNum', size);
  });
});

const validateExtractionExpression = (value: string, id: string) => {
  const method = dataMap.value[id].extraction.method;
  let validResult: { valid: boolean; message: string; } = { valid: false, message: '' };
  if (method === 'JSON_PATH') {
    validResult = jsonpath.isValid(value);
  } else if (method === 'X_PATH') {
    validResult = xpath.isValid(value);
  } else {
    validResult = regexp.isValid(value);
  }

  if (validResult.valid) {
    extractionExpressionErrorSet.value.delete(id);
    return true;
  }

  extractionExpressionErrorSet.value.add(id);
  openMap.value[id] = true;
  return false;
};

const validateExpression = (value: string, id: string) => {
  const method = dataMap.value[id].assertionCondition;
  let validResult: { valid: boolean; message: string; } = { valid: false, message: '' };
  if (method === 'JSON_PATH_MATCH') {
    validResult = jsonpath.isValid(value);
  } else if (method === 'XPATH_MATCH') {
    validResult = xpath.isValid(value);
  } else {
    validResult = regexp.isValid(value);
  }

  if (validResult.valid) {
    expressionErrorSet.value.delete(id);
    return true;
  }

  expressionErrorSet.value.add(id);
  openMap.value[id] = true;
  return false;
};

const isValid = (): boolean => {
  resetError();
  // 校验名称是否重复
  const duplicates = [];
  const uniqueNames = new Set();
  const names = Object.values(dataMap.value).map(item => item.name).filter(item => item);
  for (let i = 0, len = names.length; i < len; i++) {
    const name = names[i];
    if (uniqueNames.has(name)) {
      duplicates.push(name);
    } else {
      uniqueNames.add(name);
    }
  }

  const ids = idList.value;
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    const id = ids[i];
    const {
      name,
      condition,
      type,
      parameterName,
      assertionCondition,
      expression,
      expected,
      extraction
    } = dataMap.value[id];
    if (!name) {
      nameErrorSet.value.add(id);
    } else {
      if (duplicates.includes(name)) {
        nameErrorSet.value.add(id);
        repeatNameSet.value.add(id);
      }
    }

    if (condition && !expressionUtils.isValid(condition)) {
      conditionErrorSet.value.add(id);
    }

    if (!type) {
      typeErrorSet.value.add(id);
    } else {
      // 断言类型是响应头时，响应头名称必须
      if (type === 'HEADER') {
        if (!parameterName) {
          headerNameeErrorSet.value.add(id);
        }
      }
    }

    if (!assertionCondition) {
      assertionConditionErrorSet.value.add(id);
    }

    if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition!)) {
      if (!expression) {
        expressionErrorSet.value.add(id);
        openMap.value[id] = true;
      } else {
        validateExpression(expression!, id);
      }
    } else if (showExpectedSet.value.has(id)) {
      // 期望值
      if (!extractSet.value.has(id)) {
        if (!expected) {
          expectedErrorSet.value.add(id);
          openMap.value[id] = true;
        }
      } else { // 提取期望值
        if (extraction.method !== 'EXACT_VALUE') {
          if (!extraction.expression) {
            extractionExpressionErrorSet.value.add(id);
            openMap.value[id] = true;
          } else {
            validateExtractionExpression(extraction.expression!, id);
          }
        }

        // raw请求体和响应体不用校验
        if (!['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(extraction.location!)) {
          if (!extraction.parameterName) {
            parameterNameErrorSet.value.add(id);
            openMap.value[id] = true;
          }
        }

        if (!extraction.location) {
          locationErrorSet.value.add(id);
          openMap.value[id] = true;
        }

        if (!extraction.method) {
          methodErrorSet.value.add(id);
          openMap.value[id] = true;
        }
      }
    }
  }

  return !(nameErrorSet.value.size +
    typeErrorSet.value.size +
    headerNameeErrorSet.value.size +
    assertionConditionErrorSet.value.size +
    expressionErrorSet.value.size +
    expectedErrorSet.value.size +
    extractionExpressionErrorSet.value.size +
    parameterNameErrorSet.value.size +
    locationErrorSet.value.size +
    methodErrorSet.value.size +
    conditionErrorSet.value.size);
};

const getData = (): FormItem[] => {
  const ids = idList.value;
  const data = JSON.parse(JSON.stringify(dataMap.value));
  const _extractSet = extractSet.value;
  const _checkedSet = checkedSet.value;
  const result: FormItem[] = [];
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    const id = ids[i];
    const temp = data[id];
    temp.enabled = _checkedSet.has(id);
    if (['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL'].includes(temp.assertionCondition) || temp.expected === '' || temp.expected === undefined) {
      temp.expected = null;
    }

    if (!_extractSet.has(id)) {
      temp.extraction = null;
    }

    result.push(temp);
  }

  return result;
};

defineExpose({
  getData,
  isValid,
  addGuideData: (data:FormItem) => {
    const { name, type, assertionCondition, expected, description } = data;
    const id = idList.value[0];
    const index = 0;
    nameChange({ target: { value: name } }, id, index);
    typeChange(type, id);
    assertionConditionChange(assertionCondition, id);
    assertionConditionChange(assertionCondition, id);
    expectedChange({ target: { value: expected } }, id);
    descriptionChange({ target: { value: description } }, id);
  }
});

const styleMap = computed(() => {
  return Object.entries(openMap.value).reduce((prev, [key, value]) => {
    if (value) {
      prev[key] = {
        height: 'auto',
        marginTop: '8px',
        opacity: '100'
      };
    } else {
      prev[key] = {
        height: '0',
        marginTop: '0',
        opacity: '0'
      };
    }

    return prev;
  }, {});
});
const expressionPlaceholder = computed(() => {
  const ids = idList.value;
  const _dataMap = dataMap.value;
  const map: { [key: string]: string } = {};
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    switch (_dataMap[id].assertionCondition) {
      case 'REG_MATCH':
        map[id] = t('httpPlugin.uiConfig.httpConfigs.assertionForm.expressionPlaceholder.regMatch');
        break;
      case 'XPATH_MATCH':
        map[id] = t('httpPlugin.uiConfig.httpConfigs.assertionForm.expressionPlaceholder.xpathMatch');
        break;
      case 'JSON_PATH_MATCH':
        map[id] = t('httpPlugin.uiConfig.httpConfigs.assertionForm.expressionPlaceholder.jsonpathMatch');
        break;
      default:
        map[id] = t('httpPlugin.uiConfig.httpConfigs.assertionForm.expressionPlaceholder.regMatch');
    }
  }

  return map;
});
const textAreaAutoSize = { minRows: 1, maxRows: 5 };
const enumFieldNames = { label: 'message', value: 'value' };
</script>
<template>
  <div class="space-y-4 pb-3">
    <div
      v-for="(item, index) in idList"
      :key="item"
      :class="{ 'opacity-70': !checkedSet.has(item) && props.enabled }"
      class="flex items-start">
      <div class="flex items-center h-7 mr-2">
        <Checkbox :checked="checkedSet.has(item)" @change="checkboxChange($event, item)" />
      </div>
      <div class="flex-1 mr-3">
        <div class="flex items-start space-x-2">
          <Tooltip
            :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.name.duplicate')"
            internal
            placement="right"
            destroyTooltipOnHide
            :visible="repeatNameSet.has(item)">
            <Input
              :ref="el => { nameRefs[index] = el }"
              v-model:value="dataMap[item].name"
              :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.name.placeholder')"
              :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.name.title')"
              trim
              style="flex: 0 0 calc((100% - 40px)/6*2 + 8px);"
              :maxlength="200"
              :error="nameErrorSet.has(item)"
              @change="nameChange($event, item, index)" />
          </Tooltip>
          <Tooltip
            :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.expression.error')"
            internal
            placement="right"
            destroyTooltipOnHide
            :visible="conditionErrorSet.has(item)">
            <div style="flex: 0 0 calc((100% - 40px)/6);" class="flex items-center space-x-2">
              <Input
                :ref="el => { conditionRefs[index] = el }"
                v-model:value="dataMap[item].condition"
                :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.condition.expression.placeholder')"
                :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.condition.expression.title')"
                class="flex-1"
                trim
                :error="conditionErrorSet.has(item)"
                @blur="conditionBlur($event, item)"
                @change="conditionChange(item)" />
              <ConditionPopover />
            </div>
          </Tooltip>
          <SelectEnum
            v-model:value="dataMap[item].type"
            style="flex: 0 0 calc((100% - 40px)/6);"
            enumKey="AssertionType"
            :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.type.placeholder')"
            :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.type.title')"
            :error="typeErrorSet.has(item)"
            @change="typeChange($event, item)" />
          <Input
            v-model:value="dataMap[item].parameterName"
            :disabled="dataMap[item].type !== 'HEADER'"
            :error="headerNameeErrorSet.has(item)"
            :maxlength="400"
            trim
            style="flex: 0 0 calc((100% - 40px)/6);"
            :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.header.name.placeholder')"
            :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.header.name.title')"
            @change="headerNameChange(item)" />
          <div class="flex items-center flex-nowrap space-x-2" style="flex: 1;min-width: 100px;">
            <Select
              v-model:value="dataMap[item].assertionCondition"
              :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.condition.placeholder')"
              :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.condition.title')"
              class="flex-1 min-w-0"
              :disabled="!dataMap[item].type"
              :options="optionsMap[item]"
              :fieldNames="enumFieldNames"
              :error="assertionConditionErrorSet.has(item)"
              @change="assertionConditionChange($event, item)" />
            <Switch
              :disabled="extractDisabledSet.has(item)"
              :checked="extractSet.has(item)"
              :checkedChildren="t('httpPlugin.uiConfig.httpConfigs.assertionForm.show.expected.checked')"
              :unCheckedChildren="t('httpPlugin.uiConfig.httpConfigs.assertionForm.show.expected.unchecked')"
              @change="switchChange($event, item)" />
          </div>
        </div>
        <div :style="styleMap[item]" class="transition-all space-y-2 overflow-hidden">
          <template v-if="showExpectedSet.has(item)">
            <template v-if="extractSet.has(item)">
              <div class="flex items-start space-x-2">
                <SelectEnum
                  :value="dataMap[item].extraction?.method"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  enumKey="ExtractionMethod"
                  :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.method.placeholder')"
                  :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.method.title')"
                  trim
                  :error="methodErrorSet.has(item)"
                  @change="methodChange($event,item)" />
                <Select
                  :value="dataMap[item].extraction?.location"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.location.placeholder')"
                  :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.location.title')"
                  trim
                  :fieldNames="enumFieldNames"
                  :options="locationOptionsMap[item]"
                  :error="locationErrorSet.has(item)"
                  @change="locationChange($event,item)" />
                <Input
                  :value="dataMap[item].extraction?.parameterName"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.parameter.name.placeholder')"
                  :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.parameter.name.title')"
                  trim
                  :maxlength="400"
                  :disabled="NOT_PARAMETER_NAME.includes(dataMap[item].extraction?.location)"
                  :error="parameterNameErrorSet.has(item)"
                  @change="parameterNameChange($event,item)" />
                <Input
                  :value="dataMap[item].extraction?.defaultValue"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.default.value.placeholder')"
                  :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.default.value.title')"
                  trim
                  @change="defaultValueChange($event,item)" />
                <Tooltip
                  :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.expression.error')"
                  internal
                  placement="right"
                  destroyTooltipOnHide
                  :visible="extractionExpressionErrorSet.has(item)">
                  <Input
                    :ref="el => { extractionExpressionRefs[index] = el }"
                    :value="dataMap[item].extraction?.expression"
                    :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.expression.placeholder')"
                    :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.expression.title')"
                    trim
                    style="flex: 0 0 calc((100% - 40px)/6);"
                    :disabled="dataMap[item].extraction?.method === 'EXACT_VALUE'"
                    :error="extractionExpressionErrorSet.has(item)"
                    @blur="expressionBlur($event, item)"
                    @change="extractionExpressionChange($event,item)" />
                </Tooltip>
                <div class="flex items-center space-x-2" style="flex: 0 0 calc((100% - 40px)/6);">
                  <Input
                    :value="dataMap[item].extraction?.matchItem"
                    :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.match.item.placeholder')"
                    :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.extraction.match.item.title')"
                    trim
                    :disabled="dataMap[item].extraction?.method === 'EXACT_VALUE'"
                    :max="2000"
                    :maxlength="4"
                    dataType="number"
                    @change="matchItemChange($event,item)" />
                  <MatchItemPopover />
                </div>
              </div>
            </template>
            <template v-else>
              <div class="flex items-start space-x-2">
                <template v-if="expressionShowSet.has(item)">
                  <Tooltip
                    :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.expression.error')"
                    internal
                    placement="right"
                    destroyTooltipOnHide
                    :visible="expressionErrorSet.has(item)">
                    <Input
                      v-model:value="dataMap[item].expression"
                      trim
                      style="flex: 0 0 calc((100% - 40px)/6*2 + 8px);"
                      :placeholder="expressionPlaceholder[item]"
                      :title="expressionPlaceholder[item]"
                      :error="expressionErrorSet.has(item)"
                      :maxlength="400"
                      @change="expressionChange(item)" />
                  </Tooltip>
                  <Input
                    v-model:value="dataMap[item].matchItem"
                    :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.match.item.placeholder')"
                    :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.match.item.title')"
                    style="flex: 0 0 calc((100% - 40px)/6);"
                    trim
                    :max="2000"
                    :maxlength="4"
                    dataType="number"
                    @change="expressionMatchItemChange" />
                  <div class="flex items-center space-x-2" style="flex: 1;">
                    <Input
                      v-model:value="dataMap[item].expected"
                      :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.expected.placeholder')"
                      :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.expected.title')"
                      trim
                      :error="expectedErrorSet.has(item)"
                      @change="expectedChange($event,item)" />
                    <ExpectedPopover class="flex-shrink-0" />
                  </div>
                </template>
                <template v-else>
                  <Input
                    v-model:value="dataMap[item].expected"
                    type="textarea"
                    :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.expected.placeholder')"
                    :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.expected.title')"
                    trim
                    :autoSize="textAreaAutoSize"
                    :error="expectedErrorSet.has(item)"
                    @change="expectedChange($event,item)" />
                </template>
              </div>
            </template>
          </template>
          <Input
            v-model:value="dataMap[item].description"
            :placeholder="t('httpPlugin.uiConfig.httpConfigs.assertionForm.description.placeholder')"
            :title="t('httpPlugin.uiConfig.httpConfigs.assertionForm.description.title')"
            type="textarea"
            trim
            @change="descriptionChange($event,item)" />
        </div>
      </div>
      <div class="flex items-center h-7 space-x-2">
        <Icon
          class="cursor-pointer text-theme-sub-content hover:text-text-link-hover"
          icon="icon-shanchuguanbi"
          @click="deleteHandler(index, item)" />
        <Arrow v-model:open="openMap[item]" class="hover:text-text-link-hover" />
      </div>
    </div>
  </div>
</template>
