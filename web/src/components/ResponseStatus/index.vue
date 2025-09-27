<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue';
import { EnumMessage, HttpStatus, enumUtils } from '@xcan-angus/infra';
import { getStatusColor } from 'src/utils/apis';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();

interface Props {
  status: string|number;
}

const props = withDefaults(defineProps<Props>(), {
  status: ''
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'rendered', value: true);
}>();

const statusOpt = ref<EnumMessage<HttpStatus>[]>([]);

const loadHttpStatus = async () => {
  if (statusOpt.value.length > 0) {
    return;
  }
  statusOpt.value = enumUtils.enumToMessages(HttpStatus);
};

const getStatusText = (status:number|string):string => {
  if (status) {
    const target = statusOpt.value.find(i => i.value === (status + ''));
    return target ? (target.value + ' ' + target.message) : status + '';
  }
  return '';
};

onMounted(() => {
  loadHttpStatus();

  nextTick(() => {
    emit('rendered', true);
  });
});
</script>
<template>
  <div class="flex items-center flex-nowrap whitespace-nowrap mr-7.5">
    <span class="mr-1">{{ t('xcan_responseStatus.statusCode') }}:</span>
    <span :class="getStatusColor(props.status)">
      {{ getStatusText(props.status) }}
    </span>
  </div>
</template>
