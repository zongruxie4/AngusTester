<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Hints, Icon, Input } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { apis } from '@/api/altester';

interface Props {
  data?: {
    url: string;
    description?: string
  };
  id: string;
}
const emits = defineEmits<{(e: 'update:data', value: {url: string;description?: string}): void}>();

const url = ref();
const description = ref();
const editable = ref(false);

const props = withDefaults(defineProps<Props>(), {
  data: () => ({ url: '' }),
  id: ''
});

const editDoc = () => {
  editable.value = true;
};

const cancel = () => {
  editable.value = false;
  url.value = props.data.url;
  description.value = props.data.description;
};

const saveDoc = async () => {
  const externalDocs = { ...props.data, url: url.value, description: description.value };
  const [error] = await apis.updateApi([{ id: props.id, externalDocs }]);
  if (error) {
    return;
  }
  emits('update:data', externalDocs);
  editable.value = false;
};

watch(() => props.data, newValue => {
  url.value = newValue.url;
  description.value = newValue.description;
});

</script>
<template>
  <div class="pl-2">
    <Hints text="引用外部资源作为当前接口文档扩展" />
    <div class="flex">
      <div>
        <Input
          v-model:value="url"
          :maxLength="100"
          placeholder="链接地址"
          :disabled="!editable" />
        <Input
          v-model:value="description"
          :disabled="!editable"
          :maxLength="100"
          type="textarea"
          placeholder="外部链接描述"
          class="mt-1" />
      </div>
      <Icon
        icon="icon-shuxie"
        class="text-4 ml-1 cursor-pointer text-text-link"
        @click="editDoc" />
    </div>
    <template v-if="editable">
      <Button
        type="link"
        size="small"
        @click="saveDoc">
        保存
      </Button>
      <Button
        type="link"
        size="small"
        @click="cancel">
        取消
      </Button>
    </template>
  </div>
</template>
