<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Hints, Icon, IconRequired, Input, notification, Toggle, Tooltip } from '@xcan-angus/vue-ui';
import { cloneDeep, isEqual } from 'lodash-es';
import { dataSet } from '@/api/tester';
import { ExtractionMethod } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

import SelectEnum from '@/components/selectEnum/index.vue';
import { JdbcDatasetFormState, DataSetItem } from '../types';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  visible: boolean;
  dataSource?: DataSetItem;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  visible: false,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'ok', data: DataSetItem, isEdit: boolean): void;
  (e: 'delete', value: string): void;
  (e: 'export', value: string): void;
  (e: 'clone', value: string): void;
  (e: 'copyLink', value: string): void;
  (e: 'refresh', value: string): void;
}>();

const ButtonGroup = defineAsyncComponent(() => import('@/views/data/dataset/detail/ButtonGroup.vue'));
const ParameterNameInput = defineAsyncComponent(() => import('@/views/data/dataset/detail/ParameterNameInput.vue'));
const PreviewData = defineAsyncComponent(() => import('@/views/data/dataset/preview/index.vue'));
const DataSetUseList = defineAsyncComponent(() => import('@/views/data/dataset/detail/UseList.vue'));
const MatchItemPopover = defineAsyncComponent(() => import('@/views/data/dataset/detail/MatchItemPopover.vue'));
const SelectDataSourceModal = defineAsyncComponent(() => import('@/views/data/variable/detail/jdbc/SelectDatasource.vue'));

const parametersRef = ref();

const confirmLoading = ref(false);
const activeKey = ref<'value' | 'preview' | 'use'>('value');

const dataSetName = ref<string>('');
const description = ref<string>('');

const parameters = ref<{ name: string; }[]>([]);
const defaultParameters = ref<{ name: string; }[]>([]);

const dbType = ref<string>();
const jdbcUrl = ref<string>('');
const username = ref<string>('');
const password = ref<string>('');
const selectSqlString = ref<string>('');
const rowIndex = ref<string>('0');
const columnIndex = ref<string>('0');

const method = ref<ExtractionMethod>(ExtractionMethod.EXACT_VALUE);
const defaultValue = ref<string>('');
const expression = ref<string>('');
const matchItem = ref<string>('');

