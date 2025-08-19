<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../../PropsType';

const { t } = useI18n();

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const steps = computed(() => {
  return props.dataSource?.content?.cases?.steps || [];
});

const len = computed(() => {
  let _len = 0;
  if (steps.value?.length) {
    _len = steps.value?.length - 1;
  }
  return _len;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a6" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.5') }}<em class="inline-block w-0.25"></em>测试步骤</span>
    </h1>

    <div v-if="len>0" class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-22 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          编号
        </div>
        <div
          class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          测试步骤
        </div>
        <div
          class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5">
          预期结果
        </div>
      </div>

      <div
        v-for="(item,index) in steps"
        :key="item.id"
        :class="{'border-b':index<len}"
        class="flex border-solid border-border-input">
        <div class="w-22 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ index+1 }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.step }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ item.expectedResult }}
        </div>
      </div>
    </div>

    <div v-else class="content-text-container">无</div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
