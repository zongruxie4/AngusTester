<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, NoData } from '@xcan-angus/vue-ui';
import { http, TESTER } from '@xcan-angus/tools';

import { TaskInfo } from '../../../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

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

const descError = ref(false);
const descRichRef = ref();

const validateDesc = () => {
  if (descRichRef.value && descRichRef.value.getLength() > 8000) {
    return false;
  }
  return true;
};

const ok = async () => {
  if (!validateDesc()) {
    descError.value = true;
    return;
  }
  descError.value = false;

  const params = { description: content.value };
  emit('loadingChange', true);
  const [error] = await http.put(`${TESTER}/task/${taskId.value}/description`, params);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  editFlag.value = false;
  emit('change', { id: taskId.value, description: content.value });
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    content.value = newValue?.description || '';
  }, { immediate: true });
});

const taskId = computed(() => {
  return props.dataSource?.id;
});

</script>

<template>
  <div class="mt-4">
    <div class="flex items-center text-theme-title mb-1.75">
      <span>描述</span>
      <Button
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
            ref="descRichRef"
            :value="content"
            :height="300"
            @change="editorChange" />
          <div v-show="descError" class="text-status-error">描述最大支持8000个字符</div>
        </div>

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
