<script lang="ts" setup>
import { computed, inject, reactive, ref, watch } from 'vue';
import { Hints, Input, notification } from '@xcan-angus/vue-ui';
import { unarchived } from 'src/api/tester';
import { Button, Form, FormItem } from 'ant-design-vue';

interface Props {
  disabled:boolean
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false
});

// 更新左侧未归档列表
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshUnarchived = inject('refreshUnarchived', () => {});
const projectInfo = inject('projectInfo', ref({ id: '' }));

const closeFunCallBack = (_params: unknown) => ({ _params });
const isLoading = ref(false); // 提交中。。。

const state = reactive({
  id: '',
  treeDataList: []
});
const form = reactive({
  summary: '',
  ownerId: '',
  ownerName: '',
  description: '',
  assertions: [],
  authentication: {},
  requestBody: {
    apiContentRawType: '',
    apiContentType: '',
    formParam: [],
    rawContent: ''
  },
  host: '',
  method: '',
  protocol: '',
  requestHeaders: []
});

const disabled = computed(() => {
  return props.disabled;
});

type GetParameter = ()=>Record<string, any>
const getParameter = inject('getParameter') as GetParameter;
const globalConfigs = inject('globalConfigs', { VITE_API_SUMMARY_MAX_LENGTH: 400, VITE_API_CODE_MAX_LENGTH: 400, VITE_API_DESC_MAX_LENGTH: 20000 });
// const tenantInfo = inject('tenantInfo', ref());

state.id = inject('id', ''); // 当前 api id;
const handleCloseDrawer = inject('selectHandle', closeFunCallBack);
const isUnarchivedApi = inject('isUnarchivedApi', { value: false }); // 当前 api 是否为未存档
const setUnarchivedApiInfo = inject('setUnarchivedApiInfo', (info) => (info));
const userInfo = inject('tenantInfo', ref({ id: '', fullName: '' }));

const loadInfo = async () => {
  const [error, res] = await unarchived.loadInfo(state.id);
  if (error) {
    return;
  }
  Object.keys(res.data).forEach(key => {
    if (key === 'status') {
      form[key] = res.data[key]?.value || 'UNKNOWN';
    } else {
      form[key] = res.data[key];
    }
  });
  form.assertions = res.data?.assertions?.map(item => ({
    ...item,
    condition: item.condition?.value,
    type: item.type?.value
  })) || [];
  if (!form.ownerId && userInfo.value.id) {
    form.ownerId = userInfo.value.id;
    form.ownerName = userInfo.value.fullName;
  }
};

watch(() => state.id, async () => {
  if (state.id) {
    loadInfo();
  }
}, {
  deep: true,
  immediate: true
});

const rules = {
  summary: [{
    required: true, message: '请输入接口名称，100字符以内', trigger: 'blur'
  }]
};

const formRef = ref();

const save = async () => {
  formRef.value.validate().then(async () => {
    const formParams = await getParameter();
    const { summary, description } = form;
    const params = { ...formParams, summary, description, projectId: projectInfo.value?.id };
    isLoading.value = true;
    const [error, res] = isUnarchivedApi.value && state.id
      ? await unarchived.updateApi({ dto: [{ ...params }] })
      : await unarchived.addApi({ dto: [{ ...params }] });
    isLoading.value = false;
    if (error) {
      return;
    }
    if (!state.id) {
      refreshUnarchived();
    }
    setUnarchivedApiInfo({
      id: res.data?.[0].id || state.id,
      name: summary
    });
    notification.success('保存成功');
    handleClose();
  });
};

// 取消保存
const handleClose = () => {
  handleCloseDrawer(null);
};

</script>

<template>
  <div class="py-3">
    <Hints
      text="未归档接口为用户临时调试接口，只对添加用户可见"
      class="mb-2" />
    <div v-if="state.id" class="mb-2">ID:  {{ state.id }}</div>
    <Form
      ref="formRef"
      layout="vertical"
      :model="form"
      :rules="rules">
      <FormItem label="接口名称" name="summary">
        <Input
          v-model:value="form.summary"
          :maxlength="globalConfigs.VITE_API_SUMMARY_MAX_LENGTH"
          :disabled="disabled"
          :allowClear="false"
          class="rounded"
          size="small"
          placeholder="请输入接口名称，40字符以内" />
      </FormItem>
      <FormItem label="描述" name="externalDocs">
        <Input
          v-model:value="form.description"
          type="textarea"
          showCount
          :autoSize="{ minRows: 5, maxRows: 5 }"
          :disabled="disabled"
          :allowClear="false"
          :maxlength="globalConfigs.VITE_API_DESC_MAX_LENGTH"
          class="rounded-border"
          size="small"
          placeholder="限制输入20000字符以内，支持使用markdown语法" />
      </FormItem>
      <FormItem class="mt-5">
        <Button
          type="primary"
          class="mr-2"
          :loading="isLoading"
          :disabled="disabled"
          size="small"
          @click="save">
          保存
        </Button>
        <Button size="small" @click="handleClose">
          取消
        </Button>
      </FormItem>
    </Form>
  </div>
</template>
