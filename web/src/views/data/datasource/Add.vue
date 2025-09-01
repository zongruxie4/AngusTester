<script lang="ts" setup>
import { watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { IconRequired, Input, Modal } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';
import SelectEnum from '@/components/selectEnum/index.vue';
import { AddModalProps, AddModalEmits } from './types';
import { useAddModal } from './composables/useAddModal';

const { t } = useI18n();

/**
 * <p>Component props definition</p>
 * <p>Defines the interface for component properties</p>
 */
const props = withDefaults(defineProps<AddModalProps>(), {
  visible: false,
  editData: undefined,
  projectId: ''
});

/**
 * <p>Component emits definition</p>
 * <p>Defines all possible events that can be emitted by this component</p>
 */
const emit = defineEmits<AddModalEmits>();

/**
 * <p>Main composable for modal functionality</p>
 * <p>Handles form state, validation, and database configuration</p>
 */
const {
  loading,
  formRef,
  formState,
  shouldShowCredentials,
  initializeFormWithEditData,
  resetFormState,
  handleDatabaseChange,
  handleFormSubmit
} = useAddModal(props.projectId);

/**
 * <p>Handle modal close action</p>
 * <p>Emits close event to parent component</p>
 */
const handleClose = (): void => {
  emit('update:visible', false);
};

/**
 * <p>Handle form submission</p>
 * <p>Validates form and submits data, then closes modal and refreshes parent</p>
 */
const handleOk = async (): Promise<void> => {
  const success = await handleFormSubmit();
  if (success) {
    emit('update:visible', false);
    emit('refresh');
  }
};

// Watch for visible changes and initialize form
watch(() => props.visible, (newValue) => {
  if (!newValue) {
    return;
  }
  
  if (props.editData) {
    initializeFormWithEditData(props.editData);
  } else {
    resetFormState();
  }
}, {
  immediate: true
});
</script>

<template>
  <Modal
    :title="props.editData ? t('datasource.form.title.edit') : t('datasource.form.title.add')"
    :confirmLoading="loading"
    :visible="visible"
    @cancel="handleClose"
    @ok="handleOk">
    
    <div class="text-3 text-content flex">
      <!-- Form Labels -->
      <div class="space-y-5 pt-1.5 mr-5">
        <div class="h-7">
          <IconRequired />{{ t('datasource.form.labels.type') }}
        </div>
        <div class="h-7">
          <IconRequired />{{ t('datasource.form.labels.name') }}
        </div>
        <div class="h-7 pl-1.75">
          {{ t('datasource.form.labels.driverClassName') }}
        </div>
        <div class="h-7">
          <IconRequired />{{ t('datasource.form.labels.jdbcUrl') }}
        </div>
        <template v-if="shouldShowCredentials">
          <div class="h-7 pl-1.75">
            {{ t('datasource.form.labels.username') }}
          </div>
          <div class="h-7 pl-1.75">
            {{ t('datasource.form.labels.password') }}
          </div>
        </template>
      </div>
      
      <!-- Form Fields -->
      <Form
        ref="formRef"
        class="flex-1"
        :model="formState">
        
        <!-- Database Type Selection -->
        <FormItem name="database" :rules="{required:true,message:t('datasource.form.rules.databaseType')}">
          <SelectEnum
            v-model:value="formState.database"
            :disabled="!!props.editData"
            enumKey="DatabaseType"
            :placeholder="t('datasource.form.placeholders.databaseType')"
            @change="handleDatabaseChange" />
        </FormItem>
        
        <!-- Data Source Name -->
        <FormItem name="name" :rules="{required:true,message:t('datasource.form.rules.name')}">
          <Input
            v-model:value="formState.name"
            :maxlength="100"
            :placeholder="t('datasource.form.placeholders.name')" />
        </FormItem>
        
        <!-- Driver Class Name -->
        <FormItem name="driverClassName">
          <Input 
            v-model:value="formState.driverClassName" 
            :placeholder="t('datasource.form.placeholders.driverClassName')" />
        </FormItem>
        
        <!-- JDBC URL -->
        <FormItem name="jdbcUrl" :rules="{required:true,message:t('datasource.form.rules.jdbcUrl')}">
          <Input 
            v-model:value="formState.jdbcUrl" 
            :placeholder="t('datasource.form.placeholders.jdbcUrl')" />
        </FormItem>
        
        <!-- Username and Password (conditional) -->
        <template v-if="shouldShowCredentials">
          <FormItem name="username">
            <Input
              v-model:value="formState.username"
              :placeholder="t('datasource.form.placeholders.username')"
              :disabled="formState.database === 'SQLITE' && !!props.editData"
              :maxlength="50" />
          </FormItem>
          <FormItem name="password">
            <Input
              v-model:value="formState.password"
              :placeholder="t('datasource.form.placeholders.password')"
              :maxlength="50"
              type="password" />
          </FormItem>
        </template>
      </Form>
    </div>
  </Modal>
</template>
