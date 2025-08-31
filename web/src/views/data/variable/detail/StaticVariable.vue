<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Radio, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import { Hints, IconRequired, Input, notification, Validate, FunctionsButton, ParamTextarea } from '@xcan-angus/vue-ui';
import { isEqual } from 'lodash-es';
import { variable } from '@/api/tester';

import { StaticVariableFormState, VariableItem } from '../types';

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
  const [error] = await variable.putVariables(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('dataVariable.detail.staticVariable.notifications.editSuccess'));
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

  notification.success(t('dataVariable.detail.staticVariable.notifications.addSuccess'));
  const id = res?.data?.id;
  emit('ok', { ...params, id }, false);
};

const handleBlurValue = (targetText) => {
  variableValue.value = targetText;
};

const getParams = ():StaticVariableFormState => {
  const params:StaticVariableFormState = {
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
      <span>{{ t('dataVariable.detail.staticVariable.name') }}</span>
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
        :placeholder="t('dataVariable.detail.staticVariable.namePlaceholder')"
        trimAll
        @change="nameChange"
        @blur="nameBlur" />
    </Validate>
  </div>

  <div class="flex items-center mb-3.5">
    <div class="flex items-center flex-shrink-0 mr-2.5">
      <IconRequired />
      <span>{{ t('dataVariable.detail.staticVariable.password') }}</span>
    </div>
    <RadioGroup v-model:value="passwordValue" name="passwordValue">
      <Radio :value="false">{{ t('dataVariable.detail.staticVariable.no') }}</Radio>
      <Radio :value="true">
        <div class="flex items-center">
          <span class="mr-2">{{ t('dataVariable.detail.staticVariable.yes') }}</span>
          <Hints :text="t('dataVariable.detail.staticVariable.passwordHint')" />
        </div>
      </Radio>
    </RadioGroup>
  </div>

  <div class="flex items-start">
    <div class="flex items-center flex-shrink-0 mr-2.5 transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>{{ t('dataVariable.detail.staticVariable.description') }}</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{minRows:3,maxRows:5}"
      showCount
      type="textarea"
      class="flex-1"
      :placeholder="t('dataVariable.detail.staticVariable.descriptionPlaceholder')"
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
          <span>{{ t('dataVariable.detail.staticVariable.value') }}</span>
        </div>
      </template>

      <div class="flex">
        <Hints class="mb-2.5" :text="t('dataVariable.detail.staticVariable.hints')" />
        <FunctionsButton class="ml-1 text-3.5" />
      </div>
      <ParamTextarea
        :value="variableValue"
        :maxLength="4096"
        :height="60"
        :placeholder="t('dataVariable.detail.staticVariable.valuePlaceholder')"
        @blur="handleBlurValue" />
    </TabPane>

    <TabPane key="preview">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.staticVariable.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <TabPane v-if="variableId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.staticVariable.use') }}</span>
        </div>
      </template>

      <VariableUseList :id="variableId" />
    </TabPane>
  </Tabs>
</template>
