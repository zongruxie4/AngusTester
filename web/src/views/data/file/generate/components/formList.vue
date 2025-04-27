<script setup lang="ts">
import { ref } from 'vue';
import { Button, Form, FormItem } from 'ant-design-vue';
import { Icon, IconCopy, Input, Select } from '@xcan-angus/vue-ui';
import type { Rule } from 'ant-design-vue/es/form';

import FuncModal from './funcModal.vue';

const formRef = ref();

const formState = ref<{list:{name:string, type:string, value:string | undefined}[]}>({
  list: [
    { name: 'id', type: 'string', value: '' },
    { name: 'name', type: 'string', value: '' },
    { name: '', type: 'string', value: undefined }
  ]
});

const keNameValidator = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请输入参数名'));
  } else {
    const keyNames = new Set();
    const hasDuplicates = formState.value.list.some(obj => {
      if (keyNames.has(obj.name)) {
        return true;
      }
      keyNames.add(obj.name);
      return false;
    });
    if (hasDuplicates) {
      return Promise.reject(new Error('keyName存在重复'));
    } else {
      formRef.value.clearValidate();
      return Promise.resolve();
    }
  }
};

const inOptions = ['string', 'array', 'boolean', 'integer', 'object', 'number'].map(i => ({ value: i, label: i }));

const delApisSecurityItem = (index:number) => {
  formState.value.list.splice(index, 1);
};

const addApisSecurityItem = () => {
  if (formState.value.list.length === 10) {
    return;
  }
  formRef.value.validate().then(async () => {
    formState.value.list.push({
      name: '',
      value: '',
      type: 'string'
    });
  }).catch(error => {
    const errNames = error.errorFields.map(item => item.name[0]);
    if (errNames.length && errNames.includes('list')) {
      return;
    }
    formState.value.list.push({
      name: '',
      value: '',
      type: 'string'
    });
  });
};

const funcVisible = ref(false);
const selectFuncs = () => {
  funcVisible.value = true;
};

const getData = () => {
  return formState.value.list.filter(i => !!i.name);
};

defineExpose({
  getData
});

</script>
<template>
  <Form
    ref="formRef"
    :model="formState"
    :colon="false"
    size="small">
    <div
      v-for="(item,index) in formState.list"
      :key="index"
      class="flex space-x-2 flex-1">
      <FormItem
        class="flex-1"
        :name="['list', index, 'name']"
        :rules="{required: true, validator: keNameValidator}">
        <Input
          v-model:value="item.name"
          placeholder="列名，最多200字符以内"
          :maxLength="200" />
      </FormItem>
      <FormItem class="w-20">
        <Select
          v-model:value="item.type"
          :options="inOptions"
          showSearch
          size="small"
          placeholder="类型" />
      </FormItem>
      <FormItem
        class="flex-1"
        :name="['list', index, 'value']"
        :rules="{required: true, message: '请输入value'}">
        <Input
          v-model:value="item.value"
          placeholder="请选择或输入函数表达式，最多8192字符以内"
          :maxLength="8192">
          <template #suffix>
            <Icon
              icon="icon-hanshu"
              class="cursor-pointer"
              @click="selectFuncs" />
          </template>
        </Input>
      </FormItem>
      <Button
        class="h-6.75 w-6.75 p-0">
        <IconCopy class="text-4" />
      </Button>
      <Button
        class="h-6.75 w-6.75 p-0"
        @click="addApisSecurityItem">
        <Icon
          icon="icon-jia"
          class="text-3.5" />
      </Button>
      <Button
        class="h-6.75 w-6.75 p-0"
        @click="delApisSecurityItem(index)">
        <Icon
          icon="icon-jian"
          class="text-3.5" />
      </Button>
    </div>
  </Form>
  <FuncModal v-model:visible="funcVisible" />
</template>
