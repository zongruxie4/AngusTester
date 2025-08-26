<script setup lang="ts">
import { ref, onMounted, computed, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox } from 'ant-design-vue';
import { Arrow, Icon, Input, SelectEnum, Select, Tooltip } from '@xcan-angus/vue-ui';
import { AssertionCondition, BasicAssertionType, enumUtils, utils, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

import { AssertionConfig } from './PropsType';
import ExpectedPopover from './ExpectedPopover.vue';

export interface Props {
  value: AssertionConfig[];
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const { t } = useI18n();

const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: AssertionConfig }>({});
const conditionOptions = ref<{
  'BODY': { message: string; value: string; }[];
  'BODY_SIZE': { message: string; value: string; }[];
  'DURATION': { message: string; value: string; }[];
}>({ BODY: [], BODY_SIZE: [], DURATION: [] });

const nameErrorSet = ref<Set<string>>(new Set());
const typeErrorSet = ref<Set<string>>(new Set());
const conditionErrorSet = ref<Set<string>>(new Set());
const expressionErrorSet = ref<Set<string>>(new Set());
const expectedErrorSet = ref<Set<string>>(new Set());
const unfoldSet = ref<Set<string>>(new Set());
const checkedSet = ref<Set<string>>(new Set());
const repeatNameSet = ref<Set<string>>(new Set());

const loadConditionOptions = async () => {
  const data = enumUtils.enumToMessages(AssertionCondition);
  const numberConditions = data.filter(item => NUMBER_CONDITIONS.includes(item.value));
  conditionOptions.value = {
    BODY: data,
    BODY_SIZE: numberConditions,
    DURATION: numberConditions
  };
};

const addNewItem = () => {
  const id = utils.uuid();
  unfoldSet.value.add(id);
  idList.value.push(id);
  dataMap.value[id] = {
    assertionCondition: undefined,
    description: '',
    enabled: false,
    expected: '',
    expression: '',
    matchItem: '',
    name: '',
    type: undefined
  };
};

const delHandler = (index: number, id: string) => {
  const length = idList.value.length - 1;
  idList.value.splice(index, 1);
  delete dataMap.value[id];
  nameErrorSet.value.delete(id);
  typeErrorSet.value.delete(id);
  conditionErrorSet.value.delete(id);
  expressionErrorSet.value.delete(id);
  expectedErrorSet.value.delete(id);
  unfoldSet.value.delete(id);
  checkedSet.value.delete(id);
  repeatNameSet.value.delete(id);
  // 删除的是最后一条，自动添加一条新断言
  if (index === length) {
    addNewItem();
  }
};

const unfoldChange = (value: boolean, id: string) => {
  if (value) {
    unfoldSet.value.add(id);
    return;
  }

  unfoldSet.value.delete(id);
};

const checkboxChange = (id: string) => {
  checkedSet.value.add(id);
};

const nameChange = debounce(duration.search, (event: { target: { value: string; } }, id: string, index: number) => {
  const value = event.target.value;
  dataMap.value[id].name = value;
  nameErrorSet.value.delete(id);

  if (!checkedSet.value.has(id)) {
    dataMap.value[id].enabled = true;
    checkedSet.value.add(id);
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

  // 保证最后一条是空的
  if (index === idList.value.length - 1) {
    if (value) {
      addNewItem();
    }
  }
});

const typeChange = (value: BasicAssertionType, id: string) => {
  dataMap.value[id].type = value;
  const condition = dataMap.value[id].assertionCondition;
  typeErrorSet.value.delete(id);
  if (!NUMBER_CONDITIONS.includes(condition)) {
    dataMap.value[id].assertionCondition = undefined;
    if (EXPRESSION_CONDITIONS.includes(condition)) {
      dataMap.value[id].expression = '';
      dataMap.value[id].expected = '';
      dataMap.value[id].matchItem = '';
    }
  }
};

const conditionChange = (value: AssertionCondition, id: string) => {
  dataMap.value[id].assertionCondition = value;
  conditionErrorSet.value.delete(id);

  // 切换条件后要重置表达式
  expressionErrorSet.value.delete(id);
  dataMap.value[id].expression = '';

  if (EXPECTED_DISABLED.includes(value)) {
    dataMap.value[id].expected = '';
    dataMap.value[id].expression = '';
    dataMap.value[id].matchItem = '';
    return;
  }

  if (!EXPRESSION_CONDITIONS.includes(value)) {
    dataMap.value[id].expression = '';
    dataMap.value[id].matchItem = '';
  }
};

const expressionChange = (event: { target: { value: string; } }, id: string) => {
  const value = event.target.value;
  dataMap.value[id].expression = value;
  expressionErrorSet.value.delete(id);
};

const matchItemChange = (event: { target: { value: string; } }, id: string) => {
  const value = event.target.value;
  dataMap.value[id].matchItem = value;
};

const expectedChange = (event: { target: { value: string; } }, id: string) => {
  const value = event.target.value;
  dataMap.value[id].expected = value;
  expectedErrorSet.value.delete(id);
};

const descriptionChange = (event: { target: { value: string; } }, id: string) => {
  const value = event.target.value;
  dataMap.value[id].description = value;
};

onMounted(() => {
  loadConditionOptions();
  watch(() => props.value, (newValue) => {
    reset();
    if (newValue?.length) {
      for (let i = 0, len = newValue.length; i < len; i++) {
        const id = utils.uuid();
        const { assertionCondition, description, enabled, expected, expression, matchItem, name, type } = newValue[i];
        checkedSet.value.add(id);
        unfoldSet.value.add(id);
        idList.value.push(id);
        dataMap.value[id] = {
          assertionCondition,
          description,
          enabled,
          expected,
          expression,
          matchItem,
          name,
          type
        };
      }
    }

    addNewItem();
  }, { immediate: true });
});

const reset = () => {
  idList.value = [];
  dataMap.value = {};

  nameErrorSet.value.clear();
  typeErrorSet.value.clear();
  conditionErrorSet.value.clear();
  expressionErrorSet.value.clear();
  expectedErrorSet.value.clear();
  unfoldSet.value.clear();
  checkedSet.value.clear();
  repeatNameSet.value.clear();
};

const excludes = ({ value }) => {
  return ['STATUS', 'HEADER', 'SIZE'].includes(value);
};

const styleMap = computed(() => {
  const set = unfoldSet.value;
  return idList.value.reduce((prev, cur) => {
    if (set.has(cur)) {
      prev[cur] = {
        height: 'auto',
        marginTop: '8px',
        opacity: '100'
      };
    } else {
      prev[cur] = {
        height: '0',
        marginTop: '0',
        opacity: '0'
      };
    }

    return prev;
  }, {});
});

const PLACEHOLDER = {
  REG_MATCH: t('jdbcPlugin.UIConfigJdbc.assertionForm.expressionMap.REG_MATCH'),
  XPATH_MATCH: t('jdbcPlugin.UIConfigJdbc.assertionForm.expressionMap.XPATH_MATCH'),
  JSON_PATH_MATCH: t('jdbcPlugin.UIConfigJdbc.assertionForm.expressionMap.JSON_PATH_MATCH')
};
const EXPRESSION_CONDITIONS = ['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'];
const EXPECTED_DISABLED = ['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL'];
const NUMBER_CONDITIONS = ['EQUAL', 'NOT_EQUAL', 'GREATER_THAN', 'GREATER_THAN_EQUAL', 'LESS_THAN', 'LESS_THAN_EQUAL'];

const getData = (): AssertionConfig[] => {
  const ids = idList.value;
  const map = dataMap.value;
  const data: AssertionConfig[] = [];
  // 最后一条是空的，不保存
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    data.push({ ...map[ids[i]] });
  }

  return JSON.parse(JSON.stringify(data));
};

const isValid = (): number => {
  nameErrorSet.value.clear();
  typeErrorSet.value.clear();
  conditionErrorSet.value.clear();
  expressionErrorSet.value.clear();
  expectedErrorSet.value.clear();
  repeatNameSet.value.clear();

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
  // 最后一条是空数据，不校验
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    const id = ids[i];
    const {
      assertionCondition,
      expected,
      expression,
      name,
      type
    } = data[id];

    if (!name) {
      nameErrorSet.value.add(id);
    } else {
      if (duplicates.includes(name)) {
        nameErrorSet.value.add(id);
        repeatNameSet.value.add(id);
      }
    }

    if (!type) {
      typeErrorSet.value.add(id);
    }

    if (!assertionCondition) {
      conditionErrorSet.value.add(id);
    }

    if (!EXPECTED_DISABLED.includes(assertionCondition) && !EXPRESSION_CONDITIONS.includes(assertionCondition)) {
      if (!expected) {
        expectedErrorSet.value.add(id);
      }
    }

    if (EXPRESSION_CONDITIONS.includes(assertionCondition)) {
      if (!expression) {
        expressionErrorSet.value.add(id);
      }
    }
  }

  return nameErrorSet.value.size +
    typeErrorSet.value.size +
    conditionErrorSet.value.size +
    expressionErrorSet.value.size +
    expectedErrorSet.value.size;
};

