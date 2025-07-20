<script setup lang="ts">
import { computed, ref } from 'vue';
import { Modal, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/tools';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  visible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  visible: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: string): void;
}>();

const serviceId = ref<string>();
const apiId = ref<string>();

const cancel = () => {
  serviceId.value = undefined;
  apiId.value = undefined;
  emit('update:visible', false);
};

const ok = () => {
  if (!apiId.value) {
    return;
  }

  emit('ok', apiId.value);
  cancel();
};

const okButtonProps = computed(() => {
  const disabled = !apiId.value;
  return {
    disabled
  };
});

const serviceAction = computed(() => {
  return `${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`;
});

const apisAction = computed(() => {
  if (serviceId.value) {
    return `${TESTER}/apis?projectId=${props.projectId}&serviceId=${serviceId.value}&fullTextSearch=true`;
  }
  return `${TESTER}/apis?projectId=${props.projectId}&fullTextSearch=true`;
});
</script>

<template>
  <Modal
    title="选择接口"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    @cancel="cancel"
    @ok="ok">
    <div class="flex items-center mb-3.5">
      <div class="flex-shrink-0 mr-2.5">
        <IconRequired class="invisible" />
        <span>选择服务</span>
      </div>
      <Select
        v-model:value="serviceId"
        showSearch
        placeholder="选择服务"
        style="width: calc(100% - 58px);"
        :action="serviceAction"
        :fieldNames="{ label: 'name', value: 'id' }" />
    </div>

    <div class="flex items-center">
      <div class="flex-shrink-0 mr-2.5">
        <IconRequired class="invisible" />
        <span>选择接口</span>
      </div>
      <Select
        v-model:value="apiId"
        showSearch
        placeholder="选择接口"
        style="width: calc(100% - 58px);"
        :action="apisAction"
        :fieldNames="{ label: 'summary', value: 'id' }" />
    </div>
  </Modal>
</template>
