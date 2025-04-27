<script setup lang="ts">
import { ref, inject } from 'vue';
import { TESTER } from '@xcan-angus/tools';
import { Input, Icon, SelectEnum, Select, Hints } from '@xcan-angus/vue-ui';
import { Checkbox, FormItem, Tooltip, Form } from 'ant-design-vue';

const projectInfo = inject('projectInfo', ref({ id: '' }));

const formState = ref({
  batchRows: '1',
  type: undefined,
  datasourceId: undefined,
  password: undefined,
  username: undefined,
  jdbcUrl: undefined,
  name: undefined,
  driverClassName: undefined,
  batchInsert: false
});
const formRef = ref();
const onBatchRowsBlur = () => {
  const value = formState.value.batchRows.trim();
  if (!value) {
    formState.value.batchRows = '1';
  }
};
const typeDisabled = ref(false);
const driverClassNameDisabled = ref(false);

const defaultSource = ref();
const changeDatasource = (value, option) => {
  formState.value.datasourceId = value;
  defaultSource.value = { [value]: option };
  const { password, username, jdbcUrl, name, driverClassName, database } = option;
  formState.value.password = password;
  formState.value.username = username;
  formState.value.jdbcUrl = jdbcUrl;
  formState.value.name = name;
  formState.value.driverClassName = driverClassName;
  formState.value.type = database || undefined;
  if (database) {
    typeDisabled.value = true;
  } else {
    typeDisabled.value = false;
  }
  if (driverClassName) {
    driverClassNameDisabled.value = true;
  } else {
    driverClassNameDisabled.value = false;
  }
};

const validate = () => {
  return formRef.value.validate();
};

defineExpose({
  getData: () => {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { batchRows, datasourceId, ...storeDatasource } = formState.value;
    return {
      batchRows,
      storeDatasource: {
        ...storeDatasource
      }
    };
  },
  getCurrentData: () => {
    return {
      ...formState.value,
      defaultSource: defaultSource.value
    };
  },
  setData: (data) => {
    defaultSource.value = data.defaultSource || undefined;
    delete data.defaultSource;
    formState.value = data;
    if (defaultSource.value[data.datasourceId]) {
      changeDatasource(data.datasourceId, defaultSource.value[data.datasourceId]);
    }
  },
  validate
});

</script>
<template>
  <Form
    ref="formRef"
    :model="formState"
    layout="vertical">
    <!-- <div class="flex items-center mb-2">
      <span class="w-20">批量行数</span>
      <Input
        v-model:value="formState.batchRows"
        class="w-80"
        dataType="number"
        :max="10000"
        :min="1"
        @blur="onBatchRowsBlur" />
      <Tooltip title="最大支持10000" placement="topLeft">
        <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
      </Tooltip>
    </div>
    <div class="flex items-center mb-2">
      <span class="w-20">是否批量插入</span>
      <Checkbox v-model:checked="formState.batchInsert" />
      <Tooltip title="设置成 true 时会将 [batchrows] 条数据合并成一条 INSERT 数据">
        <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
      </Tooltip>
    </div> -->
    <FormItem label="批量行数">
      <Hints text="每次批量生成、存储或发送行数，默认 1，最大10000。" class="mb-2" />
      <div class="flex items-center">
        <Input
          v-model:value="formState.batchRows"
          dataType="number"
          :max="10000"
          :min="1"
          @blur="onBatchRowsBlur" />
        <!-- <Tooltip title="最大支持10000" placement="topLeft">
          <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
        </Tooltip> -->
      </div>
    </FormItem>
    <FormItem label="是否批量插入">
      <div class=" flex items-center">
        <Checkbox v-model:checked="formState.batchInsert" />
        <Hints :text="`设置成 true 时会将${formState.batchRows}条数据合并成一条 INSERT 数据`" class="ml-2" />
      </div>
      <!-- <Tooltip title="设置成 true 时会将 [batchrows] 条数据合并成一条 INSERT 数据">
        <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
      </Tooltip> -->
    </FormItem>
    <!-- <div class="flex mb-2">
      <span class="w-20 leading-7">选择数据源</span>
      <FormItem
        name="datasourceId"
        class="w-80"
        :rules="{ required: true, message: '请选择数据源' }">
        <Select
          v-model:value="formState.datasourceId"
          defaultActiveFirstOption
          :lazy="false"
          :action="`${TESTER}/mock/datasource`"
          :fieldNames="{value: 'id', label: 'name'}"
          @change="changeDatasource" />
      </FormItem>
    </div> -->
    <FormItem
      name="datasourceId"
      label="选择数据源"
      :rules="{ required: true, message: '请选择数据源' }">
      <Select
        v-model:value="formState.datasourceId"
        defaultActiveFirstOption
        :defaultOptions="defaultSource"
        :lazy="false"
        :action="`${TESTER}/data/datasource?projectId=${projectInfo.id}`"
        :fieldNames="{value: 'id', label: 'name'}"
        @change="changeDatasource" />
    </FormItem>
    <!-- <div class="flex mb-2">
      <span class="w-20 leading-7">数据库类型</span>
      <FormItem
        name="type"
        class="w-80"
        :rules="{ required: true, message: '请选择数据库类型' }">
        <SelectEnum
          v-model:value="formState.type"
          enumKey="DatabaseType"
          :lazy="false" />
      </FormItem>
    </div> -->
    <FormItem
      name="type"
      label="数据库类型"
      :rules="{ required: true, message: '请选择数据库类型' }">
      <SelectEnum
        v-model:value="formState.type"
        enumKey="DatabaseType"
        :disabled="typeDisabled"
        :lazy="false" />
    </FormItem>
    <!-- <div class="flex items-center">
      <span class="w-20 leading-7">驱动完整类名</span>
      <FormItem
        name="driverClassName"
        class="w-80">
        <Input
          v-model:value="formState.driverClassName"
          :maxlength="200" />
      </FormItem>
    </div> -->
    <FormItem
      name="driverClassName"
      label="驱动完整类名">
      <Hints text="不指定时将使用默认驱动类名" class="mb-2" />
      <Input
        v-model:value="formState.driverClassName"
        :disabled="driverClassNameDisabled"
        :maxlength="200" />
    </FormItem>
  </Form>
</template>
