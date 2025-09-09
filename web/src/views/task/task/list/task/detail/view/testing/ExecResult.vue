<script setup lang="ts">
import { computed } from 'vue';
import { Colon, Toggle, NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { TaskInfo } from '@/views/task/types';

type Props = {
  dataSource: TaskInfo;
  largePageLayout: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined,
  largePageLayout: undefined
});

const { t } = useI18n();

const execResult = computed(() => props.dataSource?.execResult);
const execId = computed(() => props.dataSource?.execId);
const execName = computed(() => props.dataSource?.execName);
const execByName = computed(() => props.dataSource?.execByName);
const execDate = computed(() => props.dataSource?.execDate);
const scriptId = computed(() => props.dataSource?.scriptId);
const scriptName = computed(() => props.dataSource?.scriptName);

const showContent = computed(() => {
  return execResult.value || execId.value || execName.value || execByName.value || execDate.value || scriptId.value || scriptName.value;
});

const RESULT_COLOR: {
  SUCCESS: string;
  FAILED: string;
} = {
  SUCCESS: '#52C41A',
  FAILED: '#F5222D'
};
</script>

<template>
  <Toggle>
    <template #title>
      <div class="text-3">{{ t('task.testing.execResult.title') }}</div>
    </template>

    <template #default>
      <div v-if="showContent && props.largePageLayout===false" class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execResult') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="flex items-center">
              <em
                :style="'background-color:' + RESULT_COLOR[execResult?.value]"
                class="block not-italic w-1.5 h-1.5 mr-1 rounded flex-shrink-0"></em>
              <span class="truncate">{{ execResult?.message }}</span>
            </div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execId') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ execId }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              <RouterLink
                v-if="execId"
                :to="`/execution/info/${execId}`"
                target="_blank"
                class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
                {{ execName }}
              </RouterLink>
              <template v-else>
                {{ execName }}
              </template>
            </div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execByName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ execByName }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.scriptId') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ scriptId }}</div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.scriptName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              <RouterLink
                v-if="scriptId"
                :to="`/script/detail/${scriptId}?type=view`"
                target="_blank"
                class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
                {{ scriptName }}
              </RouterLink>
              <template v-else>
                {{ scriptName }}
              </template>
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.lastExecTime') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ execDate }}</div>
          </div>
        </div>
      </div>

      <div v-else-if="showContent && props.largePageLayout===true" class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <div class="flex items-start space-x-5">
          <div class="relative w-1/3 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execResult') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="flex items-center">
              <em
                :style="'background-color:' + RESULT_COLOR[execResult?.value]"
                class="block not-italic w-1.5 h-1.5 mr-1 rounded flex-shrink-0"></em>
              <span class="truncate">{{ execResult?.message }}</span>
            </div>
          </div>

          <div class="relative w-1/3 flex items-start">
            <div class="w-12.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execId') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ execId }}</div>
          </div>

          <div class="relative w-1/3 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              <RouterLink
                v-if="execId"
                :to="`/execution/info/${execId}`"
                target="_blank"
                class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
                {{ execName }}
              </RouterLink>
              <template v-else>
                {{ execName }}
              </template>
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/3 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.execByName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ execByName }}</div>
          </div>

          <div class="relative w-1/3 flex items-start">
            <div class="w-12.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.scriptId') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ scriptId }}</div>
          </div>

          <div class="relative w-1/3 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.scriptName') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              <RouterLink
                v-if="scriptId"
                :to="`/script/detail/${scriptId}?type=view`"
                target="_blank"
                class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
                {{ scriptName }}
              </RouterLink>
              <template v-else>
                {{ scriptName }}
              </template>
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/3 flex items-start">
            <div class="w-21.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('task.testing.execResult.fields.lastExecTime') }}</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ execDate }}</div>
          </div>
        </div>
      </div>

      <NoData
        v-else
        size="small"
        class="my-10" />
    </template>
  </Toggle>
</template>

<style scoped>
.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.w-1\/3 {
  width: calc((100% - 40px)/3);
}
</style>
