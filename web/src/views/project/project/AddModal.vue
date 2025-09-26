<script lang="ts" setup>
// Vue composition API imports
import { computed, defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// Custom UI components
import { DatePicker, Icon, Image, Input, Modal, Select, SelectUser } from '@xcan-angus/vue-ui';

// Ant Design components
import { Form, FormItem, Popover, RadioButton, RadioGroup, Upload } from 'ant-design-vue';

// API and utilities
import { GM, upload } from '@xcan-angus/infra';

// Composables
import { useActions, useManagement } from './composables';
import { getProjectTypeName, getProjectTypeTipConfig, Project } from './types';
import { ProjectType } from '@/enums/enums';

// Initialize i18n
const { t } = useI18n();

interface Props {
  visible: boolean;
  dataSource?: Project;
  closable: boolean;
}

// Props and emits
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  dataSource: undefined,
  closable: true
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'cancel', value: boolean): void;
  (e: 'ok'): void;
}>();

// Async component definitions
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// Initialize composables
const {
  memberType,
  projectType,
  members,
  defaultOptionsUser,
  defaultOptionsDept,
  defaultOptionsGroup,
  formData,
  selectProjectType,
  initializeForm,
  loadProjectIntoForm,
  buildCreateParams,
  buildUpdateParams,
  validateForm
} = useManagement();

const {
  operationLoading,
  createProject,
  updateProject
} = useActions({
  onDataChange: () => emit('ok')
});

// Local refs
const formRef = ref();
const descRichRef = ref();

/** Project type configuration for tips display */
const projectTypeTipConfig = getProjectTypeTipConfig();
/** Project type name mapping for UI display */
const projectTypeName = computed(() => {
  return getProjectTypeName();
});

// Computed properties
const modalTitle = computed(() => {
  return props.dataSource?.id
    ? t('project.edit.modal.editProject')
    : t('project.edit.modal.addProject');
});

// Event handlers
const uploadImg = async ({ file }) => {
  const [error, { data = [] }] = await upload(file, {
    bizKey: 'angusTesterProjectAvatar'
  });
  if (error) {
    return;
  }
  if (data.length > 0) {
    formData.avatar = data[0].url;
  }
};

const delImg = () => {
  formData.avatar = '';
};

const cancel = () => {
  emit('update:visible', false);
  emit('cancel', false);
};

// Form validation
const validateDesc = () => {
  if (descRichRef.value && descRichRef.value.getLength() > 2000) {
    return Promise.reject(t('project.edit.rules.maxCharacters'));
  }
  return Promise.resolve();
};

// Form submission
const ok = async () => {
  try {
    await formRef.value.validate();
    const isValid = await validateForm();

    if (!isValid) {
      return;
    }

    let success = false;

    if (formData.id) {
      // Update existing project
      const params = buildUpdateParams();
      success = await updateProject(params);
    } else {
      // Create new project
      const params = buildCreateParams();
      success = await createProject(params);
    }

    if (success) {
      emit('update:visible', false);
    }
  } catch (error) {
    console.error('Form validation failed:', error);
  }
};

