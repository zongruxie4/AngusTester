 <script lang="ts" setup>
import { ref, watch, onMounted } from 'vue';
import {IconText, Modal, SelectEnum, TreeSelect, notification } from '@xcan-angus/vue-ui';
import { Form, FormItem, Button } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/tools';
import { services } from '@/api/tester';

const props = withDefaults(defineProps<{visible: boolean, service?: {id: string, name: string}, projectId: string}>(), {
  visible: false,
  service: undefined,
  projectId: '',
});
const emits = defineEmits<{(e: 'update:visible', value: boolean)}>()
const formRef = ref();

const formData = ref<{
  serviceId?: string;
  sourceLanguage: 'zh_CN'| 'en',
  targetLanguage: 'zh_CN'| 'en',
}>({
  serviceId: undefined,
  sourceLanguage: 'zh_CN',
  targetLanguage: 'en'
})

const loading = ref(false);

const handleServiceId = (serviceId) => {
  formData.value.serviceId = serviceId;
};

const submit = () => {
  formRef.value.validate().then(async () => {
    const {serviceId, ...params} = formData.value;
    loading.value = true;
    const [error] = await services.translate(serviceId as string, {...params})
    loading.value = false
      if (error) {
        return;
      }
      notification.success('翻译完成');
      emits('update:visible', false);
  });
};

const cancel = () => {
  emits('update:visible', false);
};

onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (newValue) {
      loading.value = false;
      formData.value.serviceId = props.service?.id;
    }
  }, {
    immediate: true
  });
});
 </script>
 <template>
<Modal
  title="翻译"
  :visible="props.visible"
  :footer="false"
  :width="800"
  @cancel="cancel">
  <!-- 上方：OpenAPI翻译说明 -->
  <div>
    <div class="text-3.5 font-medium -mt-2">
      OpenAPI翻译说明
    </div>

    <div class="grid-container leading-7 mt-2">
      <div class="bg-gray-bg p-2 rounded border">
        <div class="font-semibold">使用场景</div>
        <ul class="list-disc pl-4">
          <li>为多语言API文档提供支持</li>
          <li>国际化API服务</li>
          <li>跨区域API服务部署</li>
          <li>多语言开发者文档生成</li>
        </ul>
      </div>

      <div class="bg-gray-bg p-2 rounded border">
        <div class="font-semibold">注意事项</div>
        <ul class="list-disc pl-4">
          <li>AngusTester默认会使用DeepSeek进行翻译</li>
          <li>翻译是异步处理的，完成后您会收到提示消息</li>
          <li>翻译过程可能需要几分钟到几小时，取决于API规模</li>
          <li>翻译完成后，您可以在"接口 → 服务"中查看</li>
        </ul>
      </div>
    </div>
  </div>

  <!-- 下方：OpenAPI翻译配置 -->
  <div class="configuration-section mt-3">
    <div class="text-3.5 font-medium">OpenAPI翻译配置</div>

    <Form
      ref="formRef"
      layout="vertical"
      class="mt-2 translate-form-wrap"
      :model="formData">
     <FormItem label="接口服务" name="serviceId" :rules="[{required: true, message: '请选择服务'}]">
       <TreeSelect
         :defaultValue="props.service"
         :action="`${TESTER}/services?projectId=${props.projectId}&hasPermission=ADD&fullTextSearch=true`"
         :allowClear="false"
         :fieldNames="{children:'children', label:'name', value: 'id'}"
         placeholder="请选择服务"
         :virtual="false"
         size="small"
         @change="handleServiceId">
         <template #title="{name, targetType}">
           <div
             class="flex items-center"
             :title="name">
             <IconText
               text="S"
               class="bg-blue-badge-s mr-2 text-3"
               style="width: 16px; height: 16px;" />
             <span class="truncate flex-1">{{ name }}</span>
           </div>
         </template>
       </TreeSelect>
     </FormItem>
      <div class="flex items-center space-x-2">
        <FormItem label="原语言" class="flex-1 mb-0">
          <SelectEnum
            v-model:value="formData.sourceLanguage"
            enumKey="SupportedLanguage">

          </SelectEnum>
        </FormItem>
        <FormItem label="目标语言" class="flex-1">
          <SelectEnum
            v-model:value="formData.targetLanguage"
            enumKey="SupportedLanguage">
          </SelectEnum>
        </FormItem>
      </div>

   </Form>
  </div>
  <div class="text-center space-x-3 mt-8">
    <Button
      @click="cancel">
      取消
    </Button>
    <Button
      @click="submit"
      type="primary">
      提交
    </Button>
  </div>
</Modal>
 </template>
 <style scoped>
 .grid-container {
   display: grid;
   grid-template-columns: 1fr 1fr;
   gap: 16px;
 }
 :deep(.translate-form-wrap) .ant-form-item-label{
   font-weight: 600;
 }
 </style>
