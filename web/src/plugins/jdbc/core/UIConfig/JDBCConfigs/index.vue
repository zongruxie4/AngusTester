<script setup lang="ts">
import { ref, defineAsyncComponent, onMounted, watch, computed, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Badge, Collapse, CollapsePanel, Tabs, TabPane, Dropdown, Menu, MenuItem } from 'ant-design-vue';
import { Input, Tooltip, Icon, SelectEnum, SelectInput, Validate, FunctionsButton } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { cloneDeep } from 'lodash-es';

const { t } = useI18n();

import { JDBCConfig, JDBCConfigInfo, QueryType, QueryArguments } from './PropsType';
import { AssertionConfig } from './AssertionForm/PropsType';
import ActionsGroup from '../ActionsGroup/index.vue';

export interface Props {
  value: JDBCConfigInfo;
  repeatNames: string[];
  enabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  repeatNames: () => [],
  enabled: true
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: Omit<JDBCConfig, 'id'>): void;
  (e: 'nameChange', value: string): void;
  (e: 'click', value: 'delete' | 'clone'): void;
  (e: 'enabledChange', value: boolean): void;
  (e: 'renderChange'): void;
}>();

const AssertionForm = defineAsyncComponent(() => import('./AssertionForm/index.vue'));
const Parametric = defineAsyncComponent(() => import('./Parametric/index.vue'));

const assertRef = ref();
const parametricRef = ref();

const UUID = utils.uuid();
const activeKey = ref<string>(UUID);
const tabActiveKey = ref<'sql' | 'assertions' | 'query'>('sql');

const enabled = ref(false);
const name = ref('');
const description = ref('');
const queryType = ref<QueryType>();
const maxResultRows = ref('1000');
const timeoutInSecond = ref('60');
const argumentIds = ref<string[]>([]);
const argumentsMap = ref<{ [key: string]: QueryArguments }>({});
const sql = ref('');
const assertionList = ref<AssertionConfig[]>([]);

const variables = ref<JDBCConfig['variables']>([]);
const defaultVariables = ref<JDBCConfig['variables']>([]);

const datasets = ref<JDBCConfig['datasets']>([]);
const defaultDatasets = ref<JDBCConfig['datasets']>([]);

const actionOnEOF = ref<'RECYCLE' | 'STOP_THREAD'>('RECYCLE');
const sharingMode = ref<'ALL_THREAD' | 'CURRENT_THREAD'>('ALL_THREAD');

const nameError = ref(false);
const queryTypeError = ref(false);
const inoutErrorMap = ref<{ [key: string]: boolean }>({});
const inoutValueErrorMap = ref<{ [key: string]: boolean }>({});
const sqlErrorNum = ref(0);
const sqlErrorMessage = ref<string>();
const assertErrorNum = ref(0);
const parametricErrorNum = ref(0);
const queryErrorNum = ref(0);
const nameRepeatFlag = ref(false);

const nameChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  name.value = value;
  nameError.value = false;
  nameRepeatFlag.value = false;
  emit('nameChange', value);
};

const deleteQuery = (index: number, id: string) => {
  argumentIds.value.splice(index, 1);
  delete argumentsMap.value[id];
  delete inoutErrorMap.value[id];
  delete inoutValueErrorMap.value[id];
  // 删除的是最后一条，自动添加一条新断言
  if (argumentIds.value.length === 0) {
    addNewArgument();
  }
};

const addQuery = (index: number) => {
  addNewArgument(index + 1);
};

const sqlChange = (event: { target: { value: string } }) => {
  sql.value = event.target.value;
  sqlErrorNum.value = 0;
  sqlErrorMessage.value = undefined;
};

const assertionChange = (data: AssertionConfig[]) => {
  assertionList.value = data;
};

const variablesChange = (data: JDBCConfig['variables']) => {
  variables.value = data;
};

const datasetsChange = (data: JDBCConfig['datasets']) => {
  datasets.value = data;
};

const inoutChange = (id: string) => {
  if (inoutErrorMap.value[id]) {
    queryErrorNum.value--;
  }
  inoutErrorMap.value[id] = false;
};

const valueChange = (id: string) => {
  if (inoutValueErrorMap.value[id]) {
    queryErrorNum.value--;
  }
  inoutValueErrorMap.value[id] = false;
};

const openChange = (_open: boolean) => {
  if (_open) {
    activeKey.value = UUID;
    return;
  }

  activeKey.value = '';
};

const enabledChange = (_enabled: boolean) => {
  enabled.value = _enabled;
  emit('enabledChange', _enabled);
};

