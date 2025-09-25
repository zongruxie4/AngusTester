<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { FormItem, Form } from 'ant-design-vue';
import { useRoute } from 'vue-router';
import { Icon, Input, Grid } from '@xcan-angus/vue-ui';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { useI18n } from 'vue-i18n';

interface Props {
  scriptInfo: Record<string, any> | undefined;
}

const props = withDefaults(defineProps<Props>(), {
  scriptInfo: undefined
});

const { t } = useI18n();

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
    label: t('ftpPlugin.performanceTestDetail.scriptInfo.scriptName'),
    dataIndex: 'name'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.scriptInfo.scriptType'),
    dataIndex: 'typeName'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.scriptInfo.plugin'),
    dataIndex: 'plugin'
  },
  {
    label: t('common.source'),
    dataIndex: 'sourceName'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.scriptInfo.creator'),
    dataIndex: 'createdBy'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.scriptInfo.createTime'),
    dataIndex: 'createdDate'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.scriptInfo.lastModifier'),
    dataIndex: 'lastModifiedByName'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.scriptInfo.lastModifyTime'),
    dataIndex: 'lastModifiedDate'
  },
  {
    label: t('common.description'),
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
          :label="t('ftpPlugin.performanceTestDetail.scriptInfo.scriptType')"
          name="type"
          :rules="[{required: true, message: t('ftpPlugin.performanceTestDetail.scriptInfo.form.scriptTypeRequired')}]">
          <SelectEnum
            v-model:value="formData.type"
            enumKey="ScriptType"
            :placeholder="t('ftpPlugin.performanceTestDetail.scriptInfo.form.scriptTypePlaceholder')" />
        </FormItem>
        <FormItem
          :label="t('ftpPlugin.performanceTestDetail.scriptInfo.scriptName')"
          name="name"
          size="small"
          :rules="[{required: true, message: t('ftpPlugin.performanceTestDetail.scriptInfo.form.scriptNameRequired')}]">
          <Input
            v-model:value="formData.name"
            :maxlength="200"
            :placeholder="t('ftpPlugin.performanceTestDetail.scriptInfo.form.scriptNamePlaceholder  ')" />
        </FormItem>
        <FormItem
          :label="t('common.description')"
          name="description">
          <Input
            v-model:value="formData.description"
            type="textarea"
            :placeholder="t('ftpPlugin.performanceTestDetail.scriptInfo.form.descriptionPlaceholder')"
            :showCount="!!formData.description"
            :autoSize="{ minRows: 4, maxRows: 6 }"
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
