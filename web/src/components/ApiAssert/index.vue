<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { Checkbox, Switch } from 'ant-design-vue';
import { CheckboxChangeEvent } from 'ant-design-vue/lib/checkbox/interface';
import { Arrow, Icon, Input, SelectEnum, Select, Validate } from '@xcan-angus/vue-ui';
import { EnumMessage, AssertionCondition, AssertionType, HttpExtractionLocation, utils, enumUtils, ExtractionMethod } from '@xcan-angus/infra';
import elementResizeDetector from 'element-resize-detector';
import { useI18n } from 'vue-i18n';
const { t }  = useI18n();

import MatchItemPopover from './MacthItemPopover.vue';
import ExpectedPopover from './ExpectedPopover.vue';
import ConditionPopover from './ConditionPopover.vue';
import expressionUtils from './utils/expression';
import jsonpath from './utils/jsonpath';
import xpath from './utils/xpath';
import regexp from './utils/regexp';
import { Extraction } from './utils/extract/PropsType';
import { FormItem } from './PropsType';

interface Props {
  id: string | undefined;
  value: FormItem[];
  num?: number;
  viewType?: boolean;

}
const erd = elementResizeDetector({ strategy: 'scroll' });

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  value: undefined,
  num: undefined,
  viewType: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:num', num: number): void;
  (e: 'rendered', value: true);
}>();

const assertWrapperRef = ref();
const nameRefs = ref<any[]>([]);
const conditionRefs = ref<any[]>([]);
const extractionExpressionRefs = ref<any[]>([]);

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: FormItem }>({});// 数据集合

const openMap = ref<{ [key: string]: boolean }>({});

const focusSet = ref<Set<string>>(new Set());
const hadCheckedSet = ref<Set<string>>(new Set());// 已经勾选过的断言，用于控制在输入名称时，不会自动勾选该条断言
const checkedSet = ref<Set<string>>(new Set());
const extractSet = ref<Set<string>>(new Set());
const extractDisabledSet = ref<Set<string>>(new Set());// 禁止选择提取值集合

const nameErrorSet = ref<Set<string>>(new Set());
const nameErrorMsgMap = ref<{ [key: string]: string | undefined }>({});
const conditionErrorSet = ref<Set<string>>(new Set());
const conditionErrorMsgMap = ref<{ [key: string]: string | undefined }>({});
const typeErrorSet = ref<Set<string>>(new Set());
const headerNameeErrorSet = ref<Set<string>>(new Set());
const assertionConditionErrorSet = ref<Set<string>>(new Set());
const expectedErrorSet = ref<Set<string>>(new Set());

const extractionExpressionErrorSet = ref<Set<string>>(new Set());
const extractionExpressionErrorMsgMap = ref<{ [key: string]: string }>({});

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
const expressionErrorMsgMap = ref<{ [key: string]: string }>({});

// 断言条件枚举
const assertionConditionOptions = ref<{ message: string; value: string; }[]>([]);
const loadAssertionConditionOptions = async () => {
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
const loadLocationOptions = async () => {
  locationOptions.value = enumUtils.enumToMessages(HttpExtractionLocation);
};

const locationOptionsMap = computed(() => {
  const map = dataMap.value;
  const options = locationOptions.value;
  return idList.value.reduce((prev, cur) => {
    const method = map[cur].extraction.method;
    if (method && method === 'EXACT_VALUE') {
      prev[cur] = options.filter((item) => !['RESPONSE_BODY', 'RESPONSE_HEADER', 'REQUEST_RAW_BODY'].includes(item.value));
    } else {
      prev[cur] = options;
    }

    return prev;
  }, {}) || {};
});

const checkboxChange = (event: CheckboxChangeEvent, id: string) => {
  hadCheckedSet.value.add(id);
  const checked = event.target.checked;
  if (checked) {
    checkedSet.value.add(id);
    return;
  }

  checkedSet.value.delete(id);
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
    return;
  }

  extractSet.value.delete(id);
};

