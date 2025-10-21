<script setup lang="ts">
import { computed, ref } from 'vue';
import { Modal, Select, IconRequired } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

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
    :title="t('xcan_exec.selectDataSource')"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    @cancel="cancel"
    @ok="ok">
    <div class="flex items-center">
      <div class="flex-shrink-0 mr-2.5">
        <IconRequired class="invisible" />
        <span>{{ t('xcan_exec.selectDataSource') }}</span>
      </div>
      <Select
        showSearch
        :placeholder="t('xcan_exec.selectDataSource')"
        class="w-full"
        :action="apisAction"
        :fieldNames="{ label: 'name', value: 'id' }"
        @change="change" />
    </div>
  </Modal>
</template>
