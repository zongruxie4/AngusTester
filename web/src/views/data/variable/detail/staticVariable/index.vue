<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Radio, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import { Hints, IconRequired, Input, notification, Validate } from '@xcan-angus/vue-ui';
import { isEqual } from 'lodash-es';
import { FunctionsButton, ParamTextarea } from '@xcan-angus/vue-ui';
import { variable } from '@/api/tester';

import { VariableItem } from '../../PropsType';
import { FormState } from './PropsType';

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

const ButtonGroup = defineAsyncComponent(() => import('@/views/data/variable/detail/buttonGroup/index.vue'));
const PreviewData = defineAsyncComponent(() => import('@/views/data/variable/detail/previewData/index.vue'));
const VariableUseList = defineAsyncComponent(() => import('@/views/data/variable/detail/useList/index.vue'));

const confirmLoading = ref(false);
const activeKey = ref<'value' | 'preview' | 'use'>('value');

const passwordValue = ref(false);
const variableName = ref<string>('');
const variableNameError = ref(false);
const description = ref<string>('');
const variableValue = ref<string>('');

const previewData = ref<{
  name: string;
  value: string;
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

const buttonGroupClick = (key: 'ok' | 'delete'|'export' | 'clone' | 'copyLink' | 'refresh') => {
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
  const [error] = await variable.putVariables( params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success('变量修改成功');
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

  notification.success('变量添加成功');
  const id = res?.data?.id;
  emit('ok', { ...params, id }, false);
};

const handleBlurValue = (targetText) => {
  variableValue.value = targetText;
};

const getParams = ():FormState => {
  const params:FormState = {
    projectId: props.projectId,
    name: variableName.value,
    value: variableValue.value,
    passwordValue: passwordValue.value,
    description: description.value
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

  passwordValue.value = data.passwordValue;
  variableName.value = data.name;
  variableValue.value = data.value;
  description.value = data.description;
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
      value: variableValue.value
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
  return !variableName.value || !variableValue.value;
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
      <span>名称</span>
    </div>
    <Validate
      class="flex-1"
      text="支持数字、字母和!$%^&*_-+=./"
      mode="error"
      :error="variableNameError">
      <Input
        v-model:value="variableName"
        :maxlength="100"
        :error="variableNameError"
        dataType="mixin-en"
        excludes="{}"
        includes="\!\$%\^&\*_\-+=\.\/"
        placeholder="支持数字、字母和!$%^&*_-+=./，最长100个字符"
        trimAll
        @change="nameChange"
        @blur="nameBlur" />
    </Validate>
  </div>

  <div class="flex items-center mb-3.5">
    <div class="flex items-center flex-shrink-0 mr-2.5">
      <IconRequired />
      <span>密码</span>
    </div>
    <RadioGroup v-model:value="passwordValue" name="passwordValue">
      <Radio :value="false">否</Radio>
      <Radio :value="true">
        <div class="flex items-center">
          <span class="mr-2">是</span>
          <Hints text="如果是密码类型变量，变量值在界面上对用户不可见。" />
        </div>
      </Radio>
    </RadioGroup>
  </div>

  <div class="flex items-start">
    <div class="flex items-center flex-shrink-0 mr-2.5 transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>描述</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{minRows:3,maxRows:5}"
      showCount
      type="textarea"
      class="flex-1"
      placeholder="变量描述，最大支持200个字符"
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
          <span>值</span>
        </div>
      </template>

      <div class="flex">
        <Hints class="mb-2.5" text="定义一个常量、Mock数据函数或包含Mock数据函数的文本作为变量值。如果变量值中包含Mock数据函数，默认每一次迭代前更新一次值，同一个迭代内采样请求变量值保持不变。" />
        <FunctionsButton class="ml-1 text-3.5" />
      </div>
      <ParamTextarea
        :value="variableValue"
        :maxLength="4096"
        :height="60"
        placeholder="变量值，支持Mock函数，最大支持4096个字符。值示例：123456、true、@String(5,10)"
        @blur="handleBlurValue" />
    </TabPane>

    <TabPane key="preview">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>预览</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <TabPane v-if="variableId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>使用</span>
        </div>
      </template>

      <VariableUseList :id="variableId" />
    </TabPane>
  </Tabs>
</template>