const validateName = (id: string, name: string | undefined) => {
  const names = Object.entries(dataMap.value).reduce((prev, [_id, { name: _name }]) => {
    if (id !== _id && _name) {
      prev.add(_name);
    }

    return prev;
  }, new Set() as Set<string>);

  if (!name) {
    nameErrorSet.value.add(id);
    nameErrorMsgMap.value[id] = t('xcan_apiAssert.nameCannotBeEmpty');
    return;
  }

  if (names.has(name)) {
    nameErrorSet.value.add(id);
    nameErrorMsgMap.value[id] = t('xcan_apiAssert.nameDuplicate');
    return;
  }

  nameErrorSet.value.delete(id);
  nameErrorMsgMap.value[id] = undefined;
};

const inputFocus = (id: string) => {
  focusSet.value.add(id);
};

const inputBlur = (id: string) => {
  focusSet.value.delete(id);
};

const nameBlur = (event: { target: { value: string } }, id: string) => {
  inputBlur(id);

  const value = event.target.value?.trim();
  dataMap.value[id].name = value;

  if (value && !hasEmptyName(id)) {
    addEmptyRecord();
  }

  if (hasContent(id, dataMap.value[id])) {
    validateName(id, value);
  }
};

const namePressEnter = (index: number) => {
  if (!nameRefs.value[index]) {
    return;
  }

  nameRefs.value[index]?.blur?.();
};

const nameChange = (id: string) => {
  if (!hadCheckedSet.value.has(id)) {
    checkedSet.value.add(id);
    hadCheckedSet.value.add(id);
  }

  nameErrorSet.value.delete(id);
  delete nameErrorMsgMap.value[id];
};

const conditionChange = (id: string) => {
  conditionErrorSet.value.delete(id);
  conditionErrorMsgMap.value[id] = undefined;
};

// 非必选项有值才校验
const conditionBlur = async (event: { target: { value: string } }, id: string) => {
  inputBlur(id);

  const value = event.target.value?.trim();
  dataMap.value[id].condition = value;

  if (!value) {
    return;
  }

  const matchs = expressionUtils.split(value);
  if (!matchs) {
    conditionErrorSet.value.add(id);
    conditionErrorMsgMap.value[id] = t('xcan_apiAssert.conditionExpressionFormatError');
  }
};

const conditionPressEnter = (index: number) => {
  if (!conditionRefs.value[index]) {
    return;
  }

  conditionRefs.value[index]?.blur?.();
};

const typeChange = (value: AssertionType, id: string) => {
  typeErrorSet.value.delete(id);

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
    return;
  }

  if (!['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(_condition)) {
    extractDisabledSet.value.delete(id);
  }
};

const headerNameeChange = (id: string) => {
  headerNameeErrorSet.value.delete(id);
};

const assertionConditionChange = (value: typeof NOT_SHOW_CONDITION[number], id: string) => {
  assertionConditionErrorSet.value.delete(id);
  expressionShowSet.value.delete(id);
  showExpectedSet.value.add(id);

  if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(value)) {
    extractSet.value.delete(id);
    extractDisabledSet.value.add(id);
    expressionShowSet.value.add(id);
    return;
  }

  if (NOT_SHOW_CONDITION.includes(value)) {
    extractDisabledSet.value.add(id);
    showExpectedSet.value.delete(id);
    return;
  }

  extractDisabledSet.value.delete(id);
};

const expressionChange = (id: string) => {
  expressionErrorSet.value.delete(id);
  delete expressionErrorMsgMap.value[id];
};

const methodChange = (id: string) => {
  methodErrorSet.value.delete(id);
  if (dataMap.value[id].extraction.method === 'EXACT_VALUE') {
    dataMap.value[id].extraction.expression = '';
    extractionExpressionErrorSet.value.delete(id);
    delete extractionExpressionErrorMsgMap.value[id];

    const location = dataMap.value[id].extraction.location;
    if (location && ['RESPONSE_BODY', 'RESPONSE_HEADER'].includes(location)) {
      dataMap.value[id].extraction.location = undefined;
    }

    return;
  }

  const expression = dataMap.value[id].extraction.expression;
  if (expression) {
    validateExtractionExpression(expression, id);
  }
};

