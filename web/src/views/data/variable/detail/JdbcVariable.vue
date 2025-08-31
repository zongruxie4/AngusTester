<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Hints, Icon, IconRequired, Input, notification, Toggle, Tooltip } from '@xcan-angus/vue-ui';
import { isEqual } from 'lodash-es';
import { variable } from '@/api/tester';

import SelectEnum from '@/components/selectEnum/index.vue';
import { JdbcVariableFormState, VariableItem } from '../types';

const { t } = useI18n();

type Props = {
  projectId: string;
  userInfo: { id: string; };
  dataSource?: VariableItem;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'ok', data: VariableItem, isEdit: boolean): void;
  (e: 'delete', value: string): void;
  (e: 'export', value: string): void;
  (e: 'clone', value: string): void;
  (e: 'copyLink', value: string): void;
  (e: 'refresh', value: string): void;
}>();

const ButtonGroup = defineAsyncComponent(() => import('@/views/data/variable/detail/ButtonGroup.vue'));
const PreviewData = defineAsyncComponent(() => import('@/views/data/variable/detail/PreviewData.vue'));
const VariableUseList = defineAsyncComponent(() => import('@/views/data/variable/detail/UseList.vue'));
const MatchItemPopover = defineAsyncComponent(() => import('@/views/data/variable/detail/MatchItemPopover.vue'));
const SelectDataSourceModal = defineAsyncComponent(() => import('@/views/data/variable/detail/jdbc/SelectDatasource.vue'));

const modalVisible = ref(false);

const confirmLoading = ref(false);
const activeKey = ref<'value' | 'preview' | 'use'>('value');

const variableName = ref<string>('');
const variableNameError = ref(false);
const description = ref<string>('');

const dbType = ref<string>();
const jdbcUrl = ref<string>('');
const username = ref<string>('');
const password = ref<string>('');
const selectSqlString = ref<string>('');
const rowIndex = ref<string>('0');
const columnIndex = ref<string>('0');

const method = ref<'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH'>('EXACT_VALUE');
const defaultValue = ref<string>('');
const expression = ref<string>('');
const matchItem = ref<string>('');

const previewData = ref<{
  name: string;
  extraction: {
    source: 'JDBC';
    method: 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
    expression: string;
    defaultValue: string;
    matchItem: string;
    datasource: {
      type: string | undefined;
      username: string;
      password: string;
      jdbcUrl: string;
    };
    select: string;
    rowIndex: string;
    columnIndex: string;
  };
}>();

const toSelectDataSource = () => {
  modalVisible.value = true;
};

const selectedDataSourceOk = (data) => {
  dbType.value = data.database;
  jdbcUrl.value = data.jdbcUrl;
  username.value = data.username;
  password.value = data.password;
};

const nameChange = () => {
  variableNameError.value = false;
};

const nameBlur = (event: { target: { value: string; } }) => {
  const name = event.target.value;
  if (!name) {
    return;
  }

  validName(name);
};

const validName = (name: string) => {
  // eslint-disable-next-line prefer-regex-literals
  const rex = new RegExp(/[^a-zA-Z0-9!$%^&*_\-+=./]/);
  if (rex.test(name)) {
    variableNameError.value = true;
    return false;
  }

  return true;
};

const buttonGroupClick = (key: 'ok' | 'delete' | 'export' | 'clone' | 'copyLink' | 'refresh') => {
  if (key === 'ok') {
    ok();
    return;
  }

  if (key === 'delete') {
    emit('delete', variableId.value);
    return;
  }

  if (key === 'export') {
    emit('export', variableId.value);
    return;
  }

  if (key === 'clone') {
    emit('clone', variableId.value);
    return;
  }

  if (key === 'copyLink') {
    emit('copyLink', variableId.value);
    return;
  }

  if (key === 'refresh') {
    emit('refresh', variableId.value);
  }
};

const ok = async () => {
  if (!validName(variableName.value)) {
    return;
  }

  if (editFlag.value) {
    toEdit();
    return;
  }

  toCreate();
};

const toEdit = async () => {
  const params = getParams();
  confirmLoading.value = true;
  const [error] = await variable.putVariables(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('dataVariable.detail.jdbcVariable.notifications.editSuccess'));
  emit('ok', params, true);
};

const toCreate = async () => {
  const params = getParams();
  confirmLoading.value = true;
  const [error, res] = await variable.addVariables(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('dataVariable.detail.jdbcVariable.notifications.addSuccess'));
  const id = res?.data?.id;
  emit('ok', { ...params, id }, false);
};

const getParams = (): JdbcVariableFormState => {
  const params: JdbcVariableFormState = {
    projectId: props.projectId,
    name: variableName.value,
    description: description.value,
    passwordValue: false,
    extraction: {
      source: 'JDBC',
      method: method.value,
      defaultValue: defaultValue.value,
      expression: expression.value,
      matchItem: matchItem.value,
      select: selectSqlString.value,
      rowIndex: rowIndex.value,
      columnIndex: columnIndex.value,
      datasource: {
        type: dbType.value,
        jdbcUrl: jdbcUrl.value,
        username: username.value,
        password: password.value
      }
    }
  };

  const id = variableId.value;
  if (id) {
    params.id = id;
  }

  return params;
};

