<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Tooltip, Popover } from '@xcan-angus/vue-ui';
import { Radio } from 'ant-design-vue';

import { HTTPConfig } from '../PropsType';

const { t } = useI18n();

type Server = HTTPConfig['request']['server']

export interface Props {
  server: Server;
  endpoint: string;
}

const props = withDefaults(defineProps<Props>(), {
  server: undefined,
  endpoint: undefined
});


const emit = defineEmits<{
  (e: 'change', value: Server): void;
}>();

const variables = computed(() => {
  if (!props.server?.variables) {
    return [];
  }

  return Object.entries(props.server.variables).map(([_name, value]) => {
    return {
      _name,
      ...value
    };
  });
});

const serverUrl = computed(() => {
  return props.server?.url;
});

const description = computed(() => {
  return props.server?.description;
});

const radioChange = (key: string, value: string) => {
  const data: Server = JSON.parse(JSON.stringify(props.server));
  if (data?.variables?.[key]) {
    data.variables[key].defaultValue = value;
  }
  emit('change', data);
};

const overlayStyle = {
  minWidth: '300px',
  maxWidth: '450px'
};
</script>

<template>
  <div class="flex-1 flex items-center h-7 leading-7 bg-white rounded composite-container">
    <Popover :overlayStyle="overlayStyle" placement="bottomLeft">
      <template #content>
        <template v-if="variables">
          <div
            v-for="item in variables"
            :key="item._name"
            class="leading-5 text-3 space-y-1 mb-3">
            <div class="flex items-center space-x-2">
              <div class="w-1 h-1 rounded-lg bg-slate-600"></div>
              <div class="text-theme-title font-bold">{{ item._name }}</div>
            </div>
            <div class="text-theme-content space-y-0.5">
              <div
                v-for="_ele in item.allowableValues"
                :key="_ele"
                class="flex items-center pl-7">
                <div class="flex-1 truncate">{{ _ele }}</div>
                <div class="flex items-center flex-shrink-0 space-x-1">
                  <div v-if="props.server?.variables?.[item._name]?.defaultValue === _ele" class="text-theme-sub-content">
                    <span>{{ t('common.default') }}</span>
                  </div>
                  <Radio
                    :checked="props.server?.variables?.[item._name]?.defaultValue === _ele"
                    @change="radioChange(item._name, _ele)" />
                </div>
              </div>
            </div>
          </div>
        </template>
      </template>
      <template #title>
        <div class="leading-4 py-2 space-y-2">
          <div class="text-theme-title font-bold break-all">{{ serverUrl }}</div>
          <div class="text-theme-content break-all">{{ description }}</div>
        </div>
      </template>
      <div style="flex:1 1 40%;" class="truncate px-1.75 text-3">
        {{ serverUrl }}
      </div>
    </Popover>
    <div class="w-0 h-3.5 border-r border-solid border-theme-divider"></div>
    <Tooltip placement="topLeft">
      <template #title>{{ props.endpoint }}</template>
      <div style="flex:1 1 60%;" class="truncate px-1.75 text-3">
        {{ props.endpoint }}
      </div>
    </Tooltip>
  </div>
</template>

<style scoped>
.composite-container {
  transition: all 300ms linear 0ms;
  background-color: #f5f5f5;
}
</style>
