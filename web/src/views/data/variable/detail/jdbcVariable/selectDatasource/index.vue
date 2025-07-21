<script setup lang="ts">
import { computed, ref } from 'vue';
import { IconRequired, Modal, Select } from '@xcan-angus/vue-ui';
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
  (e: 'ok', value: {[key:string]: any}): void;
}>();

const selectedData = ref< {[key:string]: any}>();

const change = (_value:string, option) => {
  selectedData.value = option;
};

const cancel = () => {
  selectedData.value = undefined;
  emit('update:visible', false);
};

const ok = () => {
  if (!selectedData.value) {
    return;
  }

  emit('ok', selectedData.value);
  cancel();
};

const okButtonProps = computed(() => {
  const disabled = !selectedData.value;
  return {
    disabled
  };
});

const apisAction = computed(() => {
  return `${TESTER}/data/datasource?projectId=${props.projectId}&fullTextSearch=true`;
});
</script>

<template>
  <Modal
    title="选择数据源"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    @cancel="cancel"
    @ok="ok">
    <div class="flex items-center">
      <div class="flex-shrink-0 mr-2.5">
        <IconRequired class="invisible" />
        <span>选择数据源</span>
      </div>
      <Select
        showSearch
        placeholder="选择数据源"
        class="w-full"
        :action="apisAction"
        :fieldNames="{ label: 'name', value: 'id' }"
        @change="change" />
    </div>
  </Modal>
</template>
