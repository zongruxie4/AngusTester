<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Hints, Modal, notification } from '@xcan-angus/vue-ui';
import { apis, services } from '@/api/tester';
import SelectEnum from '@/components/selectEnum/index.vue';

interface Props {
  visible: boolean;
  value?: string;
  type: |'SERVICE'|'API';
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  value: '',
  type: 'API'
});

const { t } = useI18n();
const tipConfig = {
  // PROJECT: '修改项目时会同步修改项目下所有服务和接口状态。',
  SERVICE: t('service.statusModal.tips.serviceStatusChange'),
  API: ''
};

const publishTipConfig = {
  // PROJECT: '发布项目会发布项目下所有服务和接口，发布后的项目、服务、接口不允许修改保存。',
  SERVICE: t('service.statusModal.tips.servicePublish'),
  API: t('service.statusModal.tips.apiPublish')
};

const setStatusApiConfig = {
  SERVICE: services.patchStatus,
  API: apis.patchApiStatus
};
const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'confirm', value: {value: string, message: string}):void}>();

const statusValue = ref<string>('');
const statuName = ref<string>('');

const close = () => {
  emits('update:visible', false);
};

const handleStatusChange = (_value, option) => {
  statuName.value = option.label;
};
const submit = async () => {
  if (statusValue.value === props.value) {
    close();
    return;
  }
  const [error] = await setStatusApiConfig[props.type]({ id: props.id, status: statusValue.value });
  if (error) {
    return;
  }
  notification.success(t('service.statusModal.messages.updateStatusSuccess'));
  emits('confirm', { value: statusValue.value, message: statuName.value });
  close();
};

watch(() => props.visible, newValue => {
  if (newValue) {
    statusValue.value = props.value || '';
  }
}, {
  immediate: true
});

// const apiStatusOpt = ref<{value: string, message}[]>([]);
</script>
<template>
  <Modal
    :title="t('service.statusModal.title')"
    :visible="props.visible"
    :width="400"
    @cancel="close"
    @ok="submit">
    <Hints
      v-show="tipConfig[props.type]"
      class="text-3 mb-3"
      :text="tipConfig[props.type]">
    </Hints>
    <Hints
      v-show="statusValue === 'RELEASED'"
      class="text-3 mb-3"
      :text="publishTipConfig[props.type]">
    </Hints>
    <div class="flex items-center">
      <span class="w-10">
        {{ t('service.statusModal.form.status') }}
      </span>
      <SelectEnum
        v-model:value="statusValue"
        enumKey="ApiStatus"
        class="flex-1"
        @change="handleStatusChange" />
    </div>
  </Modal>
</template>
