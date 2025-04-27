<script setup lang="ts">
import { computed } from 'vue';

export type Props = {
  status: {
    value: 'UNKNOWN' | 'IN_DESIGN' | 'IN_DEV' | 'DEV_COMPLETED' | 'RELEASED';
    message: string;
  };
}

const props = withDefaults(defineProps<Props>(), {
  status: undefined
});

const statusMap = {
  UNKNOWN: '未知',
  IN_DESIGN: '设计中',
  IN_DEV: '开发中',
  DEV_COMPLETED: '开发完成',
  RELEASED: '已发布'
};

const statusValue = computed(() => {
  if (!props.status) {
    return '';
  }

  if (typeof props.status === 'object') {
    return props.status.value;
  }

  return props.status;
});

const statusMessage = computed(() => {
  if (!statusValue.value) {
    return '';
  }

  return statusMap[statusValue.value];
});

const className = computed(() => {
  if (!statusValue.value) {
    return '';
  }

  return 'api-status-' + statusValue.value;
});
</script>
<template>
  <div
    :class="className"
    class="api-status-div w-17.5 inline-block px-2.5 leading-4.5 border border-solid text-center text-3 rounded-xl">
    <span style="transform: scale(0.95);" class="inline-block">{{ statusMessage }}</span>
  </div>
</template>
<style scoped>
.api-status-UNKNOWN {
  border-color: rgba(235, 237, 248, 100%);
  background-color: rgba(235, 237, 248, 50%);
  color: rgba(140, 140, 140, 100%);
}

.api-status-IN_DESIGN {
  border-color: rgba(255, 129, 0, 100%);
  background-color: rgba(255, 129, 0, 10%);
  color: rgba(255, 129, 0, 100%);
}

.api-status-IN_DEV {
  border-color: rgba(0, 119, 255, 100%);
  background-color: rgba(0, 119, 255, 10%);
  color: rgba(0, 119, 255, 100%);
}

.api-status-DEV_COMPLETED {
  border-color: rgba(82, 196, 26, 100%);
  background-color: rgba(82, 196, 26, 10%);
  color: rgba(82, 196, 26, 100%);
}

.api-status-RELEASED {
  border-color: rgba(183, 235, 143, 100%);
  background-color: rgba(246 ,255, 237,100%);
  color: rgb(56 ,158, 13,100%);
}
</style>