defineExpose({
  isValid,
  getData
});
</script>

<template>
  <div class="space-y-4">
    <div
      v-for="(item, index) in idList"
      :key="item"
      :class="{'opacity-70':!dataMap[item].enabled}"
      class="flex items-start">
      <div class="flex items-center h-7 mr-2">
        <Checkbox v-model:checked="dataMap[item].enabled" @change="checkboxChange(item)" />
      </div>
      <div class="flex-1 space-y-2 mr-3">
        <div class="flex items-start space-x-2">
          <Tooltip
            :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.nameDuplicate')"
            internal
            placement="right"
            destroyTooltipOnHide
            :visible="repeatNameSet.has(item)">
            <Input
              :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionNamePlaceholder')"
              :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionName')"
              trim
              style="flex: 1 1 calc((100% - 32px)/7*2);"
              :maxlength="200"
              :value="dataMap[item].name"
              :error="nameErrorSet.has(item)"
              @change="nameChange($event, item,index)" />
          </Tooltip>
          <SelectEnum
            style="flex: 0 0 calc((100% - 32px)/7);"
            enumKey="BasicAssertionType"
            :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionTypePlaceholder')"
            :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionType')"
            :error="typeErrorSet.has(item)"
            :value="dataMap[item].type"
            :excludes="excludes"
            @change="typeChange($event, item)" />
          <Select
            style="flex: 0 0 calc((100% - 32px)/7);"
            :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionConditionPlaceholder')"
            :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionCondition')"
            class="flex-1"
            :disabled="!dataMap[item].type"
            :error="conditionErrorSet.has(item)"
            :value="dataMap[item].assertionCondition"
            :fieldNames="{ label: 'message', value: 'value' }"
            :options="conditionOptions[dataMap[item].type]"
            @change="conditionChange($event, item)" />
          <template v-if="EXPRESSION_CONDITIONS.includes(dataMap[item].assertionCondition)">
            <Tooltip
              :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.expressionFormatError')"
              placement="top"
              destroyTooltipOnHide
              :visible="expressionErrorSet.has(item)">
              <Input
                trim
                style="flex: 0 0 calc((100% - 32px)/7*2);"
                :maxlength="400"
                :placeholder="PLACEHOLDER[dataMap[item].assertionCondition]"
                :value="dataMap[item].expression"
                :error="expressionErrorSet.has(item)"
                @change="expressionChange($event, item)" />
            </Tooltip>
            <Input
              :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.matchItemPlaceholder')"
               :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.matchItem')"
              trim
              dataType="number"
              style="flex: 0 0 calc((100% - 32px)/7);"
              :value="dataMap[item].matchItem"
              :max="2000"
              :maxlength="4"
              @change="matchItemChange($event, item)" />
          </template>
          <template v-else>
            <Input
              :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.expectedValuePlaceholder')"
              :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.expectedValue')"
              style="flex: 0 0 calc((100% - 32px)/7*2);"
              trim
              :disabled="EXPECTED_DISABLED.includes(dataMap[item].assertionCondition)"
              :value="dataMap[item].expected"
              :error="expectedErrorSet.has(item)"
              @change="expectedChange($event, item)" />
          </template>
        </div>

        <div :style="styleMap[item]" class="transition-all space-y-2 overflow-hidden">
          <div
            v-if="EXPRESSION_CONDITIONS.includes(dataMap[item].assertionCondition)"
            class="flex items-center space-x-2">
            <Input
              class="flex-1"
              :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.expectedValueOptional')"
              :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.expectedValue')"
              trim
              :value="dataMap[item].expected"
              :error="expectedErrorSet.has(item)"
              @change="expectedChange($event, item)" />
            <ExpectedPopover class="flex-shrink-0" />
          </div>
          <Input
            :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.descriptionPlaceholder')"
            :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.description')"
            type="textarea"
            trim
            :value="dataMap[item].description"
            @change="descriptionChange($event, item)" />
        </div>
      </div>

      <div class="flex items-center h-7 space-x-2">
        <Icon
          class="cursor-pointer text-theme-sub-content hover:text-text-link-hover"
          icon="icon-shanchuguanbi"
          @click="delHandler(index, item)" />
        <Arrow
          class="hover:text-text-link-hover"
          :open="unfoldSet.has(item)"
          @change="unfoldChange($event, item)" />
      </div>
    </div>
  </div>
</template>
<style scoped>
.ant-input-affix-wrapper:not(.ant-input-affix-wrapper-disabled),
:deep(.ant-checkbox),
.ant-input:not(.ant-input-disabled),
.ant-select:not(.ant-select-disabled) :deep(.ant-select-selector) {
  background-color: #fff;
}
</style>
