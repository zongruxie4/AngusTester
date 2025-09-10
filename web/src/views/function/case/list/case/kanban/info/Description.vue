<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { funcCase } from '@/api/tester';

import { CaseInfo } from '../types';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: CaseInfo;
  canEdit: boolean;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
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

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

const richRef = ref();

const openFlag = ref(true);
const editFlag = ref(false);
const content = ref<string>('');

const toEdit = () => {
  openFlag.value = true;
  editFlag.value = true;
  content.value = props.dataSource?.description || '';
};

const editorChange = (value: string) => {
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
  if (richRef.value.getLength() > 2000) {
    return false;
  }
  return true;
};

const ok = async () => {
  if (!validateDesc()) {
    descrError.value = true;
    return;
  }
  descrError.value = false;

  const params = [{ id: caseId.value, description: content.value }];
  emit('loadingChange', true);
  const [error] = await funcCase.updateCase(params);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  editFlag.value = false;
  emit('change');
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    content.value = newValue?.description || '';
  }, { immediate: true });
});

const caseId = computed(() => {
  return props.dataSource?.id;
});

// const error = computed(() => {
//   if (!content.value) {
//     return false;
//   }

//   return content.value.length > 2000;
// });

</script>

<template>
  <div class="mt-4">
    <div class="flex items-center text-theme-title mb-1.75">
      <span class="font-semibold">{{ t('functionCase.kanbanView.infoDescription.title') }}</span>
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
        <div>
          <RichEditor
            ref="richRef"
            :value="content"
            @change="editorChange" />
          <div v-show="descrError" class="text-status-error">{{ t('functionCase.kanbanView.infoDescription.maxCharError') }}</div>
        </div>
        <div class="mt-2.5 space-x-2.5 w-full flex items-center justify-end">
          <Button size="small" @click="cancel">{{ t('functionCase.kanbanView.infoDescription.cancel') }}</Button>
          <Button
            size="small"
            type="primary"
            @click="ok">
            {{ t('functionCase.kanbanView.infoDescription.confirm') }}
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