const previewData = ref<{
  id: string | undefined;
  projectId: string;
  name: string;
  parameters: { name: string }[];
  extraction: {
    source: 'JDBC';
    method: ExtractionMethod;
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

const modalVisible = ref(false);

const toSelectDataSource = () => {
  modalVisible.value = true;
};

const selectedDataSourceOk = (data) => {
  dbType.value = data.database;
  jdbcUrl.value = data.jdbcUrl;
  username.value = data.username;
  password.value = data.password;
};

const buttonGroupClick = (key: 'ok' | 'delete' | 'export' | 'clone' | 'copyLink' | 'refresh') => {
  if (key === 'ok') {
    ok();
    return;
  }

  if (key === 'delete') {
    emit('delete', dataSetId.value);
    return;
  }

  if (key === 'export') {
    emit('export', dataSetId.value);
    return;
  }

  if (key === 'clone') {
    emit('clone', dataSetId.value);
    return;
  }

  if (key === 'copyLink') {
    emit('copyLink', dataSetId.value);
    return;
  }

  if (key === 'refresh') {
    emit('refresh', dataSetId.value);
  }
};

const ok = async () => {
  let validFlag = true;
  if (typeof parametersRef.value?.isValid === 'function') {
    validFlag = parametersRef.value.isValid();
  }

  if (!validFlag) {
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
  const [error] = await dataSet.putDataSet(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('dataset.detail.jdbcDataset.notifications.updateSuccess'));
  emit('ok', params, true);
};

const toCreate = async () => {
  const params = getParams();
  confirmLoading.value = true;
  const [error, res] = await dataSet.addDataSet(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('dataset.detail.jdbcDataset.notifications.addSuccess'));
  const id = res?.data?.id;
  emit('ok', { ...params, id }, false);
};

const parametersChange = (data: { name: string; }[]) => {
  parameters.value = data;
};

const getParams = (): JdbcDatasetFormState => {
  const params: JdbcDatasetFormState = {
    projectId: props.projectId,
    name: dataSetName.value,
    description: description.value,
    parameters: parameters.value,
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

  const id = dataSetId.value;
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
  dataSetName.value = data.name;
  description.value = data.description;
  parameters.value = data.parameters || [];
  defaultParameters.value = cloneDeep(data.parameters);

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
      id: props.dataSource?.id,
      projectId: props.projectId,
      name: dataSetName.value,
      parameters: parameters.value,
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

const dataSetId = computed(() => {
  return props.dataSource?.id || '';
});

const editFlag = computed(() => {
  return !!props.dataSource?.id;
});

const okButtonDisabled = computed(() => {
  let disabled = !dataSetName.value ||
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

  <div class="flex items-center mb-3.5">
    <div class="mr-2.5 flex-shrink-0">
      <IconRequired />
      <span>{{ t('dataset.detail.jdbcDataset.form.name') }}</span>
    </div>
    <Input
      v-model:value="dataSetName"
      :maxlength="100"
      :placeholder="t('dataset.detail.jdbcDataset.form.namePlaceholder')"
      trimAll
      excludes="{}" />
  </div>

  <div class="flex items-start">
    <div class="mr-2.5 flex items-center flex-shrink-0 transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>{{ t('dataset.detail.jdbcDataset.form.description') }}</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{ minRows: 3, maxRows: 5 }"
      showCount
      type="textarea"
      class="flex-1"
      :placeholder="t('dataset.detail.jdbcDataset.form.descriptionPlaceholder')"
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
          <span>{{ t('dataset.detail.jdbcDataset.form.extraction') }}</span>
        </div>
      </template>

      <div>
        <Hints class="mb-2.5" :text="t('dataset.detail.jdbcDataset.form.hints')" />

        <Toggle :title="t('dataset.detail.jdbcDataset.form.parameterName')" class="text-3 leading-5 mb-3.5 params-container">
          <ParameterNameInput
            ref="parametersRef"
            :columnIndex="columnIndex"
            :defaultValue="defaultParameters"
            @change="parametersChange" />
        </Toggle>

        <Toggle :title="t('dataset.detail.jdbcDataset.form.readConfig')" class="text-3 leading-5 mb-3.5">
          <div class="flex items-center justify-start mb-3.5">
            <div class="w-19.5 flex-shrink-0">
            </div>
            <Button
              type="link"
              size="small"
              class="flex items-center p-0 border-none h-3.5 leading-3.5 space-x-1"
              @click="toSelectDataSource">
              <Icon icon="icon-xuanze" class="text-3.5" />
              <span>{{ t('dataset.detail.jdbcDataset.form.selectDataSource') }}</span>
            </Button>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-19.5 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataset.detail.jdbcDataset.form.databaseType') }}</span>
              </div>
              <SelectEnum
                v-model:value="dbType"
                enumKey="DatabaseType"
                :placeholder="t('dataset.detail.jdbcDataset.form.databaseTypePlaceholder')"
                class="w-full-24" />
            </div>
          </div>

          <div class="flex items-center mb-3.5">
            <div class="w-19.5 flex-shrink-0">
              <IconRequired />
              <span>JdbcUrl</span>
            </div>
            <Input
              v-model:value="jdbcUrl"
              placeholder="Jdbc URL，最长2048个字符"
              class="w-full-24"
              trimAll
              :maxlength="2048" />
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-19.5 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataset.detail.jdbcDataset.form.username') }}</span>
              </div>
              <Input
                v-model:value="username"
                :placeholder="t('dataset.detail.jdbcDataset.form.usernamePlaceholder')"
                class="w-full-24"
                trim
                :maxlength="200" />
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataset.detail.jdbcDataset.form.password') }}</span>
              </div>
              <Input
                v-model:value="password"
                :placeholder="t('dataset.detail.jdbcDataset.form.passwordPlaceholder')"
                class="w-full-20.5"
                trim
                :maxlength="800" />
            </div>
          </div>

          <div class="flex items-start mb-3.5">
            <div class="w-19.5 flex-shrink-0 transform-gpu translate-y-1">
              <IconRequired />
              <span>{{ t('dataset.detail.jdbcDataset.form.selectStatement') }}</span>
            </div>
            <Input
              v-model:value="selectSqlString"
              :placeholder="t('dataset.detail.jdbcDataset.form.selectStatementPlaceholder')"
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
                <span>{{ t('dataset.detail.jdbcDataset.form.readStartRow') }}</span>
              </div>
              <Input
                v-model:value="rowIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataset.detail.jdbcDataset.form.readStartRowPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataset.detail.jdbcDataset.form.readStartRowTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataset.detail.jdbcDataset.form.readStartColumn') }}</span>
              </div>
              <Input
                v-model:value="columnIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataset.detail.jdbcDataset.form.readStartColumnPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataset.detail.jdbcDataset.form.readStartColumnTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>
        </Toggle>

        <Toggle :title="t('dataset.detail.jdbcDataset.form.extractionConfig')" class="text-3 leading-5">
          <tempalte v-if="method === 'EXACT_VALUE'">
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-19.5 flex-shrink-0">
                  <IconRequired />
                  <span>{{ t('dataset.detail.jdbcDataset.form.extractionMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataset.detail.jdbcDataset.form.extractionMethodPlaceholder')"
                  class="w-full-24" />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataset.detail.jdbcDataset.form.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataset.detail.jdbcDataset.form.defaultValuePlaceholder')"
                  class="w-full-20.5"
                  trim
                  :maxlength="4096" />
              </div>
            </div>
          </tempalte>

          <template v-else>
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-19.5 flex-shrink-0">
                  <IconRequired />
                  <span>{{ t('dataset.detail.jdbcDataset.form.extractionMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataset.detail.jdbcDataset.form.extractionMethodPlaceholder')"
                  class="w-full-24" />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired />
                  <span>{{ t('dataset.detail.jdbcDataset.form.expression') }}</span>
                </div>
                <Input
                  v-model:value="expression"
                  :placeholder="t('dataset.detail.jdbcDataset.form.expressionPlaceholder')"
                  class="w-full-20.5"
                  trim
                  :maxlength="1024" />
              </div>
            </div>

            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-19.5 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataset.detail.jdbcDataset.form.matchItem') }}</span>
                </div>
                <Input
                  v-model:value="matchItem"
                  :placeholder="t('dataset.detail.jdbcDataset.form.matchItemPlaceholder')"
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
                  <span>{{ t('dataset.detail.jdbcDataset.form.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataset.detail.jdbcDataset.form.defaultValuePlaceholder')"
                  class="w-full-20.5"
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
          <span>{{ t('dataset.detail.jdbcDataset.tabs.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <TabPane v-if="dataSetId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataset.detail.jdbcDataset.tabs.use') }}</span>
        </div>
      </template>

      <DataSetUseList :id="dataSetId" />
    </TabPane>
  </Tabs>

  <SelectDataSourceModal
    v-model:visible="modalVisible"
    :projectId="props.projectId"
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