const actionClick = (value: 'delete' | 'clone') => {
  emit('click', value);
};

const queryTypeChange = (value: QueryType) => {
  queryTypeError.value = false;
  if (!QUERY_ENUMS.includes(value)) {
    argumentIds.value = [];
    argumentsMap.value = {};
    tabActiveKey.value = 'sql';
    return;
  }

  if (!argumentIds.value.length) {
    addNewArgument();
  }
};

const insertSql = ({ key }: { key: 'INSERT' | 'SELECT' | 'UPDATE' | 'DELETE' }) => {
  const menuItem = SQL_TEMPLATES.find(item => item.key === key);
  sql.value = menuItem.value;
  sqlErrorNum.value = 0;
};

const addNewArgument = (index?: number) => {
  const id = utils.uuid();
  if (index >= 0) {
    argumentIds.value.splice(index, 0, id);
  } else {
    argumentIds.value.push(id);
  }
  argumentsMap.value[id] = {
    type: undefined,
    value: undefined,
    inout: undefined
  };
};

const initializedData = () => {
  if (!props.value) {
    return;
  }

  const {
    type,
    assertions: _assertions,
    variables: _variables,
    datasets: _datasets,
    actionOnEOF: _actionOnEOF,
    sharingMode: _sharingMode,
    arguments: _queryArguments,
    name: _name,
    enabled: _enabled,
    description: _description,
    sql: _sql,
    timeoutInSecond: _timeoutInSecond,
    maxResultRows: _maxResultRows
  } = props.value;
  name.value = _name;
  description.value = _description;
  enabled.value = _enabled;
  sql.value = _sql;
  timeoutInSecond.value = _timeoutInSecond;
  maxResultRows.value = _maxResultRows;
  queryType.value = type?.value || type;

  actionOnEOF.value = _actionOnEOF?.value || _actionOnEOF || 'RECYCLE';
  sharingMode.value = _sharingMode?.value || _sharingMode || 'ALL_THREAD';

  variables.value = _variables || [];
  defaultVariables.value = cloneDeep(_variables);

  datasets.value = _datasets || [];
  defaultDatasets.value = cloneDeep(_datasets);

  assertionList.value = (_assertions || []).map(item => {
    return {
      ...item,
      assertionCondition: item.assertionCondition?.value || item.assertionCondition,
      type: item.type?.value || item.type
    };
  });

  if (_queryArguments?.length) {
    for (let i = 0, len = _queryArguments.length; i < len; i++) {
      const id = utils.uuid();
      argumentIds.value.push(id);
      argumentsMap.value[id] = _queryArguments[i];
    }
  } else {
    addNewArgument();
  }
};

onMounted(() => {
  emit('renderChange');
  initializedData();
  watch(() => props.repeatNames, (newValue) => {
    if (newValue.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
      return;
    }

    if (nameRepeatFlag.value) {
      nameError.value = false;
      nameRepeatFlag.value = false;
    }
  });

  watchEffect(() => {
    const data = getData();
    emit('change', data);
  });
});

const isValid = (): boolean => {
  nameError.value = false;
  queryTypeError.value = false;
  inoutErrorMap.value = {};
  inoutValueErrorMap.value = {};
  sqlErrorNum.value = 0;
  sqlErrorMessage.value = undefined;
  assertErrorNum.value = 0;
  queryErrorNum.value = 0;
  nameRepeatFlag.value = false;
  let errorNum = 0;
  if (!name.value) {
    errorNum++;
    nameError.value = true;
  } else {
    if (props.repeatNames.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
    }
  }

  const _queryType = queryType.value;
  if (!_queryType) {
    errorNum++;
    queryTypeError.value = true;
  }

  if (!sql.value) {
    errorNum++;
    sqlErrorNum.value = 1;
  } else if (_queryType) {
    // let regexp = /^[\s\S]*SELECT[\s\S]+FROM[\s\S]+$/i;
    // if (_queryType === 'PREPARED_UPDATE' || _queryType === 'UPDATE') {
    //   regexp = /^[\s\S]*UPDATE[\s\S]+SET[\s\S]+WHERE[\s\S]+$/i;
    // } else if (_queryType === 'CALLABLE') {
    //   regexp = /^[\s\n?={]*CALL[\s\S]+$/i;
    // }
    // if (!regexp.test(sql.value)) {
    //   errorNum++;
    //   sqlErrorNum.value = 1;
    //   sqlErrorMessage.value = 'SQL语法错误';
    // }
  }

  const argsLength = argumentIds.value.length;
  if (argsLength > 1) {
    const ids = argumentIds.value;
    for (let i = 0, len = ids.length; i < len; i++) {
      const id = ids[i];
      const { inout, value } = argumentsMap.value[id];
      if (!inout) {
        inoutErrorMap.value[id] = true;
        queryErrorNum.value++;
        errorNum++;
      } else if (['IN', 'INOUT'].includes(inout)) {
        if (!value) {
          inoutValueErrorMap.value[id] = true;
          queryErrorNum.value++;
          errorNum++;
        }
      }
    }
  } else if (argsLength === 1) {
    // 只有一条，如果参数类型，输入输出、参数值都为空，则不校验，查询参数时非必填
    const id = argumentIds.value[0];
    const { inout, value } = argumentsMap.value[id];
    if (inout && ['INOUT', 'IN'].includes(inout)) {
      if (!value) {
        errorNum++;
        inoutValueErrorMap.value[id] = true;
        queryErrorNum.value++;
      }
    }
  }

  if (typeof assertRef.value?.isValid === 'function') {
    assertErrorNum.value = assertRef.value.isValid();
    if (assertErrorNum.value) {
      errorNum++;
    }
  }

  if (typeof parametricRef.value?.isValid === 'function') {
    if (!parametricRef.value.isValid()) {
      errorNum++;
    }
  }

  return !errorNum;
};

