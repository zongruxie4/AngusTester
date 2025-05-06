<script setup lang="ts">
import { computed, onMounted, ref, watch, defineAsyncComponent } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, NoData } from '@xcan-angus/vue-ui';
import { funcCase } from '@/api/tester';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: CaseInfo;
  canEdit: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change'): void;
}>();

const descRichRef = ref(false);
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
  if (descRichRef.value.getLength() > 2000) {
    return false;
  }
  return true;
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
      <span class="font-semibold">前置条件</span>
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
        <div v-show="descErr" class="text-status-error">富文本字符不能超过2000</div>
        <!-- <Input
          v-model:value="content"
          size="small"
          type="textarea"
          trim
          :autoSize="{ minRows: 6, maxRows: 6}"
          :maxlength="6000"
          placeholder="前置条件，" /> -->

        <div class="mt-2.5 space-x-2.5 w-full flex items-center justify-end">
          <Button size="small" @click="cancel">取消</Button>
          <Button
            size="small"
            type="primary"
            @click="ok">
            确定
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
