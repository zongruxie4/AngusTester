<script setup lang="ts">
import { computed } from 'vue';
import { Progress } from 'ant-design-vue';

type Props = {
  percent: number;
  title?: string;
  text?: string;
}

const props = withDefaults(defineProps<Props>(), {
  percent: 0,
  title: undefined,
  text: undefined
});

const showText = computed(() => {
  return props.text && props.percent < 100;
});
</script>

<template>
  <div>
    <div v-if="!!props.title" class="text-center leading-4 mb-2">{{ props.title }}</div>
    <div class="relative flex items-center justify-center">
      <div v-if="showText" class="text-theme-sub-content progress-circle-text">进度</div>
      <Progress
        size="small"
        type="circle"
        :class="{ 'uncompleted': showText }"
        :strokeWidth="10"
        :width="75"
        v-bind="props"
        :percent="props.percent" />
    </div>
  </div>
</template>

<style scoped>
.ant-progress-circle :deep(.ant-progress-text) {
  font-size: 14px;
}

.uncompleted.ant-progress-circle :deep(.ant-progress-text) {
  top: 60%;
}

.progress-circle-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translateX(-50%) translateY(-20px);
  font-size: 12px;
}
</style>