const initialize = () => {
  const data = props.dataSource;
  if (!data) {
    return;
  }

  const { extraction } = data || {};
  variableName.value = data.name;
  description.value = data.description;

  const datasource = extraction.datasource;
  dbType.value = datasource.type?.value;
  jdbcUrl.value = datasource.jdbcUrl;
  username.value = datasource.username;
  password.value = datasource.password;

  selectSqlString.value = extraction.select;
  rowIndex.value = extraction.rowIndex;
  columnIndex.value = extraction.columnIndex;

  defaultValue.value = extraction.defaultValue;
  expression.value = extraction.expression;
  matchItem.value = extraction.matchItem;
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (!newValue) {
      return;
    }

    initialize();
  }, { immediate: true });

  watch(() => activeKey.value, (newValue) => {
    if (newValue !== 'preview') {
      return;
    }

    const newData = {
      name: variableName.value,
      extraction: {
        source: 'JDBC',
        method: method.value,
        expression: expression.value,
        defaultValue: defaultValue.value,
        matchItem: matchItem.value,
        datasource: {
          type: dbType.value,
          username: username.value,
          password: password.value,
          jdbcUrl: jdbcUrl.value
        },
        select: selectSqlString.value,
        rowIndex: rowIndex.value,
        columnIndex: columnIndex.value
      }
    };

    if (!isEqual(newData, previewData.value)) {
      previewData.value = newData;
    }
  }, { immediate: true });
});

const variableId = computed(() => {
  return props.dataSource?.id || '';
});

const editFlag = computed(() => {
  return !!props.dataSource?.id;
});