const getData = (): Omit<JDBCConfig, 'id'> => {
  const _arguments = Object.values(argumentsMap.value).filter(item => !!item.inout);
  return {
    arguments: _arguments,
    assertions: assertionList.value,
    variables: variables.value,
    datasets: datasets.value,
    actionOnEOF: actionOnEOF.value,
    sharingMode: sharingMode.value,
    beforeName: '',
    transactionName: '',
    sql: sql.value,
    description: description.value,
    enabled: enabled.value,
    name: name.value,
    maxResultRows: maxResultRows.value,
    timeoutInSecond: timeoutInSecond.value,
    type: queryType.value,
    target: 'JDBC'
  };
};

defineExpose({
  getData,
  isValid,
  getName: (): string => {
    return name.value;
  },
  validateRepeatName: (value: string[]): boolean => {
    if (value.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
      return false;
    }

    return true;
  }
});

const assertNum = computed(() => {
  return assertionList.value.length;
});

const argumentsNum = computed(() => {
  return Object.values(argumentsMap.value).filter(item => !!item.inout).length;
});

const QUERY_ENUMS = ['CALLABLE', 'PREPARED_SELECT', 'PREPARED_UPDATE'];

const showQueryTab = computed(() => {
  return QUERY_ENUMS.includes(queryType.value);
});

const SQL_TEMPLATES = [{
  key: 'INSERT',
  name: 'INSERT',
  value: `INSERT INTO table_name (column1, column2, column3, ...)
VALUES (value1, value2, value3, ...);`
},
{
  key: 'SELECT',
  name: 'SELECT',
  value: `SELECT column1, column2, ...
FROM table_name
WHERE condition;`
},
{
  key: 'UPDATE',
  name: 'UPDATE',
  value: `UPDATE table_name
SET column1 = value1, column2 = value2, ...
WHERE condition;`
},
{
  key: 'DELETE',
  name: 'DELETE',
  value: `DELETE FROM table_name
WHERE condition;`
}];

const inputProps = {
  trimAll: true,
  allowClear: true
};
</script>

