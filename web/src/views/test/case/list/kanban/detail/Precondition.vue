<script setup lang="ts">
import { computed, onMounted, ref, watch, defineAsyncComponent } from 'vue';
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

const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change'): void;
}>();

const descRichRef = ref();
const openFlag = ref(true);
const editFlag = ref(false);
const content = ref<string>('');

/*
  Enter precondition edit mode.
*/
const openEditPrecondition = () => {
  openFlag.value = true;
  editFlag.value = true;
};

const cancel = () => {
  editFlag.value = false;
};

const descErr = ref(false);

/*
  Validate precondition length (<= 2000 characters).
*/
const validateDesc = () => {
  return descRichRef.value.getLength() <= 2000;
};

/*
  Validate and persist precondition, then notify parent to refresh.
*/
const savePrecondition = async () => {
  if (!validateDesc()) {
    descErr.value = true;
    return;
  }
  descErr.value = false;

  const params = [{ id: caseId.value, precondition: content.value }];
  emit('loadingChange', true);
  const [error] = await testCase.updateCase(params);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  editFlag.value = false;
  emit('change');
};

const getJson = (value) => {
  if (!value) {
    return undefined;
  }
  try {
    const result = JSON.parse(value);
    if (typeof result === 'object') {
      return value;
    }
    return JSON.stringify([{ insert: value }]);
  } catch {
    return JSON.stringify([{ insert: value }]);
  }
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    content.value = getJson(newValue?.precondition || '');
  }, { immediate: true });
});
const caseId = computed(() => {
  return props.dataSource?.id;
});
</script>

<template>
  <div class="section-container">
    <div class="section-header">
      <h3 class="section-title">{{ t('common.precondition') }}</h3>
      <Button
        v-if="props.canEdit"
        v-show="!editFlag"
        type="link"
        class="edit-btn"
        @click="openEditPrecondition">
        <Icon icon="icon-shuxie" />
      </Button>
    </div>

    <AsyncComponent :visible="editFlag">
      <div v-show="editFlag">
        <div class="mb-2.5 border border-gray-200">
          <RichEditor
            ref="descRichRef"
            v-model:value="content"
            :options="{theme: 'bubble', placeholder: t('testCase.messages.enterPrecondition')}"
            :height="80" />
        </div>
        <div v-show="descErr" class="text-status-error">
          {{ t('testCase.messages.enterPrecondition') }}
        </div>

        <div class="mt-2.5 space-x-2.5 w-full flex items-center justify-end">
          <Button size="small" @click="cancel">{{ t('actions.cancel') }}</Button>
          <Button
            size="small"
            type="primary"
            @click="savePrecondition">
            {{ t('actions.confirm') }}
          </Button>
        </div>
      </div>
    </AsyncComponent>

    <AsyncComponent :visible="!editFlag">
      <div v-show="!editFlag">
        <RichEditor
          v-model:value="content"
          mode="view" />
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