const okButtonDisabled = computed(() => {
  let disabled = !variableName.value ||
    !dbType.value ||
    !jdbcUrl.value ||
    !username.value ||
    !password.value ||
    !selectSqlString.value ||
    !rowIndex.value ||
    !columnIndex.value ||
    !method.value;

  if (!disabled) {
    if (['JSON_PATH', 'REGEX', 'X_PATH'].includes(method.value)) {
      disabled = !expression.value;
    }
  }
  return disabled;
});
</script>
<template>
  <ButtonGroup
    :editFlag="editFlag"
    :okButtonDisabled="okButtonDisabled"
    class="mb-5"
    @click="buttonGroupClick" />

  <div class="flex items-start mb-3.5">
    <div class="flex items-center flex-shrink-0 mr-2.5 leading-7">
      <IconRequired />
      <span>{{ t('dataVariable.detail.jdbcVariable.name') }}</span>
    </div>
    <Validate
      class="flex-1"
      :text="t('dataVariable.detail.jdbcVariable.nameSupport')"
      mode="error"
      :error="variableNameError">
      <Input
        v-model:value="variableName"
        :maxlength="100"
        :error="variableNameError"
        dataType="mixin-en"
        excludes="{}"
        includes="\!\$%\^&\*_\-+=\.\/"
        :placeholder="t('dataVariable.detail.jdbcVariable.namePlaceholder')"
        trimAll
        @change="nameChange"
        @blur="nameBlur" />
    </Validate>
  </div>

  <div class="flex items-start">
    <div class="mr-2.5 flex items-center flex-shrink-0 transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>{{ t('dataVariable.detail.jdbcVariable.description') }}</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{ minRows: 3, maxRows: 5 }"
      showCount
      type="textarea"
      class="flex-1"
      :placeholder="t('dataVariable.detail.jdbcVariable.descriptionPlaceholder')"
      trim />
  </div>

  <Tabs
    v-model:activeKey="activeKey"
    size="small"
    class="ant-tabs-nav-mb-2.5">
    <TabPane key="value">
      <template #tab>
        <div class="flex items-center font-normal">
          <IconRequired />
          <span>{{ t('dataVariable.detail.jdbcVariable.extract') }}</span>
        </div>
      </template>

      <div>
        <Hints class="mb-2.5" :text="t('dataVariable.detail.jdbcVariable.hints')" />

        <Toggle :title="t('dataVariable.detail.jdbcVariable.readConfig')" class="text-3 leading-5 mb-3.5">
          <div class="flex items-center justify-start mb-3.5">
            <div class="w-19.5 flex-shrink-0">
            </div>
            <Button
              type="link"
              size="small"
              class="flex items-center p-0 border-none h-3.5 leading-3.5 space-x-1"
              @click="toSelectDataSource">
              <Icon icon="icon-xuanze" class="text-3.5" />
              <span>{{ t('dataVariable.detail.jdbcVariable.selectDataSource') }}</span>
            </Button>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-19.5 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.jdbcVariable.databaseType') }}</span>
              </div>
              <SelectEnum
                v-model:value="dbType"
                 enumKey="DatabaseType"
                :placeholder="t('dataVariable.detail.jdbcVariable.databaseTypePlaceholder')"
                class="w-full-24" />
            </div>

            <div class="flex items-center mb-3.5">
              <div class="w-19.5 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.jdbcVariable.jdbcUrl') }}</span>
              </div>
              <Input
                v-model:value="jdbcUrl"
                :maxlength="2048"
                class="w-full-24"
                :placeholder="t('dataVariable.detail.jdbcVariable.jdbcUrlPlaceholder')"
                trimAll />
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-19.5 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.jdbcVariable.username') }}</span>
              </div>
              <Input
                v-model:value="username"
                class="w-full-24"
                :maxlength="200"
                :placeholder="t('dataVariable.detail.jdbcVariable.usernamePlaceholder')"
                trimAll />
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.jdbcVariable.password') }}</span>
              </div>
              <Input
                v-model:value="password"
                :maxlength="800"
                class="w-full-20.5"
                :placeholder="t('dataVariable.detail.jdbcVariable.passwordPlaceholder')"
                trimAll />
            </div>
          </div>

          <div class="flex items-start mb-3.5">
            <div class="w-19.5 flex-shrink-0 transform-gpu translate-y-1">
              <IconRequired />
              <span>{{ t('dataVariable.detail.jdbcVariable.selectStatement') }}</span>
            </div>
            <Input
              v-model:value="selectSqlString"
              :placeholder="t('dataVariable.detail.jdbcVariable.selectStatementPlaceholder')"
              class="w-full-24"
              type="textarea"
              trim
              :autoSize="{ minRows: 5, maxRows: 8 }"
              :maxlength="1024" />
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-19.5 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.jdbcVariable.readStartRow') }}</span>
              </div>
              <Input
                v-model:value="rowIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataVariable.detail.jdbcVariable.readStartRowPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.jdbcVariable.readStartRowTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.jdbcVariable.readStartColumn') }}</span>
              </div>
              <Input
                v-model:value="columnIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataVariable.detail.jdbcVariable.readStartColumnPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.jdbcVariable.readStartColumnTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>
        </Toggle>

        <Toggle :title="t('dataVariable.detail.jdbcVariable.extractConfig')" class="text-3 leading-5">
          <template v-if="method === 'EXACT_VALUE'">
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-19.5 flex-shrink-0">
                  <IconRequired />
                  <span>{{ t('dataVariable.detail.jdbcVariable.extractMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataVariable.detail.jdbcVariable.extractMethodPlaceholder')"
                  class="w-full-24 " />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataVariable.detail.jdbcVariable.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataVariable.detail.jdbcVariable.defaultValuePlaceholder')"
                  class="w-full-20.5"
                  trim
                  :maxlength="4096" />
              </div>
            </div>
          </template>

          <template v-else>
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-19.5 flex-shrink-0">
                  <IconRequired />
                  <span>{{ t('dataVariable.detail.jdbcVariable.extractMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataVariable.detail.jdbcVariable.extractMethodPlaceholder')"
                  class="w-full-24" />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired />
                  <span>{{ t('dataVariable.detail.jdbcVariable.expression') }}</span>
                </div>
                <Input
                  v-model:value="expression"
                  :placeholder="t('dataVariable.detail.jdbcVariable.expressionPlaceholder')"
                  class="w-full-20.5 "
                  trimAll />
              </div>
            </div>

            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-19.5 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataVariable.detail.jdbcVariable.matchItem') }}</span>
                </div>
                <Input
                  v-model:value="matchItem"
                  :placeholder="t('dataVariable.detail.jdbcVariable.matchItemPlaceholder')"
                  class="w-full-24"
                  dataType="number"
                  trimAll
                  :max="2000"
                  :maxlength="4" />
                <MatchItemPopover />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataVariable.detail.jdbcVariable.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataVariable.detail.jdbcVariable.defaultValuePlaceholder')"
                  class="w-full-20.5 "
                  trim
                  :maxlength="4096" />
              </div>
            </div>
          </template>
        </Toggle>
      </div>
    </TabPane>

    <TabPane key="preview">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.jdbcVariable.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <TabPane v-if="variableId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.jdbcVariable.use') }}</span>
        </div>
      </template>

      <VariableUseList :id="variableId" />
    </TabPane>
  </Tabs>

  <SelectDataSourceModal
    :projectId="props.projectId"
    :userInfo="props.userInfo"
    v-model:visible="modalVisible"
    @ok="selectedDataSourceOk" />
</template>

<style scoped>
:deep(.toggle-title) {
  color: var(--content-text-content);
  font-size: 12px;
  font-weight: normal;
}

:deep(.ant-collapse-ghost)>.ant-collapse-item>.ant-collapse-content {
  margin-top: 14px;
}

:deep(.ant-collapse) .ant-collapse-content>.ant-collapse-content-box {
  padding-left: 22px;
}

.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.w-full-24 {
  width: calc(100% - 96px);
}

.w-full-20\.5 {
  width: calc(100% - 82px);
}
</style>
