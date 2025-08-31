<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints, Icon, IconRequired, Input, notification, SelectInput, Toggle, Tooltip, Validate } from '@xcan-angus/vue-ui';
import { isEqual } from 'lodash-es';
import { variable } from '@/api/tester';
import { ExtractionMethod, Encoding, ExtractionFileType } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import SelectEnum from '@/components/selectEnum/index.vue';
import { FileVariableFormState, VariableItem } from '../types';

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

const confirmLoading = ref(false);
const activeKey = ref<'value' | 'preview' | 'use'>('value');

const variableName = ref<string>('');
const variableNameError = ref(false);
const description = ref<string>('');
const filePath = ref<string>('');
const fileType = ref<ExtractionFileType>('CSV');
const encoding = ref<Encoding>('UTF-8');
const rowIndex = ref<string>('0');
const columnIndex = ref<string>('0');
const separatorChar = ref<string>(',');
const escapeChar = ref<string>('\\');
const quoteChar = ref<string>('"');

const method = ref<ExtractionMethod>(ExtractionMethod.EXACT_VALUE);
const defaultValue = ref<string>('');
const expression = ref<string>('');
const matchItem = ref<string>('');

const previewData = ref<{
  name: string;
  extraction: {
    source: 'FILE';
    fileType: ExtractionFileType;
    path: string;
    encoding: string;
    quoteChar: string;
    escapeChar: string;
    separatorChar: string;
    rowIndex: string;
    columnIndex: string;
    method: ExtractionMethod;
    defaultValue: string;
    expression: string;
    matchItem: string;
  };
}>();

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

const validName = (name:string) => {
  // eslint-disable-next-line prefer-regex-literals
  const rex = new RegExp(/[^a-zA-Z0-9!$%^&*_\-+=./]/);
  if (rex.test(name)) {
    variableNameError.value = true;
    return false;
  }

  return true;
};

