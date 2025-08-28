<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints, Icon, IconRequired, Input, notification, SelectInput, Toggle, Tooltip } from '@xcan-angus/vue-ui';
import { cloneDeep, isEqual } from 'lodash-es';
import { dataSet } from '@/api/tester';

import SelectEnum from '@/components/selectEnum/index.vue'
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

  notification.success('数据集修改成功');
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

  notification.success('数据集添加成功');
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
      <span>名称</span>
    </div>
    <Input
      v-model:value="dataSetName"
      :maxlength="100"
      placeholder="数据集名称，最长100个字符"
      trimAll
      excludes="{}" />
  </div>

  <div class="flex items-start">
    <div class="mr-2.5 flex items-center flex-shrink-0 transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>描述</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{ minRows: 3, maxRows: 5 }"
      showCount
      type="textarea"
      class="flex-1"
      placeholder="数据集描述，最长200个字符"
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
          <span>提取</span>
        </div>
      </template>

      <div>
        <Hints class="mb-2.5" text="从文件中读取数据集，每个数据集最大允许添加200个参数，每个参数文件最大不超过500MB，总行数不超过100万行。" />

        <Toggle title="参数名称" class="text-3 leading-5 mb-3.5 params-container">
          <ParameterNameInput
            ref="parametersRef"
            :columnIndex="columnIndex"
            :defaultValue="defaultParameters"
            @change="parametersChange" />
        </Toggle>

        <Toggle title="读取配置" class="text-3 leading-5 mb-3.5">
          <div class="flex items-center mb-3.5">
            <div class="w-16 flex-shrink-0">
              <IconRequired />
              <span>文件路径</span>
            </div>
            <Input
              v-model:value="filePath"
              :maxlength="800"
              style="width:calc(100% - 82px);"
              placeholder="文件本地路径或文件URL，最长800个字符"
              trimAll />
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>文件类型</span>
              </div>
              <SelectEnum
                v-model:value="fileType"
                enumKey="ExtractionFileType"
                class="w-full-20.5" />
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>文件编码</span>
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
                <span>读开始行</span>
              </div>
              <Input
                v-model:value="rowIndex"
                :maxlength="4"
                dataType="number"
                placeholder="读取行索引，默认从0开始"
                trimAll />
              <Tooltip title="读取参数值开始行，默认索引基于0开始，即读取第一行。注意：第一行为参数名标题行时，通常需要从索引1即第二行读取数据。">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>读开始列</span>
              </div>
              <Input
                v-model:value="columnIndex"
                :maxlength="4"
                dataType="number"
                placeholder="读取列索引，默认从0开始"
                trimAll />
              <Tooltip title="读取参数开始列，默认索引基于0开始，即读取第一列。">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>分隔符</span>
              </div>
              <Input
                v-model:value="separatorChar"
                :maxlength="1"
                trimAll />
              <Tooltip title="Csv类型文件参数。用于分隔CSV文件中的不同字段或数据列，默认值为“,”。">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>转义符</span>
              </div>
              <Input
                v-model:value="escapeChar"
                :maxlength="1"
                trimAll />
              <Tooltip title="Csv类型文件参数，用于转义字段中的特殊字符，特别是将引号字符本身作为文字字符包含在内，默认为“\”。">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-16 flex-shrink-0">
                <IconRequired />
                <span>引用符</span>
              </div>
              <Input
                v-model:value="quoteChar"
                :maxlength="1"
                trimAll />
              <Tooltip title="Csv类型文件参数。用于表示CSV文件中字段的开始和结束，特别是当字段包含分隔符（例如逗号）或换行符等特殊字符时，默认为“\”。">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>
        </Toggle>

        <Toggle title="提取配置" class="text-3 leading-5">
          <template v-if="method === 'EXACT_VALUE'">
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired />
                  <span>提取方式</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  placeholder="提取方式"
                  class="w-full-20.5" />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>缺省值</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  placeholder="缺省值，最长4096个字符"
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
                  <span>提取方式</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  placeholder="提取方式"
                  class="w-full-20.5" />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired />
                  <span>表达式</span>
                </div>
                <Input
                  v-model:value="expression"
                  placeholder="表达式，最长1024个字符"
                  class="w-full-20.5"
                  trimAll />
              </div>
            </div>

            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-16 flex-shrink-0">
                  <IconRequired class="invisible" />
                  <span>匹配项</span>
                </div>
                <Input
                  v-model:value="matchItem"
                  placeholder="匹配项，范围0 ~ 2000"
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
                  <span>缺省值</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  placeholder="缺省值，最长4096个字符"
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
          <span>预览</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <TabPane v-if="dataSetId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>使用</span>
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
