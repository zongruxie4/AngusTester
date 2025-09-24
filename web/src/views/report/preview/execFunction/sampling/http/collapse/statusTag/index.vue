<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  value?: 'success' | 'fail' | 'ignore' | 'block';
}

const props = withDefaults(defineProps<Props>(), {
  value: 'ignore'
});

const text = computed(() => {
  if (props.value === 'fail') {
    return t('status.failed');
  }

  if (props.value === 'success') {
    return t('rstatus.passed');
  }

  if (props.value === 'block') {
    return t('reportPreview.execFunction.sampling.collapse.statusTag.notExecuted');
  }

  return t('status.disabled');
});

const style = computed(() => {
  if (props.value === 'fail') {
    return 'background-color:rgba(255, 129, 0 , 0.8);';
  }

  if (props.value === 'success') {
    return 'background-color:#52c41a;';
  }

  if (props.value === 'block') {
    return 'background-color:rgba(217, 217, 217, 1);';
  }

  return 'background-color:rgba(217, 217, 217, 1);';
});
</script>
<template>
  <div :style="style" class="inline-block min-w-13 text-center px-2 rounded text-3 leading-5 text-white">{{ text }}</div>
</template>
