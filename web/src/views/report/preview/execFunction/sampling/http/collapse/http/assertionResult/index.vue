<script setup lang="ts">
import { computed } from 'vue';
import { utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

import { ExecContent } from '../../../PropsType';

export interface Props {
  value: ExecContent;
  ignoreAssertions: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  ignoreAssertions: undefined
});

const assertions = computed(() => {
  return props.value?.content?.assertions?.map(item => {
    return {
      id: utils.uuid(),
      ...item
    };
  }) || [];
});

const statusMap = computed((): { [key: string]: 'Disabled' | 'Ignored' | 'Success' | 'Failed' } => {
  return assertions.value.reduce((prev, cur) => {
    if (props.ignoreAssertions) {
      prev[cur.id] = 'Ignored';
    } else if (cur.enabled === false) {
      prev[cur.id] = 'Disabled';
    } else if (cur?.ignore === true) {
      prev[cur.id] = 'Ignored';
    } else if (cur.result?.failure === true) {
      prev[cur.id] = 'Failed';
    } else {
      prev[cur.id] = 'Success';
    }

    return prev;
  }, {} as { [key: string]: 'Disabled' | 'Ignored' | 'Success' | 'Failed' });
});
</script>
<template>
  <div class="px-2.5 pt-2">
    <div class="font-semibold mb-2">{{ t('reportPreview.execFunction.sampling.collapse.assertionResult.title') }}</div>
    <div v-if="!assertions.length" class="leading-5 mb-3.5">{{ t('reportPreview.execFunction.sampling.collapse.assertionResult.none') }}</div>
    <template v-else>
      <div
        v-for="item in assertions"
        :key="item.id"
        class="mb-1.5 last:mb-0">
        <div class="flex items-center mb-2">
          <div class="flex items-center break-all whitespace-pre-wrap mr-5">
            <em class="block w-1 h-1 mr-2 bg-gray-500 rounded"></em>
            <span>{{ item.name }}</span>
          </div>
          <div
            v-if="statusMap[item.id] === 'Failed'"
            class="flex items-center flex-shrink-0"
            style="color:rgba(255, 129, 0, 100%);">
            <span class="flex-shrink-0 mr-1.5">
              <svg
                t="1725161235180"
                class="icon"
                viewBox="0 0 1024 1024"
                version="1.1"
                xmlns="http://www.w3.org/2000/svg"
                p-id="14096"
                width="14"
                height="14"><path
                  d="M512 62c248.55428585 0 450 201.44571415 450 450S760.55428585 962 512 962 62 760.55428585 62 512 263.44571415 62 512 62zM349.39571417 296.02571417a37.28571415 37.28571415 0 0 0-50.94 1.6714283l-0.75857168 0.75857168-1.67142832 1.78714336a37.28571415 37.28571415 0 0 0 1.67142832 50.94L458.51428585 512 297.69714249 672.81714249a37.28571415 37.28571415 0 0 0-2e-8 52.72714336l0.75857168 0.75857168a37.28571415 37.28571415 0 0 0 52.71428585 0L512 565.46000001l160.81714249 160.8428575 1.8 1.67142834a37.28571415 37.28571415 0 0 0 50.92714336-1.67142832l0.75857168-0.75857168 1.67142832-1.78714336a37.28571415 37.28571415 0 0 0-1.67142832-50.94L565.46000001 512l160.8428575-160.81714249a37.28571415 37.28571415 0 0 0 2e-8-52.72714336l-0.75857168-0.75857168a37.28571415 37.28571415 0 0 0-52.71428585 0L511.97428583 458.51428585 351.19571415 297.69714249z"
                  fill="#ff8100"
                  p-id="14097"></path></svg>
            </span>
            <span>{{ t('reportPreview.execFunction.sampling.collapse.assertionResult.failed') }}</span>
          </div>
          <div
            v-else-if="statusMap[item.id] === 'Success'"
            class="flex items-center flex-shrink-0"
            style="color:#52c41a;">
            <span class="flex-shrink-0 mr-1.5">
              <svg
                t="1725161311637"
                class="icon"
                viewBox="0 0 1024 1024"
                version="1.1"
                xmlns="http://www.w3.org/2000/svg"
                p-id="14128"
                width="14"
                height="14"><path
                  d="M512 62C262.77714249 62 62 262.77714249 62 512c0 249.23571415 200.77714249 450 450 450 249.23571415 0 450-200.76428585 450-450C962 262.77714249 761.23571415 62 512 62z m254.97 345.16285753l-276.50571417 272.95714247c-8.02285753 7.92-20.04428585 11.88-32.06571415 11.88-12.02142832 0-24.04285751-3.96-32.05285753-11.86714249L254.02142832 510.01999999c-16.03285753-15.81428585-16.03285753-47.46857167 0-63.29571416 16.03285753-15.81428585 48.08571417-15.81428585 64.11857167 0l140.25857169 138.47142834 244.45285665-241.3285717c16.03285753-15.81428585 48.08571417-15.81428585 64.11857167 2e-8 20.04428585 15.82714247 20.04428585 47.46857167 0 63.29571504z"
                  fill="#52c41a"
                  p-id="14129"></path></svg>
            </span>
            <span>{{ t('reportPreview.execFunction.sampling.collapse.assertionResult.passed') }}</span>
          </div>
          <div v-else-if="statusMap[item.id] === 'Ignored'" class="flex items-center flex-shrink-0">
            <span
              class="inline-block w-2 h-2 mr-1.5 rounded-md"
              style="background-color:rgba(170, 170, 170, 100%);"></span>
            <span>{{ t('reportPreview.execFunction.sampling.collapse.assertionResult.ignored') }}</span>
          </div>
          <div v-else-if="statusMap[item.id] === 'Disabled'" class="flex items-center flex-shrink-0">
            <span
              class="inline-block w-2 h-2 mr-1.5 rounded-md"
              style="background-color:rgba(170, 170, 170, 100%);"></span>
            <span>{{ t('reportPreview.execFunction.sampling.collapse.assertionResult.notEnabled') }}</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