const locationChange = (id: string) => {
  locationErrorSet.value.delete(id);

  const _location = dataMap.value[id].extraction.location || '';
  if (!_location) {
    parameterNameErrorSet.value.delete(id);
  }

  if (['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(_location)) {
    dataMap.value[id].extraction.parameterName = undefined;
    parameterNameErrorSet.value.delete(id);
  }
};

const parameterNameChange = (id: string) => {
  parameterNameErrorSet.value.delete(id);
};

const extractionExpressionChange = (id: string) => {
  extractionExpressionErrorSet.value.delete(id);
  delete extractionExpressionErrorMsgMap.value[id];
};

const expressionBlur = (event: { target: { value: string } }, id: string) => {
  inputBlur(id);

  const value = event.target.value?.trim();
  if (!value) {
    return;
  }

  validateExtractionExpression(value, id);
};

const expressionEnter = (index: number) => {
  if (!extractionExpressionRefs.value[index]) {
    return;
  }

  extractionExpressionRefs.value[index]?.blur?.();
};

const expectedChange = (id: string) => {
  expectedErrorSet.value.delete(id);
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
  focusSet.value.delete(id);
  hadCheckedSet.value.delete(id);
  checkedSet.value.delete(id);
  extractSet.value.delete(id);
  extractDisabledSet.value.delete(id);
  delete dataMap.value[id];
  delete openMap.value[id];

  nameErrorSet.value.delete(id);
  delete nameErrorMsgMap.value[id];
  conditionErrorSet.value.delete(id);
  delete conditionErrorMsgMap.value[id];
  typeErrorSet.value.delete(id);
  headerNameeErrorSet.value.delete(id);
  assertionConditionErrorSet.value.delete(id);
  expectedErrorSet.value.delete(id);

  extractionExpressionErrorSet.value.delete(id);
  delete extractionExpressionErrorMsgMap.value[id];

  parameterNameErrorSet.value.delete(id);
  locationErrorSet.value.delete(id);
  methodErrorSet.value.delete(id);

  showExpectedSet.value.delete(id);
  expressionShowSet.value.delete(id);

  if (!hasEmptyName()) {
    addEmptyRecord();
  }
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
  if (props.viewType) { // 只读状态下，不自动添加
    return;
  }
  const id = utils.uuid('a');
  idList.value.push(id);
  openMap.value[id] = true;
  dataMap.value[id] = generateDefaultData();
  showExpectedSet.value.add(id);
};

const resetError = () => {
  nameErrorSet.value.clear();
  nameErrorMsgMap.value = {};
  conditionErrorSet.value.clear();
  conditionErrorMsgMap.value = {};
  typeErrorSet.value.clear();
  headerNameeErrorSet.value.clear();
  assertionConditionErrorSet.value.clear();
  expectedErrorSet.value.clear();

  extractionExpressionErrorSet.value.clear();
  extractionExpressionErrorMsgMap.value = {};

  expressionErrorSet.value.clear();
  expressionErrorMsgMap.value = {};

  parameterNameErrorSet.value.clear();
  locationErrorSet.value.clear();
  methodErrorSet.value.clear();
};

const reset = () => {
  idList.value = [];
  nameRefs.value = [];
  focusSet.value.clear();
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

const hasContent = (id: string, data: { [key: string]: any }) => {
  const isExtract = extractSet.value.has(id);
  const entries = Object.entries(data);
  for (let i = 0, len = entries.length; i < len; i++) {
    const key = entries[i][0];
    const value = entries[i][1];
    if (key === 'extraction') {
      if (isExtract && hasContent(id, value)) {
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

const validateExtractionExpression = (value: string, id: string) => {
  const method = dataMap.value[id].extraction.method;
  let validResult:{ valid: boolean; message: string; } = { valid: false, message: '' };
  if (method === 'JSON_PATH') {
    validResult = jsonpath.isValid(value);
  } else if (method === 'X_PATH') {
    validResult = xpath.isValid(value);
  } else {
    validResult = regexp.isValid(value);
  }

  if (validResult.valid) {
    extractionExpressionErrorSet.value.delete(id);
    delete extractionExpressionErrorMsgMap.value[id];
    return true;
  }

  extractionExpressionErrorSet.value.add(id);
  extractionExpressionErrorMsgMap.value[id] = validResult.message;
  openMap.value[id] = true;
  return false;
};

const validateExpression = (value: string, id: string) => {
  const method = dataMap.value[id].assertionCondition;
  let validResult:{ valid: boolean; message: string; } = { valid: false, message: '' };
  if (method === 'JSON_PATH_MATCH') {
    validResult = jsonpath.isValid(value);
  } else if (method === 'XPATH_MATCH') {
    validResult = xpath.isValid(value);
  } else {
    validResult = regexp.isValid(value);
  }

  if (validResult.valid) {
    expressionErrorSet.value.delete(id);
    delete expressionErrorMsgMap.value[id];
    return true;
  }

  expressionErrorSet.value.add(id);
  expressionErrorMsgMap.value[id] = validResult.message;
  openMap.value[id] = true;
  return false;
};

/**
 * @description true - 校验通过，false - 校验失败
 */
const toValidate = (): boolean => {
  resetError();
  const ids = idList.value;
  const seens: Set<string> = new Set();
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const currentInfo = dataMap.value[id];
    if (hasContent(id, currentInfo)) {
      if (!currentInfo.name) {
        nameErrorSet.value.add(id);
        nameErrorMsgMap.value[id] = t('xcan_apiAssert.nameCannotBeEmpty');
      } else if (seens.has(currentInfo.name)) {
        nameErrorSet.value.add(id);
        nameErrorMsgMap.value[id] = t('xcan_apiAssert.nameDuplicate');
      } else {
        seens.add(currentInfo.name);
      }

      if (currentInfo.condition && !expressionUtils.isValid(currentInfo.condition)) {
        conditionErrorSet.value.add(id);
        conditionErrorMsgMap.value[id] = t('xcan_apiAssert.conditionExpressionFormatError');
      }

      if (!currentInfo.type) {
        typeErrorSet.value.add(id);
      } else {
        // 断言类型是响应头时，响应头名称必须
        if (currentInfo.type === 'HEADER') {
          if (!currentInfo.parameterName) {
            headerNameeErrorSet.value.add(id);
          }
        }
      }

      if (!currentInfo.assertionCondition) {
        assertionConditionErrorSet.value.add(id);
      }

      if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(currentInfo.assertionCondition!)) {
        if (!currentInfo.expression) {
          expressionErrorSet.value.add(id);
          openMap.value[id] = true;
        } else {
          validateExpression(currentInfo.expression!, id);
        }
      } else if (showExpectedSet.value.has(id)) {
        // 期望值
        if (!extractSet.value.has(id)) {
          if (!currentInfo.expected) {
            expectedErrorSet.value.add(id);
            openMap.value[id] = true;
          }
        } else { // 提取期望值
          if (currentInfo.extraction.method !== 'EXACT_VALUE') {
            if (!currentInfo.extraction.expression) {
              extractionExpressionErrorSet.value.add(id);
              openMap.value[id] = true;
            } else {
              validateExtractionExpression(currentInfo.extraction.expression!, id);
            }
          }

          // raw请求体和响应体不用校验
          if (!['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(currentInfo.extraction.location!)) {
            if (!currentInfo.extraction.parameterName) {
              parameterNameErrorSet.value.add(id);
              openMap.value[id] = true;
            }
          }

          if (!currentInfo.extraction.location) {
            locationErrorSet.value.add(id);
            openMap.value[id] = true;
          }

          if (!currentInfo.extraction.method) {
            methodErrorSet.value.add(id);
            openMap.value[id] = true;
          }
        }
      }
    }
  }

  return !(nameErrorSet.value.size ||
    typeErrorSet.value.size ||
    headerNameeErrorSet.value.size ||
    assertionConditionErrorSet.value.size ||
    expressionErrorSet.value.size ||
    expectedErrorSet.value.size ||
    extractionExpressionErrorSet.value.size ||
    parameterNameErrorSet.value.size ||
    locationErrorSet.value.size ||
    methodErrorSet.value.size
  );
};

const getData = ():{data:FormItem[], variables:string[]} => {
  const ids = idList.value;
  const data = dataMap.value;
  const _extractSet = extractSet.value;
  const _checkedSet = checkedSet.value;
  const result: FormItem[] = [];
  const variableSet = new Set<string>();
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (data[id].name) {
      const temp = JSON.parse(JSON.stringify(data[id]));
      temp.enabled = _checkedSet.has(id);

      if (['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL'].includes(temp.assertionCondition) || temp.expected === '' || temp.expected === undefined) {
        temp.expected = null;
      }

      if (!_extractSet.has(id)) {
        temp.extraction = null;
      } else {
        for (const key in temp.extraction) {
          if (temp.extraction[key] === '' || temp.extraction[key] === undefined) {
            temp.extraction[key] = null;
          }
        }
      }

      if (temp.condition === '' || temp.condition === undefined) {
        temp.condition = null;
      } else {
        const matchs = expressionUtils.split(temp.condition);
        if (matchs?.length) {
          variableSet.add(matchs[0]);
        }
      }

      if (temp.parameterName === '' || temp.parameterName === undefined) {
        temp.parameterName = null;
      }

      if (temp.description === '' || temp.description === undefined) {
        temp.description = null;
      }

      result.push(temp);
    }
  }

  return { data: result, variables: Array.from(variableSet) };
};

const wrapItems = ref(false); // 字段换行
const resetWrapWidth = () => {
  const _with = assertWrapperRef.value?.clientWidth;
  if (_with > 1000) {
    wrapItems.value = false;
  } else {
    wrapItems.value = true;
  }
};

defineExpose({
  getData,
  validate: toValidate,
  addItem: () => {
    const id = utils.uuid('a');
    idList.value.push(id);
  }
});

onMounted(() => {
  // 加载断言条件枚举选项
  loadAssertionConditionOptions();

  // 加载提取位置枚举选项
  loadLocationOptions();

  watch(() => props.value, async (newValue) => {
    reset();
    if (newValue?.length) {
      const list = JSON.parse(JSON.stringify(newValue));
      for (let i = 0, len = list.length; i < len; i++) {
        const id = utils.uuid('a');
        const data = list[i] as FormItem;
        const extraction = data.extraction;
        const assertionCondition = data.assertionCondition?.value || data.assertionCondition;
        idList.value.push(id);
        openMap.value[id] = true;
        dataMap.value[id] = {
          assertionCondition,
          condition: data.condition,
          description: data.description,
          expression: data.expression,
          matchItem: data.matchItem,
          enabled: data.enabled,
          expected: data.expected,
          extraction: data.extraction || generateDefaultExtraction(),
          parameterName: data.parameterName,
          name: data.name,
          type: data.type?.value || data.type
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

        if (['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL', 'REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
          extractDisabledSet.value.add(id);
        }
      }
    }

    addEmptyRecord();
    // @TODO 执行了多次，需要排查问题
  }, { immediate: true });

  watch(() => idList.value, (newValue) => {
    emit('update:num', newValue.length - 1);
  }, { immediate: true, deep: true });

  erd.listenTo(assertWrapperRef.value, resetWrapWidth);

  nextTick(() => {
    emit('rendered', true);
  });
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
        map[id] = t('xcan_apiAssert.regexMatchExpression');
        break;
      case 'XPATH_MATCH':
        map[id] = t('xcan_apiAssert.xpathMatchExpression');
        break;
      case 'JSON_PATH_MATCH':
        map[id] = t('xcan_apiAssert.jsonpathMatchExpression');
        break;
      default:
        map[id] = t('xcan_apiAssert.regexMatchExpression');
    }
  }

  return map;
});

const textAreaAutoSize = { minRows: 1, maxRows: 5 };

const enumFieldNames = { label: 'message', value: 'value' };
</script>
<template>
  <div ref="assertWrapperRef" class="space-y-3 pb-3">
    <div
      v-for="(item, index) in idList"
      :key="item"
      :class="{ 'not-enabled': !checkedSet.has(item) && hadCheckedSet.has(item) && !focusSet.has(item) }"
      class="flex items-start">
      <div class="flex items-center h-7 mr-2">
        <Checkbox
          :disabled="!dataMap[item].name?.length || props.viewType"
          :checked="checkedSet.has(item)"
          @change="checkboxChange($event, item)" />
      </div>
      <div class="flex-1 mr-3">
        <div v-if="!wrapItems" class="flex items-start space-x-2">
          <Validate style="flex: 0 0 calc((100% - 40px)/6*2 + 8px);" :text="nameErrorMsgMap[item]">
            <Input
              :ref="el => { nameRefs[index] = el }"
              v-model:value="dataMap[item].name"
              :placeholder="t('xcan_apiAssert.assertionName')"
              :title="t('xcan_apiAssert.assertionName')"
              :maxlength="200"
              :readonly="props.viewType"
              :error="nameErrorSet.has(item)"
              @focus="inputFocus(item)"
              @blur="nameBlur($event, item)"
              @pressEnter="namePressEnter(index)"
              @change="nameChange(item)" />
          </Validate>
          <Validate style="flex: 0 0 calc((100% - 40px)/6);" :text="conditionErrorMsgMap[item]">
            <div class="flex items-center space-x-2">
              <Input
                :ref="el => { conditionRefs[index] = el }"
                v-model:value="dataMap[item].condition"
                :placeholder="t('xcan_apiAssert.executionConditionExpression')"
                :title="t('xcan_apiAssert.executionConditionExpression')"
                class="flex-1"
                :readonly="!props.id || props.viewType"
                :error="conditionErrorSet.has(item)"
                @focus="inputFocus(item)"
                @blur="conditionBlur($event, item)"
                @change="conditionChange(item)"
                @pressEnter="conditionPressEnter(index)" />
              <ConditionPopover />
            </div>
          </Validate>
          <SelectEnum
            v-model:value="dataMap[item].type"
            style="flex: 0 0 calc((100% - 40px)/6);"
            :enumKey="AssertionType"
            :placeholder="t('xcan_apiAssert.assertionType')"
            :title="t('xcan_apiAssert.assertionType')"
            :readonly="props.viewType"
            :error="typeErrorSet.has(item)"
            @change="typeChange($event, item)" />
          <Input
            v-model:value="dataMap[item].parameterName"
            :disabled="dataMap[item].type !== 'HEADER'"
            :readonly="props.viewType"
            :error="headerNameeErrorSet.has(item)"
            :maxlength="400"
            style="flex: 0 0 calc((100% - 40px)/6);"
            :placeholder="t('xcan_apiAssert.responseHeaderName')"
            title="t('xcan_apiAssert.responseHeaderName')"
            @focus="inputFocus(item)"
            @blur="inputBlur(item)"
            @change="headerNameeChange(item)" />
          <div class="flex items-center flex-nowrap space-x-2" style="flex: 1;min-width: 100px;">
            <Select
              v-model:value="dataMap[item].assertionCondition"
              :placeholder="t('xcan_apiAssert.assertionCondition')"
              :title="t('xcan_apiAssert.assertionCondition')"
              class="flex-1 min-w-0"
              :disabled="!dataMap[item].type"
              :readonly="props.viewType"
              :options="optionsMap[item]"
              :fieldNames="enumFieldNames"
              :error="assertionConditionErrorSet.has(item)"
              @change="assertionConditionChange($event, item)" />
            <Switch
              class="flex-shrink-0 flex-grow-0"
              :disabled="extractDisabledSet.has(item) || props.viewType"
              :checked="extractSet.has(item)"
              :checkedChildren="t('xcan_apiAssert.extractExpectedValue')"
              :unCheckedChildren="t('xcan_apiAssert.expectedValue')"
              @change="switchChange($event, item)" />
          </div>
        </div>
        <template v-else>
          <div class="flex space-x-2">
            <Validate style="flex: 0 0 calc((100% - 16px)/3);" :text="nameErrorMsgMap[item]">
              <Input
                :ref="el => { nameRefs[index] = el }"
                v-model:value="dataMap[item].name"
                :placeholder="t('xcan_apiAssert.assertionName')"
                :title="t('xcan_apiAssert.assertionName')"
                :maxlength="200"
                :readonly="props.viewType"
                :error="nameErrorSet.has(item)"
                @focus="inputFocus(item)"
                @blur="nameBlur($event, item)"
                @pressEnter="namePressEnter(index)"
                @change="nameChange(item)" />
            </Validate>
            <Validate style="flex: 0 0 calc((100% - 16px)/3);" :text="conditionErrorMsgMap[item]">
              <div class="flex items-center space-x-2">
                <Input
                  :ref="el => { conditionRefs[index] = el }"
                  v-model:value="dataMap[item].condition"
                  :placeholder="t('xcan_apiAssert.executionConditionExpression')"
                  :title="t('xcan_apiAssert.executionConditionExpression')"
                  class="flex-1"
                  :readonly="!props.id || props.viewType"
                  :error="conditionErrorSet.has(item)"
                  @focus="inputFocus(item)"
                  @blur="conditionBlur($event, item)"
                  @change="conditionChange(item)"
                  @pressEnter="conditionPressEnter(index)" />
                <ConditionPopover />
              </div>
            </Validate>
            <div class="flex-1 inline-flex justify-end items-center">
              <Switch
                class="flex-shrink-0 flex-grow-0"
                :disabled="extractDisabledSet.has(item) || props.viewType"
                :checked="extractSet.has(item)"
                :checkedChildren="t('xcan_apiAssert.extractExpectedValue')"
                :unCheckedChildren="t('xcan_apiAssert.expectedValue')"
                @change="switchChange($event, item)" />
            </div>
          </div>
          <div class="mt-2 flex space-x-2">
            <SelectEnum
              v-model:value="dataMap[item].type"
              style="flex: 0 0 calc((100% - 16px)/3);"
              :enumKey="AssertionType"
              :placeholder="t('xcan_apiAssert.assertionType')"
              :title="t('xcan_apiAssert.assertionType')"
              :readonly="props.viewType"
              :error="typeErrorSet.has(item)"
              @change="typeChange($event, item)" />
            <Input
              v-model:value="dataMap[item].parameterName"
              :disabled="dataMap[item].type !== 'HEADER'"
              :readonly="props.viewType"
              :error="headerNameeErrorSet.has(item)"
              :maxlength="400"
              style="flex: 0 0 calc((100% - 16px)/3);"
              :placeholder="t('xcan_apiAssert.responseHeaderName')"
              :title="t('xcan_apiAssert.responseHeaderName')"
              @focus="inputFocus(item)"
              @blur="inputBlur(item)"
              @change="headerNameeChange(item)" />
            <Select
              v-model:value="dataMap[item].assertionCondition"
              :placeholder="t('xcan_apiAssert.assertionCondition')"
              :title="t('xcan_apiAssert.assertionCondition')"
              style="flex: 0 0 calc((100% - 16px)/3);"
              class="min-w-0"
              :disabled="!dataMap[item].type"
              :readonly="props.viewType"
              :options="optionsMap[item]"
              :fieldNames="enumFieldNames"
              :error="assertionConditionErrorSet.has(item)"
              @change="assertionConditionChange($event, item)" />
          </div>
        </template>
        <div :style="styleMap[item]" class="transition-all space-y-2 overflow-hidden">
          <template v-if="showExpectedSet.has(item)">
            <template v-if="extractSet.has(item)">
              <div class="flex items-start space-x-2">
                <SelectEnum
                  v-model:value="dataMap[item].extraction.method"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :enumKey="ExtractionMethod"
                  :placeholder="t('xcan_apiAssert.extractionMethod')"
                  :title="t('xcan_apiAssert.extractionMethod')"
                  :readonly="props.viewType"
                  :error="methodErrorSet.has(item)"
                  @change="methodChange(item)" />
                <Select
                  v-model:value="dataMap[item].extraction.location"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :placeholder="t('xcan_apiAssert.extractionLocation')"
                  :title="t('xcan_apiAssert.extractionLocation')"
                  :readonly="props.viewType"
                  :fieldNames="enumFieldNames"
                  :options="locationOptionsMap[item]"
                  :error="locationErrorSet.has(item)"
                  @change="locationChange(item)" />
                <Input
                  v-model:value="dataMap[item].extraction.parameterName"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :placeholder="t('xcan_apiAssert.extractionParameterName')"
                  :title="t('xcan_apiAssert.extractionParameterName')"
                  :maxlength="400"
                  :disabled="NOT_PARAMETER_NAME.includes(dataMap[item].extraction.location as any)"
                  :readonly="props.viewType"
                  :error="parameterNameErrorSet.has(item)"
                  @focus="inputFocus(item)"
                  @blur="inputBlur(item)"
                  @change="parameterNameChange(item)" />
                <Input
                  v-model:value="dataMap[item].extraction.defaultValue"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :placeholder="t('xcan_apiAssert.extractionDefaultValue')"
                  :title="t('xcan_apiAssert.extractionDefaultValue')"
                  :readonly="props.viewType"
                  @focus="inputFocus(item)"
                  @blur="inputBlur(item)" />
                <Validate style="flex: 0 0 calc((100% - 40px)/6);" :text="extractionExpressionErrorMsgMap[item]">
                  <Input
                    :ref="el => { extractionExpressionRefs[index] = el }"
                    v-model:value="dataMap[item].extraction.expression"
                    :placeholder="t('xcan_apiAssert.extractionExpression')"
                    :title="t('xcan_apiAssert.extractionExpression')"
                    :disabled="dataMap[item].extraction.method === 'EXACT_VALUE'"
                    :readonly="props.viewType"
                    :error="extractionExpressionErrorSet.has(item)"
                    @focus="inputFocus(item)"
                    @blur="expressionBlur($event, item)"
                    @change="extractionExpressionChange(item)"
                    @pressEnter="expressionEnter(index)" />
                </Validate>
                <div class="flex items-center space-x-2" style="flex: 0 0 calc((100% - 40px)/6);">
                  <Input
                    v-model:value="dataMap[item].extraction.matchItem"
                    :placeholder="t('xcan_apiAssert.matchItemRange')"
                    :title="t('xcan_apiAssert.matchItem')"
                    :disabled="dataMap[item].extraction.method === 'EXACT_VALUE'"
                    :readonly="props.viewType"
                    :max="2000"
                    :maxlength="4"
                    dataType="number"
                    @focus="inputFocus(item)"
                    @blur="inputBlur(item)" />
                  <MatchItemPopover />
                </div>
              </div>
            </template>
            <template v-else>
              <div class="flex items-start space-x-2">
                <template v-if="expressionShowSet.has(item)">
                  <Validate style="flex: 0 0 calc((100% - 40px)/6*2 + 8px);" :text="expressionErrorMsgMap[item]">
                    <Input
                      v-model:value="dataMap[item].expression"
                      :placeholder="expressionPlaceholder[item]"
                      :title="expressionPlaceholder[item]"
                      :error="expressionErrorSet.has(item)"
                      :maxlength="400"
                      :readonly="props.viewType"
                      @focus="inputFocus(item)"
                      @blur="inputBlur(item)"
                      @change="expressionChange(item)" />
                  </Validate>

                  <Input
                    v-model:value="dataMap[item].matchItem"
                    :placeholder="t('xcan_apiAssert.matchItemRange')"
                    :title="t('xcan_apiAssert.matchItem')"
                    style="flex: 0 0 calc((100% - 40px)/6);"
                    :max="2000"
                    :maxlength="4"
                    :readonly="props.viewType"
                    dataType="number"
                    @focus="inputFocus(item)"
                    @blur="inputBlur(item)" />

                  <div class="flex items-center space-x-2" style="flex: 1;">
                    <Input
                      v-model:value="dataMap[item].expected"
                      placeholder="t('xcan_apiAssert.expectedValuePlaceholder')"
                      title="t('xcan_apiAssert.expectedValueTitle')"
                      :readonly="props.viewType"
                      :error="expectedErrorSet.has(item)"
                      @focus="inputFocus(item)"
                      @blur="inputBlur(item)"
                      @change="expectedChange(item)" />
                    <ExpectedPopover class="flex-shrink-0" />
                  </div>
                </template>
                <template v-else>
                  <Input
                    v-model:value="dataMap[item].expected"
                    type="textarea"
                    :placeholder="t('xcan_apiAssert.expectedValue')"
                    :title="t('xcan_apiAssert.expectedValue')"

                    :readonly="props.viewType"
                    :autoSize="textAreaAutoSize"
                    :error="expectedErrorSet.has(item)"
                    @focus="inputFocus(item)"
                    @blur="inputBlur(item)"
                    @change="expectedChange(item)" />
                </template>
              </div>
            </template>
          </template>
          <Input
            v-model:value="dataMap[item].description"
            :placeholder="t('xcan_apiAssert.description')"
            :title="t('xcan_apiAssert.description')"

            type="textarea"
            :readonly="props.viewType"
            :autoSize="textAreaAutoSize"
            @focus="inputFocus(item)"
            @blur="inputBlur(item)" />
        </div>
      </div>
      <div class="flex items-center h-7 space-x-2">
        <Icon
          v-show="!props.viewType"
          class="cursor-pointer text-theme-sub-content hover:text-text-link-hover"
          icon="icon-shanchuguanbi"
          @click="deleteHandler(index, item)" />
        <Arrow v-model:open="openMap[item]" class="hover:text-text-link-hover" />
      </div>
    </div>
  </div>
</template>
<style scoped>
.not-enabled {
  opacity: 0.5;
}
</style>
