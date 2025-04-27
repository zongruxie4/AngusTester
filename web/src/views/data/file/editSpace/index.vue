<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { Input, Modal, SelectEnum } from '@xcan-angus/vue-ui';
import { space } from '@/api/storage';
import { Form, FormItem } from 'ant-design-vue';

interface FormState {
  name: string,
  quotaSize: {
    value?: string,
    unit: string
  },
  remark: string,
  id?: string
}

interface Props {
  visible: boolean;
  id?: string|undefined;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: undefined
});

const emits = defineEmits<{(e: 'ok', form: Record<string, any>): void, (e: 'update:visible', value: boolean):void}>();

const fromRef = ref();

const form = reactive<FormState>({
  name: '',
  quotaSize: {
    value: undefined,
    unit: 'Megabytes'
  },
  remark: ''
});

const resetForm = () => {
  form.name = '';
  form.quotaSize = {
    value: undefined,
    unit: 'Megabytes'
  };
  form.remark = '';
  form.id = undefined;
};

const loadDetail = async () => {
  const id = props.id;
  if (!id) {
    return;
  }

  const [error, { data }] = await space.loadDetail(id);
  if (error) {
    return;
  }

  const { name, quotaSize, remark } = data;

  if (quotaSize) {
    form.quotaSize = {
      ...quotaSize,
      unit: quotaSize.unit.value
    };
  } else {
    form.quotaSize = {
      unit: 'Megabytes',
      value: undefined
    };
  }

  form.name = name;
  form.remark = remark;
  form.id = id;
};

const onSizeBlur = () => {
  if (form.quotaSize.unit === 'Megabytes') {
    if (form.quotaSize.value && +form.quotaSize.value < 100) {
      form.quotaSize.value = '100';
    }
  }
  if (form.quotaSize.unit === 'Gigabytes') {
    if (form.quotaSize.value && +form.quotaSize.value < 0.1) {
      form.quotaSize.value = '0.1';
    }
  }
  if (form.quotaSize.unit === 'Terabytes') {
    if (form.quotaSize.value && +form.quotaSize.value < 0.0001) {
      form.quotaSize.value = '0.0001';
    }
  }
};

const confirm = () => {
  fromRef.value.validateFields().then(() => {
    const quotaSize = form.quotaSize;
    emits('ok', { ...form, quotaSize: quotaSize.value ? form.quotaSize : undefined });
  });
};

const closeModal = () => {
  emits('update:visible', false);
};

watch(() => props.visible, newValue => {
  if (newValue && props.id) {
    loadDetail();
  } else {
    resetForm();
  }
}, {
  immediate: true
});

const excludes = ({ value }):boolean => {
  return ['Bytes', 'Kilobytes'].includes(value);
};
</script>
<template>
  <Modal
    title="添加空间"
    :visible="visible"
    @cancel="closeModal"
    @ok="confirm">
    <Form
      ref="fromRef"
      :model="form"
      size="small"
      layout="vertical">
      <FormItem
        label="空间名称"
        name="name"
        :rules="[{required: true, message: '输入空间名称'}]">
        <Input
          v-model:value="form.name"
          placeholder="输入空间名称,限制100字符以内"
          size="small"
          :maxlength="100" />
      </FormItem>
      <FormItem name="quotaSize">
        <template #label>
          <div class="flex items-end">
            <span>空间配额 </span><span class="text-gray-text-light text-3">(限制空间可使用存储总大小，最小100MB)</span>
          </div>
        </template>
        <Input
          v-model:value="form.quotaSize.value"
          :trimAll="true"
          :max="2000"
          :min="0"
          :decimalPoint="4"
          placeholder="输入空间配额"
          dataType="float"
          size="small"
          @blur="onSizeBlur">
          <template #addonAfter>
            <SelectEnum
              v-model:value="form.quotaSize.unit"
              style="width: 60px;"
              enumKey="DataUnit"
              :excludes="excludes" />
          </template>
        </Input>
      </FormItem>
      <FormItem label="备注">
        <Input
          v-model:value="form.remark"
          type="textarea"
          placeholder="限制200字符以内"
          showCount
          class="h-25 mb-4"
          :maxlength="200" />
      </FormItem>
    </Form>
  </Modal>
</template>
