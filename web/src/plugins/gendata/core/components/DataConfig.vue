<script setup lang="ts">
import { ref } from 'vue';
import { Form, FormItem, Checkbox } from 'ant-design-vue';
import { Input, Select, SelectEnum } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

export interface Props {
  format: string;
}

const props = withDefaults(defineProps<Props>(), {
  format: 'CSV',
  plugin: 'MockCsv'
});

const { t } = useI18n();

const emits = defineEmits<{(e: 'update:format', value: string):void; (e: 'update:plugin', value: string): void; (e: 'formatChange', value: string):void}>();

const formRef = ref();
const formState = ref({
  format: props.format,
  lineEnding: 'UNIT_LF',
  includeHeader: true,
  escapeChar: undefined, // 转义符
  quoteChar: undefined, // 引用符
  separatorChar: ',', // 分隔符
  includeNull: true,
  rowsToArray: true, // 是否将多个 JSON 对象（行）转换为数组。
  tableName: undefined,
  batchInsert: false, // 是否生成批量 INSERT，设置成 true 时会将 `batchRows` 条数据合并成一条 INSERT
  rootElement: undefined, // XML 数据的根元素名称，最大长度 200 个字符
  recordElement: undefined // XML 数据的记录元素名称，最大长度 200 个字符
});

const bOptions = [
  { label: 'CSV', value: 'CSV', installPackage: 'xcan-angus.csv-mock-plugin-1.0.0.zip', plugin: 'MockCsv' },
  { label: 'JSON', value: 'JSON', installPackage: 'xcan-angus.json-mock-plugin-1.0.0.zip', plugin: 'MockJson' },
  { label: 'CUSTOM', value: 'CUSTOM', installPackage: 'xcan-angus.custom-mock-plugin-1.0.0.zip', plugin: 'MockCustom' },
  { label: 'EXCEL', value: 'EXCEL', installPackage: 'xcan-angus.excel-mock-plugin-1.0.0.zip', plugin: 'MockExcel' },
  { label: 'SQL', value: 'SQL', installPackage: 'xcan-angus.sql-mock-plugin-1.0.0.zip', plugin: 'MockSql' },
  { label: 'TAB', value: 'TAB', installPackage: 'xcan-angus.tab-mock-plugin-1.0.0.zip', plugin: 'MockTab' },
  { label: 'XML', value: 'XML', installPackage: 'xcan-angus.xml-mock-plugin-1.0.0.zip', plugin: 'MockXml' }
];

const formatFieldMap = {
  CSV: ['lineEnding', 'includeHeader'],
  JSON: ['lineEnding', 'includeNull', 'rowsToArray'],
  CUSTOM: ['lineEnding', 'includeHeader', 'escapeChar', 'quoteChar', 'separatorChar'],
  EXCEL: ['includeHeader'],
  SQL: ['lineEnding', 'batchInsert', 'tableName'],
  TAB: ['lineEnding', 'includeHeader'],
  XML: ['lineEnding', 'rootElement', 'recordElement']
};

// eslint-disable-next-line no-control-regex
const regexpChar = /[\u0000-\u00ff]/;
const validateChar = (_key, value) => {
  if (!value) {
    return Promise.resolve();
  }
  if (regexpChar.test(value)) {
    return Promise.resolve();
  }
  // eslint-disable-next-line prefer-promise-reject-errors
  return Promise.reject(t('gendata.dataSet.validation.singleChar'));
};

const onFormatChange = (value) => {
  formState.value.format = value;
  // emits('update:format', value);
  // emits('update:plugin', option.plugin || '');
  emits('formatChange', value);
};

const getData = () => {
  const fileds = formatFieldMap[formState.value.format];
  const data = { format: formState.value.format };
  fileds.forEach(key => {
    data[key] = formState.value[key];
  });
  return data;
};

const validate = () => {
  return formRef.value.validate();
};

const setData = (data) => {
  const { format } = data;
  formState.value.format = format;
  const fileds = formatFieldMap[formState.value.format];
  fileds.forEach(key => {
    formState.value[key] = data[key];
  });
};

defineExpose({
  getData,
  validate,
  setData
});