// Watchers
watch(() => props.visible, (newValue) => {
  if (!newValue) {
    return;
  }

  if (props.dataSource) {
    loadProjectIntoForm(props.dataSource);
  } else {
    initializeForm();
  }
}, {
  immediate: true
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :title="modalTitle"
    :closable="props.closable"
    :cancelButtonProps="{
      class: {'hidden': !props.closable}
    }"
    :okButtonProps="{
      loading: operationLoading
    }"
    :width="1200"
    @ok="ok"
    @cancel="cancel">
    <div class="modal-content">
      <!-- Project type selection area -->
      <div class="project-type-section">
        <h3 class="section-title">{{ t('project.edit.form.selectProjectType') }}</h3>
        <div class="project-type-cards">
          <div
            class="project-type-card"
            :class="{ 'selected': projectType === ProjectType.AGILE }"
            @click="selectProjectType(ProjectType.AGILE)">
            <Icon
              v-show="projectType === ProjectType.AGILE"
              icon="icon-xuanzhongduigou"
              class="selection-icon" />
            <Icon icon="icon-minjiexiangmuguanli" class="type-icon" />
            <div class="type-name">{{ projectTypeName.get(ProjectType.AGILE) }}</div>
          </div>
          <div
            class="project-type-card"
            :class="{ 'selected': projectType === ProjectType.GENERAL }"
            @click="selectProjectType(ProjectType.GENERAL)">
            <Icon
              v-show="projectType === ProjectType.GENERAL"
              icon="icon-xuanzhongduigou"
              class="selection-icon" />
            <Icon icon="icon-jiandanxiangmuguanli" class="type-icon" />
            <div class="type-name">{{ projectTypeName.get(ProjectType.GENERAL) }}</div>
          </div>
          <div
            class="project-type-card"
            :class="{ 'selected': projectType === ProjectType.TESTING }"
            @click="selectProjectType(ProjectType.TESTING)">
            <Icon
              v-show="projectType === ProjectType.TESTING"
              icon="icon-xuanzhongduigou"
              class="selection-icon" />
            <Icon icon="icon-ceshixiangmuguanli" class="type-icon" />
            <div class="type-name">{{ projectTypeName.get(ProjectType.TESTING) }}</div>
          </div>
        </div>
      </div>

      <!-- Form fields area -->
      <div class="form-fields-section">
        <h3 class="section-title">{{ t('common.basicInfo') }}</h3>
        <Form
          ref="formRef"
          class="project-form"
          :model="formData"
          layout="vertical">
          <!-- Avatar upload -->
          <FormItem class="avatar-upload-item">
            <div class="avatar-upload-container">
              <div v-if="formData.avatar" class="avatar-preview">
                <div class="avatar-wrapper">
                  <Image
                    :src="formData.avatar"
                    class="avatar-image"
                    alt="avatar" />
                  <div class="avatar-overlay">
                    <Icon
                      icon="icon-qingchu"
                      class="delete-icon"
                      @click="delImg" />
                  </div>
                </div>
              </div>
              <div v-else class="avatar-upload">
                <Upload
                  name="avatar"
                  accept=".png,.jpeg,.jpg"
                  listType="picture-card"
                  :showUploadList="false"
                  :customRequest="uploadImg"
                  class="upload-component">
                  <div class="upload-area">
                    <img src="../../../assets/images/default.png" class="upload-icon" />
                    <div class="upload-text">{{ t('project.edit.actions.clickToReplaceIcon') }}</div>
                  </div>
                </Upload>
              </div>
            </div>
          </FormItem>

          <!-- Project name -->
          <FormItem
            :label="t('project.edit.form.name')"
            name="name"
            class="form-field"
            required>
            <Input
              v-model:value="formData.name"
              :placeholder="t('project.edit.form.projectNamePlaceholder')"
              :maxlength="100"
              class="enhanced-input" />
          </FormItem>

          <!-- Time plan -->
          <FormItem
            name="dateRange"
            class="form-field with-tooltip"
            :rules="[{ required: true, message: t('project.edit.rules.timeRequired')}]">
            <template #label>
              <span>{{ t('project.edit.form.time') }}</span>
            </template>
            <DatePicker
              v-model:value="formData.dateRange"
              :allowClear="false"
              class="enhanced-date-picker"
              type="date-range" />
            <Popover placement="right" overlayClassName="form-tooltip">
              <template #content>
                <div class="tooltip-content">
                  {{ t('project.edit.form.planTimeDescription') }}
                </div>
              </template>
              <Icon icon="icon-tishi1" class="tooltip-icon" />
            </Popover>
          </FormItem>

          <div class="form-row">
            <!-- Project owner -->
            <FormItem
              name="ownerId"
              class="form-field with-tooltip flex-1"
              :rules="[{ required: true, message: t('project.edit.rules.ownerRequired')}]">
              <template #label>
                <span>{{ t('common.owner') }}</span>
              </template>
              <SelectUser
                v-model:value="formData.ownerId"
                size="small"
                :placeholder="t('common.placeholders.selectOwner')"
                :allowClear="false"
                class="enhanced-select" />
              <Popover placement="right" overlayClassName="form-tooltip">
                <template #content>
                  <div class="tooltip-content">
                    {{ t('project.edit.form.ownerDescription') }}
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="tooltip-icon" />
              </Popover>
            </FormItem>

            <!-- Import example -->
            <FormItem class="form-field with-tooltip flex-1">
              <template #label>
                <span>{{ t('actions.importExamples') }}</span>
              </template>
              <RadioGroup
                v-model:value="formData.importExample"
                :options="[{ value: true, label: t('status.yes')}, { value: false, label: t('status.no') }]"
                class="enhanced-radio-group" />
              <Popover placement="right" overlayClassName="form-tooltip">
                <template #content>
                  <div class="tooltip-content">
                    {{ t('project.edit.form.importExampleDescription') }}
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="tooltip-icon" />
              </Popover>
            </FormItem>
          </div>

          <!-- Project members -->
          <FormItem
            :label="t('common.members')"
            class="form-field members-field"
            required>
            <div class="members-selector">
              <div class="member-type-tabs">
                <RadioGroup
                  v-model:value="memberType"
                  buttonStyle="solid"
                  size="small"
                  class="enhanced-radio-group">
                  <RadioButton value="user">
                    {{ t('organization.user') }}
                  </RadioButton>
                  <RadioButton value="dept">
                    {{ t('organization.dept') }}
                  </RadioButton>
                  <RadioButton value="group">
                    {{ t('organization.group') }}
                  </RadioButton>
                </RadioGroup>
                <Popover placement="right" overlayClassName="form-tooltip">
                  <template #content>
                    <div class="tooltip-content">
                      {{ t('project.edit.form.membersDescription') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="tooltip-icon" />
                </Popover>
              </div>
              <div class="member-select-container">
                <Select
                  v-show="memberType === 'user'"
                  v-model:value="members.USER"
                  :showSearch="true"
                  :placeholder="t('organization.placeholders.selectUser')"
                  :action="`${GM}/user?fullTextSearch=true`"
                  :defaultOptions="defaultOptionsUser"
                  mode="multiple"
                  :fieldNames="{ label: 'fullName', value: 'id' }"
                  class="enhanced-select" />

                <Select
                  v-show="memberType === 'dept'"
                  v-model:value="members.DEPT"
                  :placeholder="t('organization.placeholders.selectDepartment')"
                  :showSearch="true"
                  :action="`${GM}/dept?fullTextSearch=true`"
                  :defaultOptions="defaultOptionsDept"
                  mode="multiple"
                  :fieldNames="{ label: 'name', value: 'id' }"
                  class="enhanced-select" />

                <Select
                  v-show="memberType === 'group'"
                  v-model:value="members.GROUP"
                  :placeholder="t('organization.placeholders.selectGroup')"
                  :showSearch="true"
                  :action="`${GM}/group?fullTextSearch=true`"
                  :defaultOptions="defaultOptionsGroup"
                  mode="multiple"
                  :fieldNames="{ label: 'name', value: 'id' }"
                  class="enhanced-select" />
              </div>
            </div>
          </FormItem>

          <!-- Project description -->
          <FormItem
            :label="t('common.description')"
            name="description"
            class="form-field description-field"
            :rules="[{validator: validateDesc}]">
            <RichEditor
              ref="descRichRef"
              v-model:value="formData.description"
              class="enhanced-editor"
              :height="80"
              :options="{placeholder: t('project.edit.form.descriptionPlaceholder'), theme: 'bubble'}" />
          </FormItem>
        </Form>
      </div>

      <!-- Project type preview area -->
      <div class="project-preview-section">
        <h3 class="section-title">{{ projectTypeName.get(projectType) }}</h3>
        <div class="preview-content">
          <div class="preview-image">
            <img
              v-show="projectType===ProjectType.AGILE"
              src="./images/agile.png"
              class="preview-img agile" />
            <img
              v-show="projectType===ProjectType.GENERAL"
              src="./images/general.png"
              class="preview-img general" />
            <img
              v-show="projectType===ProjectType.TESTING"
              src="./images/testing.png"
              class="preview-img testing" />
          </div>
          <div class="preview-features">
            <div
              v-for="(item, index) in projectTypeTipConfig[projectType]"
              :key="index"
              class="feature-item">
              <Icon icon="icon-duihao-copy" class="feature-icon" />
              <p class="feature-text">{{ item }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Modal>
</template>
<style scoped>
/* Modal content container */
.modal-content {
  display: grid;
  grid-template-columns: 200px 2.67fr 1fr;
  gap: 32px;
  min-height: 500px;
}

/* Project type selection area */
.project-type-section {
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 20px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.project-type-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.project-type-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 12px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
  min-height: 100px;
}

.project-type-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  transform: translateY(-2px);
}

.project-type-card.selected {
  border-color: #3b82f6;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.selection-icon {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 16px;
  color: #3b82f6;
}

.type-icon {
  font-size: 32px;
  color: #6b7280;
  margin-bottom: 8px;
}

.project-type-card.selected .type-icon {
  color: #3b82f6;
}

.type-name {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  text-align: center;
  line-height: 1.2;
}

.project-type-card.selected .type-name {
  color: #1f2937;
  font-weight: 600;
}

/* Form fields area */
.form-fields-section {
  display: flex;
  flex-direction: column;
}

.project-form {
  flex: 1;
}

.form-field {
  margin-bottom: 20px;
}

.form-field.with-tooltip {
  position: relative;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* Avatar upload styles */
.avatar-upload-item {
  margin-bottom: 24px;
}

.avatar-upload-container {
  display: flex;
  justify-content: center;
}

.avatar-preview {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #e5e7eb;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.delete-icon {
  color: white;
  font-size: 24px;
  cursor: pointer;
}

.avatar-upload {
  display: flex;
  align-items: center;
}

.upload-component {
  width: 100px;
  height: 100px;
}

:global(.upload-component .ant-upload) {
  width: 100px !important;
  height: 100px !important;
}

.upload-area {
  width: 100px;
  height: 100px;
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f9fafb;
}

.upload-area:hover {
  border-color: #3b82f6;
  background: #eff6ff;
}

.upload-icon {
  width: 32px;
  height: 32px;
  margin-bottom: 6px;
}

.upload-text {
  font-size: 11px;
  color: #6b7280;
  text-align: center;
  line-height: 1.2;
}

/* Enhanced form controls */
.enhanced-input {
  border-radius: 8px;
  border: 1px solid #d1d5db;
  transition: all 0.3s ease;
}

.enhanced-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.enhanced-date-picker {
  width: 100%;
  border-radius: 8px;
}

.enhanced-select {
  border-radius: 8px;
}

.enhanced-radio-group {
  border-radius: 8px;
}

.enhanced-editor {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #d1d5db;
}

/* Member selector styles */
.members-field {
  margin-bottom: 24px;
}

.members-selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-type-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-select-container {
  min-height: 32px;
}

/* Description field styles */
.description-field {
  margin-bottom: 24px;
}

/* Tooltip styles */
.tooltip-icon {
  position: absolute;
  right: -20px;
  top: 8px;
  font-size: 14px;
  color: #9ca3af;
  cursor: help;
}

:global(.form-tooltip .ant-popover-inner-content) {
  padding: 12px 16px;
}

.tooltip-content {
  max-width: 240px;
  font-size: 13px;
  line-height: 1.5;
  color: #374151;
}

/* Project preview area */
.project-preview-section {
  display: flex;
  flex-direction: column;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex: 1;
}

.preview-image {
  padding: 20px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 160px;
}

.preview-img {
  max-width: 100%;
  height: auto;
  transition: all 0.3s ease;
}

.preview-img.agile {
  width: 70%;
}

.preview-img.general {
  width: 85%;
}

.preview-img.testing {
  width: 72%;
}

.preview-features {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 6px;
}

.feature-icon {
  font-size: 12px;
  color: #059669;
  margin-top: 2px;
  flex-shrink: 0;
}

.feature-text {
  font-size: 12px;
  line-height: 1.4;
  color: #374151;
  margin: 0;
}

/* Responsive design */
@media (max-width: 1100px) {
  .modal-content {
    grid-template-columns: 180px 1fr 180px;
    gap: 24px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

@media (max-width: 900px) {
  .modal-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}
</style>
