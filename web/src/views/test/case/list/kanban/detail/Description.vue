<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';

import { CaseInfoEditProps } from '@/views/test/case/list/types';

const RichEditor = defineAsyncComponent(() => import('@/components/editor/richEditor/index.vue'));

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

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
  <div class="section-container">
    <div class="section-header">
      <h3 class="section-title">{{ t('common.description') }}</h3>
      <Button
        v-if="props.canEdit"
        v-show="!editFlag"
        type="link"
        class="edit-btn"
        @click="openEditDescription">
        <Icon icon="icon-shuxie" />
      </Button>
    </div>

    <AsyncComponent :visible="editFlag">
      <div v-show="editFlag">
        <div class="mb-2.5 border border-gray-200">
          <RichEditor
            ref="richRef"
            :value="content"
            :options="{theme: 'bubble', placeholder: t('common.placeholders.inputDescription30')}"
            @change="handleEditorChange" />
          <div v-show="descrError" class="text-status-error">
            {{ t('testCase.messages.richTextTooLong') }}
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
/* Section container styles */
.section-container {
  margin-top: 0;
  margin-left: -20px;
  margin-right: -20px;
}

/* Section header styles */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Edit button styles */
.edit-btn {
  flex-shrink: 0;
  padding: 0;
  height: 16px;
  width: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  color: #1890ff !important;
  cursor: pointer;
  transition: color 0.2s;
}

.edit-btn:focus {
  color: #1890ff !important;
  background: none !important;
  border: none !important;
  box-shadow: none !important;
}

.edit-btn:hover {
  color: #1890ff;
}

.edit-btn .anticon {
  font-size: 12px;
}
</style>

<style scoped>
.border-none {
  border: none;
}
</style>
