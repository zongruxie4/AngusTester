<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { Input, Modal } from '@xcan-angus/vue-ui';
import { space } from '@/api/storage';
import { Form, FormItem } from 'ant-design-vue';
import SelectEnum from '@/components/selectEnum/index.vue';
import { useI18n } from 'vue-i18n';

import { SpaceFormState, EditSpaceProps, EditSpaceEmits } from './types';

const { t } = useI18n();

// Component props with default values
const props = withDefaults(defineProps<EditSpaceProps>(), {
  visible: false,
  id: undefined
});

// Component emits for parent communication
const emits = defineEmits<EditSpaceEmits>();

// Form reference for validation
const formRef = ref();

// Reactive form state
const form = reactive<SpaceFormState>({
  name: '',
  quotaSize: {
    value: undefined,
    unit: 'Megabytes'
  },
  remark: ''
});

/**
 * <p>Reset form to initial state.</p>
 * <p>Clears all form fields and resets to default values.</p>
 */
const resetForm = () => {
  form.name = '';
  form.quotaSize = {
    value: undefined,
    unit: 'Megabytes'
  };
  form.remark = '';
  form.id = undefined;
};

/**
 * <p>Load space details for editing existing space.</p>
 * <p>Fetches space information from API and populates form fields.</p>
 */
const loadDetail = async () => {
  const id = props.id;
  if (!id) {
    return;
  }

  const [error, { data }] = await space.getSpaceDetail(id);
  if (error) {
    return;
  }

  const { name, quotaSize, remark } = data;

  // Handle quota size configuration
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

/**
 * <p>Handle quota size input blur event.</p>
 * <p>Validates and adjusts size values based on selected unit to ensure minimum requirements.</p>
 */
const onSizeBlur = () => {
  // Validate Megabytes (minimum 100 MB)
  if (form.quotaSize.unit === 'Megabytes') {
    if (form.quotaSize.value && +form.quotaSize.value < 100) {
      form.quotaSize.value = '100';
    }
  }
  
  // Validate Gigabytes (minimum 0.1 GB)
  if (form.quotaSize.unit === 'Gigabytes') {
    if (form.quotaSize.value && +form.quotaSize.value < 0.1) {
      form.quotaSize.value = '0.1';
    }
  }
  
  // Validate Terabytes (minimum 0.0001 TB)
  if (form.quotaSize.unit === 'Terabytes') {
    if (form.quotaSize.value && +form.quotaSize.value < 0.0001) {
      form.quotaSize.value = '0.0001';
    }
  }
};

/**
 * <p>Confirm form submission.</p>
 * <p>Validates form fields and emits success event with form data.</p>
 */
const confirm = () => {
  formRef.value.validateFields().then(() => {
    const quotaSize = form.quotaSize;
    // Emit form data, excluding quotaSize if no value is set
    emits('ok', { 
      ...form, 
      quotaSize: quotaSize.value ? form.quotaSize : undefined 
    });
  });
};

/**
 * <p>Close modal and emit visibility update event.</p>
 */
const closeModal = () => {
  emits('update:visible', false);
};

/**
 * <p>Watch for modal visibility changes.</p>
 * <p>Loads space details when editing existing space or resets form for new space.</p>
 */
watch(() => props.visible, (newValue) => {
  if (newValue && props.id) {
    loadDetail();
  } else {
    resetForm();
  }
}, {
  immediate: true
});

/**
 * <p>Filter function for excluding certain data units from selection.</p>
 * <p>Prevents selection of Bytes and Kilobytes as they are too small for practical use.</p>
 * 
 * @param param - Object containing the value to check
 * @returns Boolean indicating whether the value should be excluded
 */
const excludes = ({ value }: { value: string }): boolean => {
  return ['Bytes', 'Kilobytes'].includes(value);
};
</script>

<template>
  <Modal
    :title="t('fileSpace.spaceForm.title')"
    :visible="visible"
    @cancel="closeModal"
    @ok="confirm">
    
    <!-- Space configuration form -->
    <Form
      ref="formRef"
      :model="form"
      size="small"
      layout="vertical">
      
      <!-- Space name field -->
      <FormItem
        :label="t('fileSpace.spaceForm.form.spaceName')"
        name="name"
        :rules="[{required: true, message: t('fileSpace.spaceForm.rules.spaceName')}]">
        <Input
          v-model:value="form.name"
          :placeholder="t('fileSpace.spaceForm.placeholders.spaceName')"
          size="small"
          :maxlength="100" />
      </FormItem>
      
      <!-- Quota size field -->
      <FormItem name="quotaSize">
        <template #label>
          <div class="flex items-end">
            <span>{{ t('fileSpace.spaceForm.form.spaceQuota') }} </span>
            <span class="text-gray-text-light text-3">
              {{ t('fileSpace.spaceForm.quotaDescription') }}
            </span>
          </div>
        </template>
        <Input
          v-model:value="form.quotaSize.value"
          :trimAll="true"
          :max="2000"
          :min="0"
          :decimalPoint="4"
          :placeholder="t('fileSpace.spaceForm.placeholders.spaceQuota')"
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
      
      <!-- Remarks field -->
      <FormItem :label="t('fileSpace.spaceForm.form.remark')">
        <Input
          v-model:value="form.remark"
          type="textarea"
          :placeholder="t('fileSpace.spaceForm.placeholders.remark')"
          showCount
          class="h-25 mb-4"
          :maxlength="200" />
      </FormItem>
    </Form>
  </Modal>
</template>
