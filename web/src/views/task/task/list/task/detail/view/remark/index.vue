<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, Scroll } from '@xcan-angus/vue-ui';
import { http, utils, TESTER } from '@xcan-angus/tools';

type Remark = {
  content: string;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  id: string;
  taskId: string;
}

type Props = {
  id: string;
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  notify: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:notify', value: string): void;
}>();

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

const richEditorRef = ref();
const content = ref<string>('');
const dataList = ref<Remark[]>([]);

const editorChange = (value: string) => {
  content.value = value;
};

const toDelete = async (id: string) => {
  const [error] = await http.del(`${TESTER}/task/remark/${id}`);
  if (error) {
    return;
  }

  dataList.value = dataList.value.filter(item => item.id !== id);
};

const scrollChange = (data: Remark[]) => {
  dataList.value = data;
};

const isEmptyContent = (content) => {
  const values = JSON.parse(content);
  if (!values?.length) {
    return true;
  }
  if (values?.length > 1) {
    return false;
  }
  if (values.length === 1) {
    return !values[0].insert.replaceAll('\n', '');
  }
  return false;
};

const validateErr = ref(false);
const ok = async () => {
  if (!content.value) {
    return;
  }
  if (isEmptyContent(content.value)) {
    return;
  }

  if (isError()) {
    validateErr.value = true;
    return;
  }
  validateErr.value = false;

  const params = { taskId: props.id, content: content.value };
  const [error] = await http.post(`${TESTER}/task/remark`, params);
  if (error) {
    return;
  }

  content.value = '';
  emit('update:notify', utils.uuid());
};

const isError = () => {
  if (!content.value) {
    return false;
  }
  const length = richEditorRef.value.getLength();
  return length > 6000;
};

const params = computed(() => {
  return { orderBy: 'createdDate', orderSort: 'DESC', taskId: props.id };
});

</script>

<template>
  <div class="h-full overflow-auto pr-5">
    <div>
      <div class="mb-2.5">
        <RichEditor
          ref="richEditorRef"
          :value="content"
          :height="150"
          :options="{placeholder: ' 添加补充说明，如注意事项，确认不通过原因等，最大支持6000个字符'}"
          @change="editorChange" />
        <div v-show="validateErr" class="text-status-error">备注最大支持6000个字符</div>
      </div>

      <div class="space-x-2.5 w-full flex items-center justify-end">
        <Button
          size="small"
          type="primary"
          @click="ok">
          确定
        </Button>
      </div>
    </div>

    <Scroll
      :action="`${TESTER}/task/remark`"
      :hideNoData="true"
      :params="params"
      :lineHeight="56"
      :notify="props.notify"
      style="height: calc(100% - 238px);"
      transition
      @change="scrollChange">
      <div
        v-for="(item) in dataList"
        :key="item.id"
        class="mb-1.5 last:mb-0">
        <div class="flex items-center mb-0.5">
          <div class="border-2 border-theme-text-box w-2.5 h-2.5 rounded-full"></div>

          <div class="ml-3 font-normal text-3 flex items-center space-x-2 leading-4">
            <div class="text-theme-content font-medium">{{ item.createdByName }}</div>
            <div class="text-theme-content font-medium">添加了备注</div>
            <div class="text-theme-sub-content">{{ item.createdDate }}</div>
            <Icon
              icon="icon-qingchu"
              class="cursor-pointer text-theme-text-hover text-3.5"
              @click="toDelete(item.id)" />
          </div>
        </div>

        <div class="browser-container">
          <RichEditor :value="item.content" mode="view" />
        </div>
      </div>
    </Scroll>
  </div>
</template>

<style scoped>
.browser-container  {
  padding-left: 22px;
}
</style>
