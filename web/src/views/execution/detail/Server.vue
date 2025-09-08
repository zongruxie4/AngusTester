<script lang="ts" setup>
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';
import { Radio } from 'ant-design-vue';
import { useServerData } from './composables/useServerData';

/**
 * Props for the Server component
 */
interface Props {
  /** Execution ID for loading server data */
  execId: string
}

// Initialize internationalization
const { t } = useI18n();

// Define component props with defaults
const props = withDefaults(defineProps<Props>(), {
  execId: ''
});

// Use server data composable for logic
const { serverList, loadServers, hasVariable } = useServerData(props.execId);

// Default empty variable object
const defaultVariable = {};

// Load servers when component is mounted
onMounted(() => {
  if (props.execId) {
    loadServers();
  }
});
</script>
<template>
  <div class="w-100 space-y-3">
    <div
      v-for="(serverObj, index) in serverList"
      :key="index"
      class="border rounded p-2">
      <div class="font-bold text-text-title flex items-center">
        <Icon icon="icon-fuwuqi" class="mr-1" />{{ serverObj.url }}
      </div>
      <div class="my-3 ">{{ serverObj.description || t('execution.infoServer.noDescription') }}</div>
      <ul v-if="hasVariable(serverObj.variables)" class="list-disc space-y-1 pl-4">
        <li v-for="(_value, key) in (serverObj.variables || defaultVariable)" :key="key">
          <div
            class="text-3 text-text-title rounded-sm leading-5 truncate cursor-pointer inline font-bold"
            :title="key + ''"
            style="max-width: 400px;">
            {{ key }}
          </div>
          <div class="space-y-1">
            <div
              v-for="en in _value.enum"
              :key="en"
              class="flex items-center justify-between">
              <div
                class="truncate cursor-pointer"
                style="max-width: 400px;"
                :title="en">
                {{ en }}
              </div>
              <div class="inline-flex items-center space-x-1">
                <span v-show="_value.default === en">{{ t('execution.infoServer.default') }}</span>
                <Radio
                  size="small"
                  disabled
                  :checked="_value.default === en"
                  class="-mt-1.5" />
              </div>
            </div>
          </div>
        </li>
      </ul>
      <div v-else>
        {{ t('execution.infoServer.noVariables') }}
      </div>
    </div>
  </div>
</template>
