<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints, Icon, IconRequired, Input, notification, SelectInput, Toggle, Tooltip } from '@xcan-angus/vue-ui';
import { cloneDeep, isEqual } from 'lodash-es';
import { dataSet } from '@/api/tester';

const { t } = useI18n();

import SelectEnum from '@/components/selectEnum/index.vue';
import { DataSetItem } from '../../PropsType';
import { FormState } from './PropsType';

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

const ButtonGroup = defineAsyncComponent(() => import('@/views/data/dataset/detail/buttonGroup/index.vue'));
const ParameterNameInput = defineAsyncComponent(() => import('@/views/data/dataset/detail/parameterNameInput/index.vue'));
const PreviewData = defineAsyncComponent(() => import('@/views/data/dataset/previewData/index.vue'));
const DataSetUseList = defineAsyncComponent(() => import('@/views/data/dataset/detail/useList/index.vue'));
const MatchItemPopover = defineAsyncComponent(() => import('@/views/data/dataset/detail/matchItemPopover/index.vue'));

const parametersRef = ref();

const confirmLoading = ref(false);
const activeKey = ref<'value' | 'preview' | 'use'>('value');

const dataSetName = ref<string>('');
const description = ref<string>('');

const parameters = ref<{ name: string; }[]>([]);
const defaultParameters = ref<{ name: string; }[]>([]);

const filePath = ref<string>('');
const fileType = ref<'CSV' | 'EXCEL' | 'TXT'>('CSV');
const encoding = ref<'UTF-8' | 'UTF-16' | 'UTF-16BE' | 'UTF-16LE' | 'US-ASCII' | 'ISO-8859-1'>('UTF-8');
const rowIndex = ref<string>('0');
const columnIndex = ref<string>('0');
const separatorChar = ref<string>(',');
const escapeChar = ref<string>('\\');
const quoteChar = ref<string>('"');

const method = ref<'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH'>('EXACT_VALUE');
const defaultValue = ref<string>('');
const expression = ref<string>('');
const matchItem = ref<string>('');

const previewData = ref<{
  id: string | undefined;
  projectId: string;
  name: string;
  parameters: { name: string }[];
  extraction: {
    source: 'FILE';
    fileType: 'CSV' | 'EXCEL' | 'TXT';
    path: string;
    encoding: string;
    quoteChar: string;
    escapeChar: string;
    separatorChar: string;
    rowIndex: string;
    columnIndex: string;
    method: 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
    defaultValue: string;
    expression: string;
    matchItem: string;
  };
}>();

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

  notification.success(t('dataset.detail.fileDataset.notifications.updateSuccess'));
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

  notification.success(t('dataset.detail.fileDataset.notifications.addSuccess'));
  const id = res?.data?.id;
  emit('ok', { ...params, id }, false);
};

const parametersChange = (data: { name: string; }[]) => {
  parameters.value = data;
};

