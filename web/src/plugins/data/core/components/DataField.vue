<script setup lang="ts">
import { ref, defineAsyncComponent} from 'vue';
import { Form, FormItem, Button } from 'ant-design-vue';
import { Select, Input, Icon, Popover, AsyncComponent, FunctionsModal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import type { Rule } from 'ant-design-vue/es/form';
const ParamInput = defineAsyncComponent(() => import('@/components/ParamInput/index.vue'));

export interface FieldItem {
  name:string, type:string, value:string | undefined
}

export interface Props {
  format: string;
  data?: FieldItem[]
}

const props = withDefaults(defineProps<Props>(), {
  format: 'CVS'
});

const { t } = useI18n();

const formRef = ref();

const formState = ref<{list:FieldItem[]}>({
  list: [
    // { name: 'id', type: 'string', value: '' },
    // { name: 'name', type: 'string', value: '' },
    { name: '', type: 'string', value: undefined }
  ]
});

const keNameValidator = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('genDataPlugin.messages.pleaseInput')));
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
      return Promise.reject(new Error(t('genDataPlugin.messages.operationFailed')));
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

const funcModalVisible = ref(false);
const openFuncModal = () => {
  funcModalVisible.value = true;
};

const handleValueChange = (target: Element, idx: number) => {
  const value = target?.innerText?.trim().replace('\n', '');
  formState.value.list[idx].value = value;
};

const getData = () => {
  return formState.value.list.map(i => {
    return i;
  });
};

const valid = ref(false);
const validate = () => {
  const list = getData();
  if (list.length > 0) {
    return Promise.resolve();
  }
  valid.value = true;
  return formRef.value.validate();
};

const setData = (data = []) => {
  if (data.length) {
    formState.value.list = data;
  } else {
    formState.value.list = [{ name: '', type: 'string', value: undefined }];
  }
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
          :placeholder="t('genDataPlugin.dataConfig.fieldName')"
          :maxLength="200" />
      </FormItem>
      <FormItem class="w-20">
        <Select
          v-model:value="item.type"
          :options="inOptions"
          showSearch
          size="small"
          :placeholder="t('genDataPlugin.form.type')" />
      </FormItem>
      <FormItem
        class="flex-1"
        :name="['list', index, 'value']"
        :rules="{required: true, message: t('genDataPlugin.messages.pleaseInput')}">
        <ParamInput
          :value="item.value"
          :placeholder="t('genDataPlugin.dataConfig.fieldValue')"
          :maxLength="8192"
          :error="!item.value && valid"
          @blur="handleValueChange($event, index)" />
        <!-- <Input
          v-model:value="item.value"
          placeholder="请选择或输入函数表达式，最多8192字符以内"
          :maxlength="8192">
        </Input> -->
      </FormItem>
      <Popover :content="t('genDataPlugin.dataConfig.title')">
        <Button
          size="small"
          class="h-6.75 w-6.75 px-0"
          @click="openFuncModal">
          <Icon icon="icon-hanshu" class="text-3.5" />
        </Button>
      </Popover>
      <Button
        size="small"
        class="h-6.75 w-6.75 px-0"
        :disabled="formState.list.length >= 200"
        @click="addApisSecurityItem">
        <Icon
          icon="icon-jia"
          class="text-3.5" />
      </Button>
      <Button
        size="small"
        class="h-6.75 w-6.75 px-0"
        :disabled="formState.list.length <= 1"
        @click="delApisSecurityItem(index)">
        <Icon
          icon="icon-jian"
          class="text-3.5" />
      </Button>
    </div>
    <AsyncComponent :visible="funcModalVisible">
      <FunctionsModal v-model:visible="funcModalVisible" />
    </AsyncComponent>
  </Form>
</template>