<template>
  <Collapse
    :activeKey="activeKey"
    :class="{ 'opacity-70': !enabled && props.enabled }"
    class="jdbc-collapse-container"
    :bordered="false">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      style="background-color: rgba(251, 251, 251, 100%);"
      collapsible="disabled">
      <template #header>
        <div class="flex items-center flex-nowrap w-full whitespace-nowrap">
          <Icon icon="icon-jdbc" class="flex-shrink-0 mr-3 text-4" />
          <div class="flex-1 flex items-center space-x-5 mr-5">
            <Tooltip
              :title="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.nameDuplicate')"
              internal
              placement="right"
              destroyTooltipOnHide
              :visible="nameRepeatFlag">
              <Input
                :value="name"
                :maxlength="400"
                :error="nameError"
                :title="name"
                trim
                class="jdbc-name-input"
                :placeholder="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.namePlaceholder')"
                @change="nameChange" />
            </Tooltip>
            <Input
              v-model:value="description"
              :maxlength="800"
              :title="description"
              trim
              class="flex-1"
              :placeholder="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.descriptionPlaceholder')" />
          </div>
          <ActionsGroup
            v-model:enabled="enabled"
            :open="activeKey === UUID"
            :arrowVisible="true"
            @openChange="openChange"
            @enabledChange="enabledChange"
            @click="actionClick" />
        </div>
      </template>

      <Tabs v-model:activeKey="tabActiveKey" size="small">
        <template #rightExtra>
          <div class="flex items-center space-x-3">
            <SelectEnum
              v-model:value="queryType"
              :error="queryTypeError"
              enumKey="QueryType"
              class="w-70"
              :placeholder="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.queryTypePlaceholder')"
              @change="queryTypeChange" />
            <Dropdown destroyPopupOnHide :mouseEnterDelay="0.3">
              <template #overlay>
                <Menu
                  size="small"
                  mode="vertical"
                  @click="insertSql">
                  <MenuItem v-for="item in SQL_TEMPLATES" :key="item.key">
                    <div class="leading-5">{{ item.name }}</div>
                  </MenuItem>
                </Menu>
              </template>
              <template #default>
                <Button type="link" size="small">
                  {{ t('jdbcPlugin.UIConfigJdbc.jdbcConfig.insertSqlTemplate') }}
                </Button>
              </template>
            </Dropdown>
            <FunctionsButton class="text-3.5" />
          </div>
        </template>
        <TabPane key="sql">
          <template #tab>
            <Badge size="small" :count="sqlErrorNum">
              <div>SQL</div>
            </Badge>
          </template>
          <div>
            <Validate :error="!!sqlErrorNum" :text="sqlErrorMessage">
              <Input
                v-model:value="sql"
                :autoSize="{ minRows: 6, maxRows: 6 }"
                :error="!!sqlErrorNum"
                :maxlength="8192"
                trim
                showCount
                type="textarea"
                :placeholder="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.sqlPlaceholder')"
                @change="sqlChange" />
            </Validate>
            <div class="flex items-center leading-5 mt-5 space-x-5">
              <div class="flex items-center space-x-2">
                <div class="flex-shrink-0 flex items-center space-x-1">
                  <span class="flex-shrink-0">{{ t('jdbcPlugin.UIConfigJdbc.jdbcConfig.processResultRows') }}</span>
                  <Tooltip
                                          :title="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.processResultRowsTooltip')"
                    internal
                    placement="right"
                    destroyTooltipOnHide>
                    <Icon icon="icon-tishi1" class="flex-shrink-0 text-3.5 cursor-pointer text-tips" />
                  </Tooltip>
                </div>
                <Input
                  v-model:value="maxResultRows"
                  dataType="number"
                  class="flex-1"
                  :placeholder="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.processResultRowsPlaceholder')"
                  trimAll
                  :min="1"
                  :max="10000" />
              </div>
              <div class="flex items-center space-x-2">
                <div class="flex-shrink-0 flex items-center space-x-1">
                  <span class="flex-shrink-0">{{ t('jdbcPlugin.UIConfigJdbc.jdbcConfig.queryTimeout') }}</span>
                </div>
                <Input
                  v-model:value="timeoutInSecond"
                  dataType="number"
                  class="flex-1"
                  :placeholder="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.queryTimeoutPlaceholder')"
                  trimAll
                  :min="0"
                  :max="2147483647" />
                <span>秒</span>
              </div>
            </div>
          </div>
        </TabPane>
        <TabPane v-if="showQueryTab" key="query">
          <template #tab>
            <Badge size="small" :count="queryErrorNum">
              <div class="flex items-center space-x-0.5">
                <div>{{ t('jdbcPlugin.UIConfigJdbc.jdbcConfig.queryParameters') }}</div>
                <div class="flex items-center space-x-0.5">
                  <em>(</em>
                  <span>{{ argumentsNum }}</span>
                  <em>)</em>
                </div>
              </div>
            </Badge>
          </template>
          <div class="space-y-3">
            <div
              v-for="(item, index) in argumentIds"
              :key="item"
              class="flex items-start">
              <SelectInput
                v-model:value="argumentsMap[item].type"
                :inputProps="inputProps"
                class="w-50 mr-2"
                enumKey="ColumnType"
                :title="argumentsMap[item].type || t('jdbcPlugin.UIConfigJdbc.jdbcConfig.parameterTypePlaceholder')"
                :placeholder="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.parameterTypePlaceholder')" />
              <SelectEnum
                v-model:value="argumentsMap[item].inout"
                :error="inoutErrorMap[item]"
                allowClear
                enumKey="InputOutputType"
                :placeholder="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.inputOutputPlaceholder')"
                class="w-30 mr-2"
                @change="inoutChange(item)" />
              <Validate
                class="flex-1 mr-3"
                :error="inoutValueErrorMap[item]"
                mode="error"
                :text="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.inputOutputTooltip')">
                <Input
                  v-model:value="argumentsMap[item].value"
                  :maxlength="8192"
                  :error="inoutValueErrorMap[item]"
                  allowClear
                  trim
                  :title="argumentsMap[item].value || t('jdbcPlugin.UIConfigJdbc.jdbcConfig.parameterValuePlaceholder')"
                  :placeholder="t('jdbcPlugin.UIConfigJdbc.jdbcConfig.parameterValuePlaceholder')"
                  @change="valueChange(item)" />
              </Validate>
              <div class="flex-shrink-0 flex items-center h-7 space-x-2">
                <Button
                  size="small"
                  class="w-7 h-7 p-0"
                  @click="deleteQuery(index, item)">
                  <Icon icon="icon-shanchuguanbi" />
                </Button>
                <Button
                  size="small"
                  class="w-7 h-7 p-0"
                  @click="addQuery(index)">
                  <Icon icon="icon-jia" />
                </Button>
              </div>
            </div>
          </div>
        </TabPane>
        <TabPane key="parametric">
          <template #tab>
            <Badge
              class="count-Badge-container"
              size="small"
              :count="parametricErrorNum">
              <div class="flex items-center space-x-0.5">
                <div>{{ t('jdbcPlugin.UIConfigJdbc.jdbcConfig.parametric') }}</div>
              </div>
            </Badge>
          </template>
          <Parametric
            ref="parametricRef"
            v-model:errorNum="parametricErrorNum"
            v-model:actionOnEOF="actionOnEOF"
            v-model:sharingMode="sharingMode"
            :variables="defaultVariables"
            :datasets="defaultDatasets"
            @variablesChange="variablesChange"
            @datasetsChange="datasetsChange" />
        </TabPane>
        <TabPane key="assertions">
          <template #tab>
            <Badge size="small" :count="assertErrorNum">
              <div class="flex items-center space-x-0.5">
                <div>{{ t('jdbcPlugin.UIConfigJdbc.jdbcConfig.assertions') }}</div>
                <div class="flex items-center space-x-0.5">
                  <em>(</em>
                  <span>{{ assertNum }}</span>
                  <em>)</em>
                </div>
              </div>
            </Badge>
          </template>
          <AssertionForm
            ref="assertRef"
            v-model:errorNum="assertErrorNum"
            :value="assertionList"
            @change="assertionChange" />
        </TabPane>
      </Tabs>
    </CollapsePanel>
  </Collapse>
