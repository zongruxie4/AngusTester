<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';
import { Radio } from 'ant-design-vue';
import { exec } from '@/api/tester';

interface Props {
  execId: string
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  execId: ''
});

const serverList = ref([]);

const loadServers = async () => {
  const [error, { data = [] }] = await exec.getTestServer(props.execId);
  if (error) {
    return;
  }
  serverList.value = data || [];
};

const hasVariable = (variables = {}) => {
  if (!variables) {
    return false;
  }
  return !!Object.keys(variables || {}).length;
};

const defaultVariable = {
};

onMounted(() => {
  if (props.execId) {
    loadServers();
  }
});
</script>
<template>
  <div class="w-100 space-y-3">
    <div v-for="serverObj in serverList" class="border rounded p-2">
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
