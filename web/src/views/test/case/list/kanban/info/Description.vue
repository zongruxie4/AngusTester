<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';

import { CaseInfoEditProps } from '@/views/test/case/list/types';

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change'): void;
}>();

const richRef = ref();

const openFlag = ref(true);
const editFlag = ref(false);
const content = ref<string>('');

/*
  Enter description edit mode and preload current content.
*/
const openEditDescription = () => {
  openFlag.value = true;
  editFlag.value = true;
  content.value = props.dataSource?.description || '';
};

/*
  Handle rich editor content change and sync to local state.
*/
const handleEditorChange = (value: string) => {
  content.value = value;
};

const cancel = () => {
  editFlag.value = false;
};

const descrError = ref(false);
const validateDesc = () => {
  if (!content.value) {
    return true;
  }
  return richRef.value.getLength() <= 2000;
};

/*
  Validate and persist description, then notify parent to refresh.
*/
const saveDescription = async () => {
  if (!validateDesc()) {
    descrError.value = true;
    return;
  }
  descrError.value = false;

  const params = [{ id: caseId.value, description: content.value }];
  emit('loadingChange', true);
  const [error] = await testCase.updateCase(params);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  editFlag.value = false;
  emit('change');
};

const caseId = computed(() => {
  return props.dataSource?.id;
});

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    content.value = newValue?.description || '';
  }, { immediate: true });
});
</script>

<template>
  <div class="mt-4">
    <div class="flex items-center text-theme-title mb-1.75">
      <span class="font-semibold">
        {{ t('common.description') }}
      </span>

      <Button
        v-if="props.canEdit"
        v-show="!editFlag"
        type="link"
        class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
        @click="openEditDescription">
        <Icon icon="icon-shuxie" class="text-3.5" />
      </Button>
    </div>

    <AsyncComponent :visible="editFlag">
      <div v-show="editFlag">
        <div>
          <RichEditor
            ref="richRef"
            :value="content"
            @change="handleEditorChange" />
          <div v-show="descrError" class="text-status-error">
            {{ t('testCase.kanbanView.infoDescription.maxCharError') }}
          </div>
        </div>

        <div class="mt-2.5 space-x-2.5 w-full flex items-center justify-end">
          <Button size="small" @click="cancel">{{ t('actions.cancel') }}</Button>
          <Button
            size="small"
            type="primary"
            @click="saveDescription">
            {{ t('actions.confirm') }}
          </Button>
        </div>
      </div>
    </AsyncComponent>

    <AsyncComponent :visible="!editFlag">
      <div v-show="!editFlag">
        <RichEditor :value="props?.dataSource?.description" mode="view" />
      </div>

      <NoData
        v-show="!editFlag&&!content?.length"
        size="small"
        class="my-10" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.border-none {
  border: none;
}
</style>
