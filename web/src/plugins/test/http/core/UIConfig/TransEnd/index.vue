<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import { TransEndConfig } from './PropsType';

const { t } = useI18n();

export interface Props {
  name: string;
  description:string;
}

const props = withDefaults(defineProps<Props>(), {
  name: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'renderChange'): void;
}>();

onMounted(() => {
  emit('renderChange');
});

const showName = computed(() => {
  const _name = props.name?.replace(/(.+)_end$/, '$1');
  return _name ? (_name + t('httpPlugin.uiConfig.transEnd.suffix')) : '';
});

const getData = ():Omit<TransEndConfig, 'id'> => {
  return {
    enabled: true,
    target: 'TRANS_END',
    name: props.name + '_end',
    description: props.description,
    beforeName: '',
    transactionName: ''
  };
};

defineExpose({
  getData
});
</script>

<template>
  <div class="h-9 flex items-center pl-9.5 pr-3 rounded bg-blue-light">
    <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-shiwu" />
    <div
      :title="showName"
      class="truncate"
      style="width: calc((100% - (280px))*2/5);">
      {{ showName }}
    </div>
  </div>
</template>
<style scoped>
.bg-blue-light {
  background-color: rgba(232, 240, 251, 100%);
}
</style>
