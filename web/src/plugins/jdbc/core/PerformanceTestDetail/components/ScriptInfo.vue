<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { FormItem, Form } from 'ant-design-vue';
import { useRoute } from 'vue-router';
import { Icon, Input, SelectEnum, Grid } from '@xcan-angus/vue-ui';

interface Props {
  scriptInfo: Record<string, any> | undefined;
}

const props = withDefaults(defineProps<Props>(), {
  scriptInfo: undefined
});

const route = useRoute();
const type = ref<'info' | 'edit' | 'add'>(route.query.type as 'info' | 'edit' | 'add');

const initName = ref();
const initDescription = ref();
watch(() => route.query.type, (newValue) => {
  type.value = newValue as 'info' | 'edit' | 'add';

  if (newValue !== 'info') {
    initName.value = formData.name;
    initDescription.value = formData.description;
  } else {
    formData.name = initName.value;
    formData.description = initDescription.value;
  }
});

const isSpread = ref(true);
const formRef = ref();
const formData = reactive({
  name: '',
  plugin: '',
  type: undefined,
  description: '',
  source: '',
  createdBy: '',
  exec: '',
  createdDate: '',
  lastModifiedDate: '',
  lastModifiedByName: '',
  typeName: '',
  sourceName: ''
});

const infoConfig = [[
  {
    label: '脚本名称',
    dataIndex: 'name'
  },
  {
    label: '脚本类型',
    dataIndex: 'typeName'
  },
  {
    label: '插件',
    dataIndex: 'plugin'
  },
  {
    label: '来源',
    dataIndex: 'sourceName'
  },
  {
    label: '创建人',
    dataIndex: 'createdBy'
  },
  {
    label: '创建时间',
    dataIndex: 'createdDate'
  },
  {
    label: '最后更新人',
    dataIndex: 'lastModifiedByName'
  },
  {
    label: '最后更新时间',
    dataIndex: 'lastModifiedDate'
  },
  {
    label: '描述',
    dataIndex: 'description'
  }
]];

const validate = () => {
  return formRef.value.validate();
};

const getFormData = () => {
  const { name, type, description } = formData;
  return { name, type, description };
};

const toggle = () => {
  isSpread.value = !isSpread.value;
};

watch(() => props.scriptInfo, newValue => {
  if (newValue) {
    formData.name = newValue.name;
    formData.plugin = newValue?.plugin;
    initName.value = newValue.name;
    formData.type = newValue.type?.value;
    formData.typeName = newValue.type?.message;
    formData.description = newValue.description;
    formData.source = newValue.source?.value;
    formData.sourceName = newValue.source?.message;
    formData.createdBy = newValue.createdByName;
    formData.createdDate = newValue.createdDate;
    formData.lastModifiedDate = newValue.lastModifiedDate;
    formData.lastModifiedByName = newValue.lastModifiedByName;
  }
}, {
  deep: true,
  immediate: true
});

defineExpose({ getFormData, validate, isSpread, toggle });
</script>
<template>
  <div class="relative pt-2 pr-3.5">
    <template v-if="isSpread">
      <Form
        v-if="type !== 'info'"
        ref="formRef"
        :model="formData"
        layout="vertical"
        size="small">
        <FormItem
          label="脚本类型"
          name="type"
          :rules="[{required: true, message: '请选择脚本类型'}]">
          <SelectEnum
            v-model:value="formData.type"
            enumKey="ScriptType"
            placeholder="请选择脚本类型" />
        </FormItem>
        <FormItem
          label="脚本名称"
          name="name"
          size="small"
          :rules="[{required: true, message: '请填写脚本名称'}]">
          <Input
            v-model:value="formData.name"
            :maxlength="200"
            placeholder="请输入脚本名称,最多200字符" />
        </FormItem>
        <FormItem label="描述" name="description">
          <Input
            v-model:value="formData.description"
            placeholder="请输入脚本描述，最多800字符"
            type="textarea"
            :showCount="!!formData.description"
            :autosize="{ minRows: 4, maxRows: 6 }"
            :maxlength="800" />
        </FormItem>
      </Form>
      <Grid
        v-else
        :columns="infoConfig"
        :dataSource="formData"
        marginBottom="18px"
        fontSize="12px">
        <template #plugin="{text}">
          <template v-if="text">
            <span class="px-3 py-0.5 rounded-xl" style="background-color:rgba(0, 119, 255, 10%);color:rgba(0, 119, 255, 100%)">{{ text }}</span>
          </template>
          <template v-else>
            --
          </template>
        </template>
      </Grid>
    </template>
    <div
      class="switch-icon"
      :class="{'-left-3.25 transform rotate-180': !isSpread, '-left-0.75': isSpread}"
      @click="toggle">
      <template v-if="isSpread">
        <Icon class="icon" icon="icon-zhediejiantouyou" />
      </template>
      <template v-else>
        <Icon class="icon" icon="icon-zhediejiantouzuo" />
      </template>
    </div>
  </div>
</template>
<style scoped>
.switch-icon {
  @apply absolute bottom-20 h-12.5 cursor-pointer overflow-hidden select-none;

  z-index: 1;
}

.switch-icon .icon {
  @apply absolute left-1 top-1/2;

  transform: translateX(-50%) translateY(-50%);
}
</style>