const getParams = (): FormState => {
  const params: FormState = {
    projectId: props.projectId,
    name: dataSetName.value,
    description: description.value,
    parameters: parameters.value,
    extraction: {
      columnIndex: columnIndex.value,
      encoding: encoding.value,
      escapeChar: escapeChar.value,
      fileType: fileType.value,
      path: filePath.value,
      quoteChar: quoteChar.value,
      rowIndex: rowIndex.value,
      separatorChar: separatorChar.value,
      source: 'FILE',
      method: method.value,
      defaultValue: defaultValue.value,
      expression: expression.value,
      matchItem: matchItem.value
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

  filePath.value = extraction.path;
  filePath.value = extraction.path;
  fileType.value = extraction.fileType?.value;
  encoding.value = extraction.encoding;
  rowIndex.value = extraction.rowIndex;
  columnIndex.value = extraction.columnIndex;
  separatorChar.value = extraction.separatorChar;
  escapeChar.value = extraction.escapeChar;
  quoteChar.value = extraction.quoteChar;

  method.value = extraction.method?.value;
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
        source: 'FILE',
        fileType: fileType.value,
        path: filePath.value,
        encoding: encoding.value,
        quoteChar: quoteChar.value,
        escapeChar: escapeChar.value,
        separatorChar: separatorChar.value,
        rowIndex: rowIndex.value,
        columnIndex: columnIndex.value,
        method: method.value,
        defaultValue: defaultValue.value,
        expression: expression.value,
        matchItem: matchItem.value
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
    !filePath.value ||
    !fileType.value ||
    !encoding.value ||
    !rowIndex.value ||
    !columnIndex.value ||
    !separatorChar.value ||
    !escapeChar.value ||
    !quoteChar.value;

  if (!disabled) {
    if (['JSON_PATH', 'REGEX', 'X_PATH'].includes(method.value)) {
      disabled = !expression.value;
    }
  }

  return disabled;
});

const encodingOptions = [
  {
    label: 'UTF-8',
    value: 'UTF-8'
  },
  {
    label: 'UTF-16',
    value: 'UTF-16'
  },
  {
    label: 'UTF-16BE',
    value: 'UTF-16BE'
  },
  {
    label: 'UTF-16LE',
    value: 'UTF-16LE'
  },
  {
    label: 'US-ASCII',
    value: 'US-ASCII'
  },
  {
    label: 'ISO-8859-1',
    value: 'ISO-8859-1'
  }
];

const inputProps = {
  maxlength: 20,
  trimAll: true
};
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
      <span>{{ t('dataset.detail.fileDataset.form.name') }}</span>
    </div>
    <Input
      v-model:value="dataSetName"
      :maxlength="100"
      :placeholder="t('dataset.detail.fileDataset.form.namePlaceholder')"
      trimAll
      excludes="{}" />
  </div>

  <div class="flex items-start">
    <div class="mr-2.5 flex items-center flex-shrink-0 transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>{{ t('dataset.detail.fileDataset.form.description') }}</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{ minRows: 3, maxRows: 5 }"
      showCount
      type="textarea"
      class="flex-1"
      :placeholder="t('dataset.detail.fileDataset.form.descriptionPlaceholder')"
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
          <span>{{ t('dataset.detail.fileDataset.form.extraction') }}</span>
        </div>
      </template>

      <div>
        <Hints class="mb-2.5" :text="t('dataset.detail.fileDataset.form.hints')" />

        <Toggle :title="t('dataset.detail.fileDataset.form.parameterName')" class="text-3 leading-5 mb-3.5 params-container">
          <ParameterNameInput
            ref="parametersRef"
            :columnIndex="columnIndex"
            :defaultValue="defaultParameters"
            @change="parametersChange" />
        </Toggle>

        <Toggle :title="t('dataset.detail.fileDataset.form.readConfig')" class="text-3 leading-5 mb-3.5">
          <div class="flex items-center mb-3.5">
            <div class="w-16 flex-shrink-0">
              <IconRequired />
              <span>{{ t('dataset.detail.fileDataset.form.filePath') }}</span>
            </div>
            <Input
              v-model:value="filePath"
              :maxlength="800"
              style="width:calc(100% - 82px);"
              :placeholder="t('dataset.detail.fileDataset.form.filePathPlaceholder')"
              trimAll />
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.fileType') }}</span>
              </div>
              <SelectEnum
                v-model:value="fileType"
                enumKey="ExtractionFileType"
                class="w-full-20.5" />
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.fileEncoding') }}</span>
              </div>
              <SelectInput
                v-model:value="encoding"
                :options="encodingOptions"
                :inputProps="inputProps"
                class="w-full-20.5" />
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.readStartRow') }}</span>
              </div>
              <Input
                v-model:value="rowIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataset.detail.fileDataset.form.readStartRowPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataset.detail.fileDataset.form.readStartRowTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.readStartColumn') }}</span>
              </div>
              <Input
                v-model:value="columnIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataset.detail.fileDataset.form.readStartColumnPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataset.detail.fileDataset.form.readStartColumnTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.separator') }}</span>
              </div>
              <Input
                v-model:value="separatorChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataset.detail.fileDataset.form.separatorTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.escapeChar') }}</span>
              </div>
              <Input
                v-model:value="escapeChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataset.detail.fileDataset.form.escapeCharTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.quoteChar') }}</span>
              </div>
              <Input
                v-model:value="quoteChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataset.detail.fileDataset.form.quoteCharTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>
        </Toggle>

        <Toggle :title="t('dataset.detail.fileDataset.form.extractionConfig')" class="text-3 leading-5">
          <template v-if="method === 'EXACT_VALUE'">
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired />
                  <span>{{ t('dataset.detail.fileDataset.form.extractionMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataset.detail.fileDataset.form.extractionMethodPlaceholder')"
                  class="w-full-20.5" />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataset.detail.fileDataset.form.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataset.detail.fileDataset.form.defaultValuePlaceholder')"
                  class="w-full-20.5"
                  trim
                  :maxlength="4096" />
              </div>
            </div>
          </template>

          <template v-else>
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired />
                  <span>{{ t('dataset.detail.fileDataset.form.extractionMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataset.detail.fileDataset.form.extractionMethodPlaceholder')"
                  class="w-full-20.5" />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired />
                  <span>{{ t('dataset.detail.fileDataset.form.expression') }}</span>
                </div>
                <Input
                  v-model:value="expression"
                  :placeholder="t('dataset.detail.fileDataset.form.expressionPlaceholder')"
                  class="w-full-20.5"
                  trimAll />
              </div>
            </div>

            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataset.detail.fileDataset.form.matchItem') }}</span>
                </div>
                <Input
                  v-model:value="matchItem"
                  :placeholder="t('dataset.detail.fileDataset.form.matchItemPlaceholder')"
                  class="w-full-20.5"
                  dataType="number"
                  trimAll
                  :max="2000"
                  :maxlength="4" />
                <MatchItemPopover />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataset.detail.fileDataset.form.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataset.detail.fileDataset.form.defaultValuePlaceholder')"
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
          <span>{{ t('dataset.detail.fileDataset.tabs.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <TabPane v-if="dataSetId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataset.detail.fileDataset.tabs.use') }}</span>
        </div>
      </template>

      <DataSetUseList :id="dataSetId" />
    </TabPane>
  </Tabs>
</template>

<style scoped>
:deep(.toggle-title) {
  color: var(--content-text-content);
  font-size: 12px;
  font-weight: normal;
}

:deep(.ant-collapse)>.ant-collapse-item>.ant-collapse-content {
  margin-top: 14px;
}

:deep(.ant-collapse) .ant-collapse-content>.ant-collapse-content-box {
  padding-left: 22px;
}

:deep(.params-container.ant-collapse)>.ant-collapse-item>.ant-collapse-content {
  margin-top: 10px;
}

.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.w-full-20\.5 {
  width: calc(100% - 82px);
}
</style>
