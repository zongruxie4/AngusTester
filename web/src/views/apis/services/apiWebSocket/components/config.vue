<script lang="ts" setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { Form, FormItem } from 'ant-design-vue';
import { Input } from '@xcan-angus/vue-ui';

interface Props {
  id: string;
  value: Record<string, string>;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  value: () => ({})
});

const emits = defineEmits<{(e: 'update:value', value: Record<string, string>): void}>();
const form = reactive({
  connectTimeout: '60000',
  maxReconnections: '0',
  reconnectionInterval: '200'
});
const formRef = ref();
const rules = {
  connectTimeout: [{
    required: true, message: '请输入等待超时时间', trigger: 'blur'
  }],
  maxReconnections: [{
    required: true, message: '请输入重连次数', trigger: 'change'
  }],
  reconnectionInterval: [{
    required: true, message: '请输入重连次间隔时间', trigger: 'change'
  }]
};

watch(() => form, () => {
  emits('update:value', form);
}, {
  deep: true
});

onMounted(() => {
  form.connectTimeout = props.value?.connectTimeout || '0';
  form.maxReconnections = props.value?.maxReconnections || '0';
  form.reconnectionInterval = props.value?.reconnectionInterval || '1000';
});

</script>
<template>
  <Form
    ref="formRef"
    :model="form"
    labelAlign="left"
    :labelCol="{span: 22}"
    :rules="rules">
    <FormItem name="connectTimeout">
      <template #label>
        <p class="text-3">连接超时<span class="text-gray-text">(指定客户端和服务器建立连接的最长等待时间)</span></p>
      </template>
      <Input
        v-model:value="form.connectTimeout"
        :maxlength="40"
        :allowClear="false"
        dataType="number"
        class="rounded"
        placeholder="请输入超时时间">
        <template #suffix>
          <span>ms</span>
        </template>
      </Input>
    </FormItem>
    <FormItem name="maxReconnections">
      <template #label>
        <p class="text-3">重新连接次数<span class="text-gray-text">(连接关闭时尝试重新连接次数，最大允许重连10次)</span></p>
      </template>
      <Input
        v-model:value="form.maxReconnections"
        :max="10"
        :min="0"
        :allowClear="false"
        dataType="number"
        class="rounded"
        placeholder="请输入重新连接次数" />
    </FormItem>
    <FormItem name="reconnectionInterval">
      <template #label>
        <p class="text-3">重连间隔时间<span class="text-gray-text">(连接失败时的重试间隔，默认为200毫秒，允许的最大时间为30分钟)</span></p>
      </template>
      <Input
        v-model:value="form.reconnectionInterval"
        class="rounded-border"
        dataType="number"
        placeholder="请输入重连间隔时间"
        :allowClear="false">
        <template #suffix>
          <span>ms</span>
        </template>
      </Input>
    </FormItem>
  </Form>
</template>
