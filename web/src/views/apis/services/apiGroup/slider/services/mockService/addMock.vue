<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Input, Modal, notification, Select, Spin, TreeSelect } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem } from 'ant-design-vue';
import { TESTER, site } from '@xcan-angus/tools';
import { mock, services } from '@/api/altester';
import type { Rule } from 'ant-design-vue/es/form';

import ApiList from '@/views/mock/add/apiList.vue';

interface Props {
  visible: boolean;
  serviceId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  serviceId: undefined
});

const emits = defineEmits<{(e:'update:visible', value:boolean):void;
 (e:'reload', value:string):void;
}>();

const isPrivate = ref(false);
const formRef = ref();
const projectDetail = ref();

const formState = ref<{
  name: string;
  serviceDomainUrl:string;
  servicePort:string;
  nodeId: string | undefined;
  serviceId: string;
  apiIds: string[];
}>({
  name: '',
  serviceDomainUrl: '',
  servicePort: '',
  nodeId: undefined,
  serviceId: props.serviceId,
  apiIds: []
});

const loading = ref(false);

const domainRegex = /^(?=.{1,253}$)([a-z0-9]|[a-z0-9][a-z0-9\\-]{0,61}[a-z0-9])\.angusmock\.cloud$/;
const serviceDomainValidate = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请输入域名'));
  } else if (!domainRegex.test(value + '.angusmock.cloud')) {
    return Promise.reject(new Error('请输入正确的域名'));
  } else {
    return Promise.resolve();
  }
};

const rules = computed(() => {
  const baseRule = {
    name: [{ required: true, message: '请输入名称', trigger: 'change' }],
    servicePort: [{ required: true, message: '请输入端口（1~65535）', trigger: 'change' }],
    nodeId: [{ required: true, message: '请选择节点', trigger: 'change' }]
  };

  const privateRule = {
    ...baseRule
  };

  const publicRule:Record<string, any> = {
    ...baseRule,
    serviceDomainUrl: [{ required: true, validator: serviceDomainValidate, trigger: 'change' }]
  };

  return projectDetail.value?.mockServiceId ? {} : isPrivate.value ? privateRule : publicRule;
});

const treeSelectChange = (id: string) => {
  formState.value.serviceId = id;
};

const handleSave = () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    let addParams:any = {
      name: formState.value.name,
      serviceDomainUrl: !isPrivate.value ? formState.value.serviceDomainUrl + '.angusmock.cloud' : formState.value.serviceDomainUrl,
      servicePort: formState.value.servicePort,
      nodeId: formState.value.nodeId,
      serviceId: formState.value.serviceId
    };

    if (formState.value.apiIds?.length) {
      addParams = { ...addParams, apiIds: formState.value.apiIds };
    }

    const updateParams = { id: projectDetail.value?.mockServiceId, apiIds: formState.value.apiIds };
    const [error, { data }] = projectDetail.value?.mockServiceId ? await mock.patchService(updateParams) : await mock.addAngusService(addParams);
    loading.value = false;
    if (error) { return; }
    notification.success(projectDetail.value?.mockServiceId ? '更新成功' : '添加成功');
    emits('update:visible', false);
    emits('reload', data.id);
    reset();
  }, () => { /** */ });
};

const cancel = () => {
  emits('update:visible', false);
  reset();
};

const reset = () => {
  formRef.value.clearValidate();
  formState.value = {
    name: '',
    serviceDomainUrl: '',
    servicePort: '',
    nodeId: undefined,
    serviceId: projectDetail.value?.id,
    apiIds: []
  };
};

const loadProjectInfo = async () => {
  loading.value = true;
  const [error, { data }] = await services.loadInfo(props.serviceId);
  loading.value = false;
  if (error) {
    return;
  }
  projectDetail.value = data;
};

watch(() => props.visible, (newValue) => {
  if (newValue && props.serviceId !== projectDetail.value?.id) {
    loadProjectInfo();
  }
}, {
  deep: true,
  immediate: true
});

onMounted(async () => {
  isPrivate.value = await site.isPrivate();
});
</script>
<template>
  <Modal
    title="生成Mock服务"
    :visible="props.visible"
    :width="760"
    :footer="null"
    @cancel="cancel">
    <Spin :spinning="loading" :delay="0">
      <Form
        ref="formRef"
        size="small"
        class="ml-3.5 mt-3.5"
        :model="formState"
        :rules="rules">
        <div class="flex">
          <div>
            <FormItem label="名称" required />
            <FormItem
              label="域名"
              :required="!isPrivate"
              :class="isPrivate?'pl-2.25':''" />
            <FormItem label="端口" required />
            <FormItem label="节点" required />
            <FormItem label="服务" required />
            <template v-if="formState.serviceId && projectDetail?.hasApisFlag">
              <FormItem label="接口" />
            </template>
          </div>
          <div class="w-150">
            <FormItem name="name">
              <Input
                v-model:value="formState.name"
                placeholder="服务标识命名信息，最多允许100个字符"
                :disabled="projectDetail?.mockServiceId"
                :maxlength="100" />
            </FormItem>
            <FormItem name="serviceDomainUrl">
              <Input
                v-model:value="formState.serviceDomainUrl"
                :disabled="projectDetail?.mockServiceId"
                placeholder="为服务设置域名后，您可以通过域名访问Mock接口">
                <template v-if="!isPrivate && !projectDetail?.mockServiceId" #addonAfter>
                  <span>.angusmock.cloud</span>
                </template>
              </Input>
            </FormItem>
            <FormItem name="servicePort">
              <Input
                v-model:value="formState.servicePort"
                dataType="number"
                placeholder="服务所监听的端口，服务添加后不允许修改(1~65535)"
                :disabled="projectDetail?.mockServiceId"
                :min="1"
                :max="65535" />
            </FormItem>
            <FormItem name="nodeId">
              <Select
                v-model:value="formState.nodeId"
                :disabled="projectDetail?.mockServiceId"
                :action="`${TESTER}/node/search?`"
                :fieldNames="{label:'name',value:'id'}"
                :maxlength="100"
                showSearch
                placeholder="服务所运行的节点，服务添加后不允许修改"
                size="small">
                <template #option="item">
                  {{ `${item.name} ( ${item.ip} )` }}
                </template>
              </Select>
            </FormItem>
            <FormItem name="serviceId">
              <TreeSelect
                :action="`${TESTER}/services/search`"
                :fieldNames="{label:'name',value:'id'}"
                :defaultValue="projectDetail"
                :virtual="false"
                size="small"
                disabled
                showSearch
                allowClear
                placeholder="选择或查询服务"
                @change="treeSelectChange">
                <template #title="item">
                  <div class="text-3 leading-3 flex items-center h-6.5">
                    <label class="w-4 h-4 leading-4 rounded-full text-white text-center mr-1" :class="`bg-blue-badge-s`">S</label>
                    <div :title="item.name" class="truncate">{{ item.name }}</div>
                  </div>
                </template>
              </TreeSelect>
            </FormItem>
            <template v-if="formState.serviceId && projectDetail?.hasApisFlag">
              <FormItem name="apiIds">
                <ApiList
                  v-model:apiIds="formState.apiIds"
                  :serviceId="formState.serviceId" />
              </FormItem>
            </template>
          </div>
        </div>
      </Form>
      <div class="flex justify-end space-x-5 mt-5">
        <Button size="small" @click="cancel">取消</Button>
        <Button
          size="small"
          type="primary"
          @click="handleSave">
          确定
        </Button>
      </div>
    </Spin>
  </Modal>
</template>