</script>
<template>
  <Form
    ref="formRef"
    :model="formState"
    layout="inline"
    class="form-item-wrapper mb-1.5"
    size="small">
    <FormItem :label="t('gendata.dataSet.format')">
      <Select
        :value="formState.format"
        class="!w-30"
        :options="bOptions"
        @change="onFormatChange" />
    </FormItem>
    <template v-if="['CSV', 'TAB'].includes(formState.format)">
      <FormItem :label="t('gendata.dataSet.lineEnding')">
        <SelectEnum
          v-model:value="formState.lineEnding"
          class="!w-30"
          enumKey="LineEndingType" />
      </FormItem>
      <FormItem>
        <Checkbox v-model:checked="formState.includeHeader">{{ t('gendata.dataSet.includeHeader') }}</Checkbox>
      </FormItem>
    </template>
    <template v-if="formState.format=== 'CUSTOM'">
      <FormItem
        :label="t('gendata.dataSet.lineEnding')"
        name="lineEnding">
        <SelectEnum
          v-model:value="formState.lineEnding"
          enumKey="LineEndingType"
          class="!w-30" />
      </FormItem>
      <FormItem
        name="escapeChar"
        :label="t('gendata.dataSet.escapeChar')"
        :rules="{validator: validateChar}">
        <Input
          v-model:value="formState.escapeChar"
          :maxlength="1"
          class="!w-30" />
      </FormItem>
      <FormItem
        name="quoteChar"
        :label="t('gendata.dataSet.quoteChar')"
        :rules="{validator: validateChar}">
        <Input
          v-model:value="formState.quoteChar"
          :maxlength="1"
          class="!w-30" />
      </FormItem>
      <FormItem
        name="separatorChar"
        :label="t('gendata.dataSet.separatorChar')"
        :rules="{validator: validateChar}">
        <Input
          v-model:value="formState.separatorChar"
          :maxlength="1"
          class="!w-30" />
      </FormItem>
      <FormItem
        name="includeHeader">
        <Checkbox v-model:checked="formState.includeHeader">{{ t('gendata.dataSet.includeHeader') }}</Checkbox>
      </FormItem>
    </template>
    <template v-if="formState.format=== 'JSON'">
      <FormItem
        :label="t('gendata.dataSet.lineEnding')"
        name="lineEnding">
        <SelectEnum
          v-model:value="formState.lineEnding"
          class="!w-30"
          enumKey="LineEndingType" />
      </FormItem>
      <FormItem name="includeNull">
        <Checkbox v-model:checked="formState.includeNull">{{ t('gendata.dataSet.includeNull') }}</Checkbox>
      </FormItem>
      <FormItem name="rowsToArray">
        <Checkbox v-model:checked="formState.rowsToArray">{{ t('gendata.dataSet.rowsToArray') }}</Checkbox>
      </FormItem>
    </template>
    <template v-if="formState.format === 'EXCEL'">
      <FormItem name="includeHeader">
        <Checkbox v-model:checked="formState.includeHeader">{{ t('gendata.dataSet.includeHeader') }}</Checkbox>
      </FormItem>
    </template>
    <template v-if="formState.format === 'SQL'">
      <FormItem
        :label="t('gendata.dataSet.lineEnding')"
        name="lineEnding">
        <SelectEnum
          v-model:value="formState.lineEnding"
          class="!w-30"
          enumKey="LineEndingType" />
      </FormItem>
      <FormItem
        name="tableName"
        :label="t('gendata.dataSet.tableName')"
        :rules="{ required: true, message: t('gendata.dataSet.validation.tableNameRequired') }">
        <Input v-model:value="formState.tableName" class="!w-30" />
      </FormItem>
      <FormItem name="batchInsert">
        <Checkbox v-model:checked="formState.batchInsert">{{ t('gendata.dataSet.batchInsert') }}</Checkbox>
      </FormItem>
    </template>
    <template v-if="formState.format === 'XML'">
      <FormItem
        :label="t('gendata.dataSet.lineEnding')"
        name="lineEnding">
        <SelectEnum
          v-model:value="formState.lineEnding"
          class="!w-30"
          enumKey="LineEndingType" />
      </FormItem>
      <FormItem
        :label="t('gendata.dataSet.rootElement')"
        name="rootElement">
        <Input
          v-model:value="formState.rootElement"
          class="!w-30"
          :maxlength="200" />
      </FormItem>
      <FormItem
        :label="t('gendata.dataSet.recordElement')"
        name="recordElement">
        <Input
          v-model:value="formState.recordElement"
          class="!w-30"
          :maxlength="200" />
      </FormItem>
    </template>
  </Form>
</template>
<style scoped>
.form-item-wrapper > div{
  margin-bottom: 8px;
}

</style>
