<script setup lang="ts">
import { computed } from 'vue';
import { TypographyParagraph } from 'ant-design-vue';
import { Colon } from '@xcan-angus/vue-ui';

interface Props {
  value: {
    [key: string]: {
      default: string;
      enum: string[];
      description: string;
    }
  };
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const keys = computed(() => {
  if (!props.value) {
    return [];
  }

  return Object.keys(props.value);
});

const dataMap = computed(() => {
  if (!props.value) {
    return {};
  }

  return props.value;
});
</script>
<template>
  <div class="space-y-2.5">
    <div v-for="item in keys" :key="item">
      <div class="flex items-center">
        <div class="w-1.5 h-1.5 rounded border-2 border-solid border-gray-400"></div>
        <div class="text-theme-title text-3.5 ml-1.5">{{ item }}</div>
      </div>
      <div class="flex items-center mt-1.5">
        <div class="flex-shrink-0 flex items-center w-13">
          <span class="text-theme-title">默认值</span>
          <Colon />
        </div>
        <div class="flex-1 truncate">{{ dataMap[item].default }}</div>
      </div>
      <div class="flex items-start mt-1.5">
        <div class="flex-shrink-0 flex items-center w-13">
          <span class="text-theme-title">枚举值</span>
          <Colon />
        </div>
        <div class="flex-1 flex items-start space-x-2">
          <div
            v-for="_enum in dataMap[item].enum"
            :key="_enum"
            :title="_enum"
            class="tag-item">
            {{ _enum }}
          </div>
        </div>
      </div>
      <div class="flex items-start mt-1.5">
        <div class="flex-shrink-0 flex items-center w-13">
          <span class="text-theme-title">描述</span>
          <Colon />
        </div>
        <TypographyParagraph
          v-if="dataMap[item].description"
          class="break-all"
          :content="dataMap[item].description"
          :ellipsis="{ rows: 3, expandable: true, symbol: '更多' }" />
      </div>
    </div>
  </div>
</template>
<style scoped>
.tag-item {
  min-width: 40px;
  max-width: 80px;
  padding: 2px 4px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  background: #f2f4f5;
  line-height: 14px;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
