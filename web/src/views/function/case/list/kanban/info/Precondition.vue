<script setup lang="ts">
import { computed, onMounted, ref, watch, defineAsyncComponent } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { funcCase } from '@/api/tester';

import { CaseInfoEditProps } from '@/views/function/case/list/types';

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

const descRichRef = ref();
const openFlag = ref(true);
const editFlag = ref(false);
const content = ref<string>('');

const toEdit = () => {
  openFlag.value = true;
  editFlag.value = true;
};

const cancel = () => {
  editFlag.value = false;
};

const descErr = ref(false);

const validateDesc = () => {
  return descRichRef.value.getLength() <= 2000;
};

const ok = async () => {
  if (validateDesc()) {
    descErr.value = true;
    return;
  }
  descErr.value = false;

  const params = [{ id: caseId.value, precondition: content.value }];
  emit('loadingChange', true);
  const [error] = await funcCase.updateCase(params);
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
  <div class="mt-4">
    <div class="flex items-center text-theme-title mb-1.75">
      <span class="font-semibold">
        {{ t('functionCase.kanbanView.infoPrecondition.title') }}
      </span>

      <Button
        v-if="props.canEdit"
        v-show="!editFlag"
        type="link"
        class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
        @click="toEdit">
        <Icon icon="icon-shuxie" class="text-3.5" />
      </Button>
    </div>

    <AsyncComponent :visible="editFlag">
      <div v-show="editFlag">
        <RichEditor
          ref="descRichRef"
          v-model:value="content"
          :height="80" />
        <div v-show="descErr" class="text-status-error">
          {{ t('functionCase.kanbanView.infoPrecondition.maxCharError') }}
        </div>

        <div class="mt-2.5 space-x-2.5 w-full flex items-center justify-end">
          <Button size="small" @click="cancel">{{ t('actions.cancel') }}</Button>
          <Button
            size="small"
            type="primary"
            @click="ok">
            {{ t('actions.confirm') }}
          </Button>
        </div>
      </div>
    </AsyncComponent>

    <AsyncComponent :visible="!editFlag">
      <div v-show="!editFlag">
        <!-- {{ content }} -->
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