const buttonGroupClick = (key: 'ok' | 'delete' |'export'| 'clone' | 'copyLink' | 'refresh') => {
  if (key === 'ok') {
    ok();
    return;
  }

  if (key === 'delete') {
    emit('delete', variableId.value);
    return;
  }

  if (key === 'export') {
    emit('clone', variableId.value);
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

  notification.success(t('dataVariable.detail.fileVariable.notifications.editSuccess'));
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

  notification.success(t('dataVariable.detail.fileVariable.notifications.addSuccess'));
  const id = res?.data?.id;
  emit('ok', { ...params, id }, false);
};

const getParams = (): FileVariableFormState => {
  const params: FileVariableFormState = {
    projectId: props.projectId,
    name: variableName.value,
    description: description.value,
    passwordValue: false,
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
      name: variableName.value,
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

const variableId = computed(() => {
  return props.dataSource?.id || '';
});

const editFlag = computed(() => {
  return !!props.dataSource?.id;
});

const okButtonDisabled = computed(() => {
  let disabled = !variableName.value ||
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

  <div class="flex items-start mb-3.5">
    <div class="flex items-center flex-shrink-0 mr-2.5 leading-7">
      <IconRequired />
      <span>{{ t('dataVariable.detail.fileVariable.name') }}</span>
    </div>
    <Validate
      class="flex-1"
      :text="t('dataVariable.detail.fileVariable.nameSupport')"
      mode="error"
      :error="variableNameError">
      <Input
        v-model:value="variableName"
        :maxlength="100"
        :error="variableNameError"
        dataType="mixin-en"
        excludes="{}"
        includes="\!\$%\^&\*_\-+=\.\/"
        :placeholder="t('dataVariable.detail.fileVariable.namePlaceholder')"
        trimAll
        @change="nameChange"
        @blur="nameBlur" />
    </Validate>
  </div>

  <div class="flex items-start">
    <div class="mr-2.5 flex items-center flex-shrink-0 transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>{{ t('dataVariable.detail.fileVariable.description') }}</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{ minRows: 3, maxRows: 5 }"
      showCount
      type="textarea"
      class="flex-1"
      :placeholder="t('dataVariable.detail.fileVariable.descriptionPlaceholder')"
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
          <span>{{ t('dataVariable.detail.fileVariable.extract') }}</span>
        </div>
      </template>

      <div>
        <Hints class="mb-2.5" :text="t('dataVariable.detail.fileVariable.extractHint')" />

        <Toggle :title="t('dataVariable.detail.fileVariable.readConfig')" class="text-3 leading-5 mb-3.5">
          <div class="flex items-center mb-3.5">
            <div class="w-16 flex-shrink-0">
              <IconRequired />
              <span>{{ t('dataVariable.detail.fileVariable.filePath') }}</span>
            </div>
            <Input
              v-model:value="filePath"
              :maxlength="800"
              style="width:calc(100% - 82px);"
              :placeholder="t('dataVariable.detail.fileVariable.filePathPlaceholder')"
              trimAll />
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.fileType') }}</span>
              </div>
              <SelectEnum
                v-model:value="fileType"
                enumKey="ExtractionFileType"
                class="w-full-20.5 " />
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.fileEncoding') }}</span>
              </div>
              <SelectInput
                v-model:value="encoding"
                :options="encodingOptions"
                :inputProps="inputProps"
                class="w-full-20.5 " />
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.readStartRow') }}</span>
              </div>
              <Input
                v-model:value="rowIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataVariable.detail.fileVariable.readStartRowPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.fileVariable.readStartRowTooltip')" class="text-tips ml-1 text-3.5 cursor-pointer">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.readStartColumn') }}</span>
              </div>
              <Input
                v-model:value="columnIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataVariable.detail.fileVariable.readStartColumnPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.fileVariable.readStartColumnTooltip')" class="text-tips ml-1 text-3.5 cursor-pointer">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.separator') }}</span>
              </div>
              <Input
                v-model:value="separatorChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.fileVariable.separatorTooltip')" class="text-tips ml-1 text-3.5 cursor-pointer">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.escapeChar') }}</span>
              </div>
              <Input
                v-model:value="escapeChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.fileVariable.escapeCharTooltip')" class="text-tips ml-1 text-3.5 cursor-pointer">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.quoteChar') }}</span>
              </div>
              <Input
                v-model:value="quoteChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.fileVariable.quoteCharTooltip')" class="text-tips ml-1 text-3.5 cursor-pointer">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>
        </Toggle>

        <Toggle :title="t('dataVariable.detail.fileVariable.extractConfig')" class="text-3 leading-5">
          <template v-if="method === 'EXACT_VALUE'">
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired />
                  <span>{{ t('dataVariable.detail.fileVariable.extractMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataVariable.detail.fileVariable.extractMethodPlaceholder')"
                  class="w-full-20.5 " />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataVariable.detail.fileVariable.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataVariable.detail.fileVariable.defaultValuePlaceholder')"
                  class="w-full-20.5 "
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
                  <span>{{ t('dataVariable.detail.fileVariable.extractMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataVariable.detail.fileVariable.extractMethodPlaceholder')"
                  class="w-full-20.5 " />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired />
                  <span>{{ t('dataVariable.detail.fileVariable.expression') }}</span>
                </div>
                <Input
                  v-model:value="expression"
                  :placeholder="t('dataVariable.detail.fileVariable.expressionPlaceholder')"
                  class="w-full-20.5 "
                  trimAll />
              </div>
            </div>

            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataVariable.detail.fileVariable.matchItem') }}</span>
                </div>
                <Input
                  v-model:value="matchItem"
                  :placeholder="t('dataVariable.detail.fileVariable.matchItemPlaceholder')"
                  class="w-full-20.5 "
                  dataType="number"
                  trimAll
                  :max="2000"
                  :maxlength="4" />
                <MatchItemPopover />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataVariable.detail.fileVariable.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataVariable.detail.fileVariable.defaultValuePlaceholder')"
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
          <span>{{ t('dataVariable.detail.fileVariable.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <TabPane v-if="variableId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.fileVariable.use') }}</span>
        </div>
      </template>

      <VariableUseList :id="variableId" />
    </TabPane>
  </Tabs>
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

.w-full-20\.5 {
  width: calc(100% - 82px);
}
</style>
