<script lang="ts" setup>
import { defineAsyncComponent, ref, onMounted, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Icon } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';

import { funcCase } from '@/api/tester';

const { t } = useI18n();

interface Props {
  caseInfo?: { precondition: string};
  contentClass: string;
  readonly: boolean;
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined,
  contentClass: '',
  readonly: true
});

const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change'): void;
}>();

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));


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
  watch(() => props.caseInfo, (newValue) => {
    content.value = getJson(newValue?.precondition || '');
  }, { immediate: true });
});
const caseId = computed(() => {
  return props?.caseInfo?.id;
});

</script>
<template>
  <div class="bg-white rounded-lg">
    <div v-show="!props.readonly" class="flex justify-end items-center text-theme-title mb-1.75">
      <Button
        v-show="!editFlag"
        type="link"
        class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none text-theme-special"
        @click="toEdit">
        <Icon icon="icon-xiugai" />
      </Button>

      <div v-show="editFlag" class="space-x-2.5 w-full flex items-center justify-end">
        <Button
          size="small"
          type="link"
          @click="cancel">{{ t('actions.cancel') }}</Button>
        <Button
          size="small"
          type="link"
          @click="ok">
          {{ t('common.confirm') }}
        </Button>
      </div>
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
      </div>
    </AsyncComponent>
    <AsyncComponent :visible="!editFlag">
      <div v-if="props.caseInfo?.precondition" v-show="!editFlag" :class="props.contentClass">
        <div class="bg-gray-50 rounded-lg p-4">
          <RichEditor :value="props.caseInfo?.precondition" mode="view" />
        </div>
      </div>
      <div
        v-else
        v-show="!editFlag"
        class="text-center py-8 text-gray-400"
        :class="props.contentClass">
        <Icon icon="icon-kong" class="text-4xl mb-2" />
        <div>{{ t('common.noData') }}</div>
      </div>
    </AsyncComponent>
  </div>
</template>