</template>
<style scoped>
em {
  font-style: normal;
}

.jdbc-name-input {
  flex: 0 0 calc((100% - (121px))*2/5);
}

.child-drag-container .jdbc-name-input {
  flex: 0 0 calc((100% - (132px))*2/5);
}

.ant-collapse.jdbc-collapse-container {
  line-height: 20px;
}

.ant-collapse-borderless.jdbc-collapse-container> :deep(.ant-collapse-item) {
  border: none;
  border-radius: 4px;
}

.ant-collapse.jdbc-collapse-container> :deep(.ant-collapse-item)>.ant-collapse-header {
  align-items: center;
  height: 46px;
  padding: 0 12px 0 38px;
  border-radius: 4px;
  background-color: rgba(239, 240, 243, 100%);
  line-height: 20px;
}

.jdbc-collapse-container :deep(.ant-collapse-content)>.ant-collapse-content-box {
  padding: 0 14px 14px;
}

.ant-input-textarea,
.ant-input-affix-wrapper,
.ant-input,
:deep(.select-input-wrapper) {
  background-color: #fff;
}

.ant-badge {
  color: inherit;
}

:deep(.ant-badge-count) {
  top: -2px;
  right: -5px;
}

.ant-tabs> :deep(.ant-tabs-nav) {
  padding-top: 10px;
}

.ant-tabs :deep(.ant-tabs-extra-content) {
  transform: translateY(-4px);
}

.ant-btn-link {
  height: 24px;
  padding: 0;
  background-color: transparent;
  line-height: 24px;
}

.ant-btn-link:hover,
.ant-btn-link:active,
.ant-btn-link:visited,
.ant-btn-link:focus {
  background-color: transparent;
}

.ant-select:not(.ant-select-disabled) {
  background-color: #fff;
}
</style>
