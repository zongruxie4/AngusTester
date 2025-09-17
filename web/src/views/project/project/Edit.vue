<script lang="ts" setup>
// Vue composition API imports
import { defineAsyncComponent, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

// Ant Design components
import { Button, Form, FormItem, Popover, RadioButton, RadioGroup } from 'ant-design-vue';

// Custom components
import { Cropper, DatePicker, Icon, Image, Input, Select, SelectUser } from '@xcan-angus/vue-ui';
import { GM } from '@xcan-angus/infra';

// Local imports
import { ProjectType } from '@/enums/enums';
import { cropperUploadOption } from '@/utils/constant';
import { getProjectTypeTipConfig, toolbarOptions, uploadParams } from './utils';

// Composables
import { useForm, useMembers, useAvatar } from './composables';
import { ProjectEditEmits, ProjectEditProps } from '@/views/project/project/types';

/** Component props definition */
const props = withDefaults(defineProps<ProjectEditProps>(), {
  projectId: undefined,
  data: undefined
});

/** Component emits definition */
const emit = defineEmits<ProjectEditEmits>();

/** Internationalization composable */
const { t } = useI18n();

/** Project members management composable */
const {
  memberType,
  members,
  defaultOptionsUser,
  defaultOptionsDept,
  defaultOptionsGroup,
  initializeMembers
} = useMembers();

/** Project form management composable */
const {
  loading,
  formRef,
  projectDetail,
  descRef,
  loadProjectDetail,
  validateDescription,
  selectProjectType,
  submitForm,
  cancelForm
} = useForm(props.projectId, members);

/** Project avatar management composable */
const {
  uploadAvatarVisible,
  openCropper,
  uploadImgSuccess,
  deleteImage
} = useAvatar(projectDetail);

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

/** Project type configuration for tips display */
const projectTypeTipConfig = getProjectTypeTipConfig();

/** Project type name mapping for UI display */
const projectTypeName = computed(() => ({
  [ProjectType.AGILE]: t('project.projectEdit.projectTypeName.agile'),
  [ProjectType.GENERAL]: t('project.projectEdit.projectTypeName.general'),
  [ProjectType.TESTING]: t('project.projectEdit.projectTypeName.testing')
}));

/** Get current project type name for display */
const currentProjectTypeName = computed(() => {
  const typeValue = projectDetail.value.type?.value;
  return typeValue ? projectTypeName.value[typeValue as keyof typeof projectTypeName.value] : '';
});

/**
 * Handle form submission
 * Integrates form validation and member data submission
 */
const handleFormSubmit = async (): Promise<void> => {
  try {
    // Submit form using form composable
    await submitForm();
    // Emit success event
    emit('ok');
  } catch (error) {
    console.error('Form submission failed:', error);
  }
};

/**
 * Handle form cancellation
 */
const handleFormCancel = (): void => {
  cancelForm();
  emit('cancel');
};

/**
 * Handle successful image upload from cropper
 * @param response - Upload response from server
 */
const handleImageUpload = (response: any): void => {
  uploadImgSuccess(response);
};

onMounted(async () => {
  // Load project data for editing mode
  if (props.projectId) {
    await loadProjectDetail(props.projectId);

    // Initialize members after loading project detail
    if (projectDetail.value.members) {
      initializeMembers(projectDetail.value.members);
    }
  }
});
</script>
<template>
  <div class="project-edit-container">
    <!-- Add project section (when project ID is empty) -->
    <div v-if="!props.projectId" class="project-create-layout">
      <div class="form-content-wrapper">
        <Form
          ref="formRef"
          labelAlign="right"
          :colon="false"
          class="project-form"
          :model="projectDetail"
          :labelCol="{ style: {width: '80px'} }">
          <div class="form-main-content">
            <!-- Project type selection area -->
            <div class="project-type-section">
              <h3 class="section-title">{{ t('project.projectEdit.tabs.projectType') }}</h3>
              <div class="project-type-cards">
                <div
                  class="project-type-card"
                  :class="{ 'selected': projectDetail.type?.value === ProjectType.AGILE }"
                  @click="selectProjectType(ProjectType.AGILE)">
                  <Icon
                    v-show="projectDetail.type?.value === ProjectType.AGILE"
                    icon="icon-xuanzhongduigou"
                    class="selection-icon" />
                  <Icon icon="icon-minjiexiangmuguanli" class="type-icon" />
                  <div class="type-name">{{ t('project.projectEdit.projectTypeName.agile') }}</div>
                </div>
                <div
                  class="project-type-card"
                  :class="{ 'selected': projectDetail.type?.value === ProjectType.GENERAL }"
                  @click="selectProjectType(ProjectType.GENERAL)">
                  <Icon
                    v-show="projectDetail.type?.value === ProjectType.GENERAL"
                    icon="icon-xuanzhongduigou"
                    class="selection-icon" />
                  <Icon icon="icon-jiandanxiangmuguanli" class="type-icon" />
                  <div class="type-name">{{ t('project.projectEdit.projectTypeName.general') }}</div>
                </div>
                <div
                  class="project-type-card"
                  :class="{ 'selected': projectDetail.type?.value === ProjectType.TESTING }"
                  @click="selectProjectType(ProjectType.TESTING)">
                  <Icon
                    v-show="projectDetail.type?.value === ProjectType.TESTING"
                    icon="icon-xuanzhongduigou"
                    class="selection-icon" />
                  <Icon icon="icon-ceshixiangmuguanli" class="type-icon" />
                  <div class="type-name">{{ t('project.projectEdit.projectTypeName.testing') }}</div>
                </div>
              </div>
            </div>

            <!-- Form fields area -->
            <div class="form-fields-section">
              <div class="form-fields-content">
                <FormItem label=" " class="avatar-upload-item">
                  <div class="avatar-upload-container">
                    <div v-if="projectDetail.avatar" class="avatar-preview">
                      <div class="avatar-wrapper">
                        <Image
                          :src="projectDetail.avatar || ''"
                          class="avatar-image"
                          alt="avatar" />
                        <div class="avatar-overlay">
                          <Icon
                            icon="icon-qingchu"
                            class="delete-icon"
                            @click="deleteImage" />
                        </div>
                      </div>
                    </div>
                    <div v-else class="avatar-upload">
                      <div
                        class="upload-area"
                        @click="openCropper">
                        <img src="../../../assets/images/default.png" class="upload-icon" />
                        <div class="upload-text">{{ t('project.projectEdit.actions.clickToReplaceIcon') }}</div>
                      </div>
                    </div>
                  </div>
                </FormItem>
                <FormItem
                  :label="t('project.projectEdit.form.projectName')"
                  name="name"
                  class="form-field"
                  required>
                  <Input
                    v-model:value="projectDetail.name"
                    :placeholder="t('project.projectEdit.form.projectNamePlaceholder')"
                    :maxlength="100"
                    class="enhanced-input">
                  </Input>
                </FormItem>

                <FormItem
                  :label="t('project.projectEdit.form.timePlan')"
                  name="dateRange"
                  class="form-field with-tooltip"
                  required>
                  <DatePicker
                    v-model:value="projectDetail.dateRange"
                    class="enhanced-date-picker"
                    :allowClear="false"
                    type="date-range">
                  </DatePicker>
                  <Popover placement="right" overlayClassName="form-tooltip">
                    <template #content>
                      <div class="tooltip-content">
                        {{ t('project.projectEdit.form.timePlanDescription') }}
                      </div>
                    </template>
                    <Icon icon="icon-tishi1" class="tooltip-icon" />
                  </Popover>
                </FormItem>

                <div class="form-row">
                  <FormItem
                    :label="t('project.projectEdit.form.owner')"
                    name="ownerId"
                    required
                    class="form-field with-tooltip flex-1">
                    <SelectUser
                      v-model:value="projectDetail.ownerId"
                      size="small"
                      :placeholder="t('project.projectEdit.form.ownerPlaceholder')"
                      :allowClear="false"
                      class="enhanced-select" />
                    <Popover placement="right" overlayClassName="form-tooltip">
                      <template #content>
                        <div class="tooltip-content">
                          {{ t('project.projectEdit.form.ownerDescription') }}
                        </div>
                      </template>
                      <Icon icon="icon-tishi1" class="tooltip-icon" />
                    </Popover>
                  </FormItem>

                  <FormItem
                    :label="t('project.projectEdit.form.importExample')"
                    class="form-field with-tooltip flex-1">
                    <RadioGroup
                      v-model:value="projectDetail.importExample"
                      :options="[{ value: true, label: t('status.yes')}, { value: false, label: t('status.no') }]"
                      class="enhanced-radio-group">
                    </RadioGroup>
                    <Popover placement="right" overlayClassName="form-tooltip">
                      <template #content>
                        <div class="tooltip-content">
                          {{ t('project.projectEdit.form.importExampleDescription') }}
                        </div>
                      </template>
                      <Icon icon="icon-tishi1" class="tooltip-icon" />
                    </Popover>
                  </FormItem>
                </div>

                <FormItem
                  :label="t('project.projectEdit.form.projectMembers')"
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
                          {{ t('project.projectEdit.form.user') }}
                        </RadioButton>
                        <RadioButton value="dept">
                          {{ t('project.projectEdit.form.department') }}
                        </RadioButton>
                        <RadioButton value="group">
                          {{ t('project.projectEdit.form.group') }}
                        </RadioButton>
                      </RadioGroup>
                      <Popover placement="right" overlayClassName="form-tooltip">
                        <template #content>
                          <div class="tooltip-content">
                            {{ t('project.projectEdit.form.membersDescription') }}
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
                        :placeholder="t('project.projectEdit.form.selectUser')"
                        :action="`${GM}/user?fullTextSearch=true`"
                        :defaultOptions="defaultOptionsUser"
                        mode="multiple"
                        :fieldNames="{ label: 'fullName', value: 'id' }"
                        class="enhanced-select">
                      </Select>

                      <Select
                        v-show="memberType === 'dept'"
                        v-model:value="members.DEPT"
                        :placeholder="t('project.projectEdit.form.selectDepartment')"
                        :showSearch="true"
                        :action="`${GM}/dept?fullTextSearch=true`"
                        :defaultOptions="defaultOptionsDept"
                        mode="multiple"
                        :fieldNames="{ label: 'name', value: 'id' }"
                        class="enhanced-select">
                      </Select>

                      <Select
                        v-show="memberType === 'group'"
                        v-model:value="members.GROUP"
                        :placeholder="t('project.projectEdit.form.selectGroup')"
                        :showSearch="true"
                        :action="`${GM}/group?fullTextSearch=true`"
                        :defaultOptions="defaultOptionsGroup"
                        mode="multiple"
                        :fieldNames="{ label: 'name', value: 'id' }"
                        class="enhanced-select">
                      </Select>
                    </div>
                  </div>
                </FormItem>

                <FormItem
                  :label="t('project.projectEdit.form.description')"
                  name="description"
                  :rules="[{validator: validateDescription}]"
                  class="form-field description-field">
                  <RichEditor
                    ref="descRef"
                    v-model:value="projectDetail.description"
                    :toolbarOptions="toolbarOptions"
                    :options="{placeholder: t('project.projectEdit.form.descriptionPlaceholder')}"
                    class="enhanced-editor" />
                </FormItem>
              </div>
            </div>

            <!-- Project type preview area -->
            <div class="project-preview-section">
              <h3 class="section-title">
                {{ currentProjectTypeName }}
              </h3>
              <div class="preview-content">
                <div class="preview-image">
                  <img
                    v-show="projectDetail.type?.value === ProjectType.AGILE"
                    src="./images/agile.png"
                    class="preview-img agile" />
                  <img
                    v-show="projectDetail.type?.value === ProjectType.GENERAL"
                    src="./images/general.png"
                    class="preview-img general" />
                  <img
                    v-show="projectDetail.type?.value === ProjectType.TESTING"
                    src="./images/testing.png"
                    class="preview-img testing" />
                </div>
                <div class="preview-features">
                  <div
                    v-for="(item, index) in projectTypeTipConfig[projectDetail.type?.value || ProjectType.AGILE]"
                    :key="index"
                    class="feature-item">
                    <Icon icon="icon-duihao-copy" class="feature-icon" />
                    <p class="feature-text">{{ item }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <FormItem label="" class="form-actions">
            <div class="action-buttons">
              <Button
                type="primary"
                size="small"
                htmlType="submit"
                :loading="loading"
                class="primary-button"
                @click="handleFormSubmit">
                <Icon icon="icon-dangqianxuanzhong" class="button-icon" />
                {{ t('actions.save') }}
              </Button>
              <Button
                size="small"
                class="secondary-button"
                @click="handleFormCancel">
                <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-zhongzhi2" />
                {{ t('actions.cancel') }}
              </Button>
            </div>
          </FormItem>
        </Form>
      </div>
    </div>

    <!-- Edit project section (when project ID is not empty) -->
    <div v-else class="form-content-wrapper">
      <Form
        ref="formRef"
        labelAlign="right"
        :colon="false"
        class="project-form"
        :model="projectDetail"
        :labelCol="{ style: {width: '80px'} }">
        <div class="form-main-content edit-layout">
          <!-- Form fields area -->
          <div class="form-fields-section">
            <div class="form-fields-content">
              <FormItem label=" " class="avatar-upload-item">
                <div class="avatar-upload-container">
                  <div v-if="projectDetail.avatar" class="avatar-preview">
                    <div class="avatar-wrapper">
                      <Image
                        :src="projectDetail.avatar || ''"
                        class="avatar-image"
                        alt="avatar" />
                      <div class="avatar-overlay">
                        <Icon
                          icon="icon-qingchu"
                          class="delete-icon"
                          @click="deleteImage" />
                      </div>
                    </div>
                  </div>
                  <div v-else class="avatar-upload">
                    <div
                      class="upload-area"
                      @click="openCropper">
                      <img src="../../../assets/images/default.png" class="upload-icon" />
                      <div class="upload-text">{{ t('project.projectEdit.actions.clickToReplaceIcon') }}</div>
                    </div>
                  </div>
                </div>
              </FormItem>
              <FormItem
                :label="t('project.projectEdit.form.projectName')"
                name="name"
                class="form-field"
                required>
                <Input
                  v-model:value="projectDetail.name"
                  :placeholder="t('project.projectEdit.form.projectNamePlaceholder')"
                  :maxlength="100"
                  class="enhanced-input">
                </Input>
              </FormItem>

              <div class="form-row">
                <FormItem
                  :label="t('project.projectEdit.form.timePlan')"
                  name="dateRange"
                  class="form-field with-tooltip flex-1"
                  required>
                  <DatePicker
                    v-model:value="projectDetail.dateRange"
                    class="enhanced-date-picker"
                    :allowClear="false"
                    type="date-range">
                  </DatePicker>
                  <Popover placement="right" overlayClassName="form-tooltip">
                    <template #content>
                      <div class="tooltip-content">
                        {{ t('project.projectEdit.form.timePlanDescription') }}
                      </div>
                    </template>
                    <Icon icon="icon-tishi1" class="tooltip-icon" />
                  </Popover>
                </FormItem>

                <FormItem
                  :label="t('project.projectEdit.form.owner')"
                  name="ownerId"
                  required
                  class="form-field with-tooltip flex-1">
                  <SelectUser
                    v-model:value="projectDetail.ownerId"
                    size="small"
                    :placeholder="t('project.projectEdit.form.ownerPlaceholder')"
                    :allowClear="false"
                    class="enhanced-select" />
                  <Popover placement="right" overlayClassName="form-tooltip">
                    <template #content>
                      <div class="tooltip-content">
                        {{ t('project.projectEdit.form.ownerDescription') }}
                      </div>
                    </template>
                    <Icon icon="icon-tishi1" class="tooltip-icon" />
                  </Popover>
                </FormItem>
              </div>

              <FormItem
                :label="t('project.projectEdit.form.projectMembers')"
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
                        {{ t('project.projectEdit.form.user') }}
                      </RadioButton>
                      <RadioButton value="dept">
                        {{ t('project.projectEdit.form.department') }}
                      </RadioButton>
                      <RadioButton value="group">
                        {{ t('project.projectEdit.form.group') }}
                      </RadioButton>
                    </RadioGroup>
                    <Popover placement="right" overlayClassName="form-tooltip">
                      <template #content>
                        <div class="tooltip-content">
                          {{ t('project.projectEdit.form.membersDescription') }}
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
                      :placeholder="t('project.projectEdit.form.selectUser')"
                      :action="`${GM}/user?fullTextSearch=true`"
                      :defaultOptions="defaultOptionsUser"
                      mode="multiple"
                      :fieldNames="{ label: 'fullName', value: 'id' }"
                      class="enhanced-select">
                    </Select>

                    <Select
                      v-show="memberType === 'dept'"
                      v-model:value="members.DEPT"
                      :placeholder="t('project.projectEdit.form.selectDepartment')"
                      :showSearch="true"
                      :action="`${GM}/dept?fullTextSearch=true`"
                      :defaultOptions="defaultOptionsDept"
                      mode="multiple"
                      :fieldNames="{ label: 'name', value: 'id' }"
                      class="enhanced-select">
                    </Select>

                    <Select
                      v-show="memberType === 'group'"
                      v-model:value="members.GROUP"
                      :placeholder="t('project.projectEdit.form.selectGroup')"
                      :showSearch="true"
                      :action="`${GM}/group?fullTextSearch=true`"
                      :defaultOptions="defaultOptionsGroup"
                      mode="multiple"
                      :fieldNames="{ label: 'name', value: 'id' }"
                      class="enhanced-select">
                    </Select>
                  </div>
                </div>
              </FormItem>

              <FormItem
                :label="t('project.projectEdit.form.description')"
                name="description"
                :rules="[{validator: validateDescription}]"
                class="form-field description-field">
                <RichEditor
                  ref="descRef"
                  v-model:value="projectDetail.description"
                  :toolbarOptions="toolbarOptions"
                  :options="{placeholder: t('project.projectEdit.form.descriptionPlaceholder')}"
                  class="enhanced-editor" />
              </FormItem>
            </div>
          </div>

          <!-- Project type preview area -->
          <div class="project-preview-section">
            <h3 class="section-title">
              {{ projectDetail.type?.value ? projectTypeName[projectDetail.type.value as keyof typeof projectTypeName] : '' }}
            </h3>
            <div class="preview-content">
              <div class="preview-image">
                <img
                  v-show="projectDetail.type?.value === ProjectType.AGILE"
                  src="./images/agile.png"
                  class="preview-img agile" />
                <img
                  v-show="projectDetail.type?.value === ProjectType.GENERAL"
                  src="./images/general.png"
                  class="preview-img general" />
                <img
                  v-show="projectDetail.type?.value === ProjectType.TESTING"
                  src="./images/testing.png"
                  class="preview-img testing" />
              </div>
              <div class="preview-features">
                <div
                  v-for="(item, index) in projectTypeTipConfig[projectDetail.type?.value || ProjectType.AGILE]"
                  :key="index"
                  class="feature-item">
                  <Icon icon="icon-duihao-copy" class="feature-icon" />
                  <p class="feature-text">{{ item }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <FormItem label="" class="form-actions">
          <div class="action-buttons">
            <Button
              type="primary"
              size="small"
              htmlType="submit"
              :loading="loading"
              class="primary-button"
              @click="handleFormSubmit">
              <Icon icon="icon-dangqianxuanzhong" class="button-icon" />
              {{ t('actions.save') }}
            </Button>
            <Button
              size="small"
              class="secondary-button"
              @click="handleFormCancel">
              <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-zhongzhi2" />
              {{ t('actions.cancel') }}
            </Button>
          </div>
        </FormItem>
      </Form>
    </div>

    <Cropper
      v-model:visible="uploadAvatarVisible"
      :title="t('project.projectEdit.actions.uploadIcon')"
      :params="uploadParams"
      :options="cropperUploadOption"
      @success="handleImageUpload" />
  </div>
</template>
<style scoped>
/* Main container styles */
.project-edit-container {
  padding: 24px;
  height: 100%;
  overflow: auto;
  background: transparent;
}

/* Project creation layout */
.project-create-layout {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  min-height: calc(100vh - 48px);
  padding-left: 0;
}

.form-content-wrapper {
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  padding: 0;
  width: 100%;
  max-width: none;
}

.project-form {
  width: 100%;
}

.form-main-content {
  display: grid;
  grid-template-columns: 200px 2.67fr 1fr;
  gap: 40px;
  min-height: 600px;
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
  border-bottom: 1.5px solid #e5e7eb;
}

.project-type-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
}

.project-type-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
  min-height: 120px;
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
  font-size: 20px;
  color: #3b82f6;
}

.type-icon {
  font-size: 40px;
  color: #6b7280;
  margin-bottom: 12px;
}

.project-type-card.selected .type-icon {
  color: #3b82f6;
}

.type-name {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  text-align: center;
}

.project-type-card.selected .type-name {
  color: #1f2937;
  font-weight: 600;
}

/* Form fields area */
.form-fields-section {
  display: flex;
  flex-direction: column;
  margin-right: 20px;
}

.form-fields-content {
  flex: 1;
}

.form-field {
  margin-bottom: 24px;
  font-weight: bold
}

.form-field.with-tooltip {
  position: relative;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

/* Avatar upload styles */
.avatar-upload-item {
  margin-bottom: 32px;
}

.avatar-upload-container {
  display: flex;
  justify-content: flex-start;
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
  width: 40px;
  height: 40px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 12px;
  color: #6b7280;
  text-align: center;
  line-height: 1.3;
}

/* Enhanced form controls */
.enhanced-input {
  border-radius: 4px;
  border: 1px solid #e0e3e8;
  transition: all 0.3s ease;
}

.enhanced-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.enhanced-date-picker {
  width: 100%;
  border-radius: 4px;
}

.enhanced-select {
  border-radius: 4px;
}

.enhanced-radio-group {
  border-radius: 4px;
}

.enhanced-editor {
  border-radius: 4px;
  overflow: hidden;
}

/* Member selector styles */
.members-field {
  margin-bottom: 32px;
}

.members-selector {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.member-type-tabs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-select-container {
  min-height: 32px;
}

/* Description field styles */
.description-field {
  margin-bottom: 32px;
}

/* Tooltip styles */
.tooltip-icon {
  position: absolute;
  right: -24px;
  top: 8px;
  font-size: 14px;
  color: #9ca3af;
  cursor: help;
}

:global(.form-tooltip .ant-popover-inner-content) {
  padding: 12px 16px;
}

.tooltip-content {
  max-width: 280px;
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
  gap: 24px;
  flex: 1;
}

.preview-image {
  padding: 24px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.preview-img {
  max-width: 100%;
  height: auto;
  transition: all 0.3s ease;
}

.preview-img.agile {
  width: 74%;
}

.preview-img.general {
  width: 92%;
}

.preview-img.testing {
  width: 70%;
}

.preview-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.feature-icon {
  font-size: 14px;
  color: #059669;
  margin-top: 2px;
  flex-shrink: 0;
}

.feature-text {
  font-size: 14px;
  line-height: 1.5;
  color: #374151;
  margin: 0;
}

/* Action button styles */
.form-actions {
  margin-bottom: 0;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.primary-button {
  padding: 8px 24px;
  height: auto;
  border-radius: 4px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.secondary-button {
  padding: 8px 24px;
  height: auto;
  border-radius: 4px;
  font-weight: 500;
}

.button-icon {
  font-size: 14px;
}

/* Edit layout styles */
.edit-layout {
  grid-template-columns: 2.67fr 1fr;
  gap: 40px;
}

/* Responsive design */
@media (max-width: 1200px) {
  .form-main-content {
    grid-template-columns: 150px 1fr 200px;
    gap: 24px;
  }

  .edit-layout {
    grid-template-columns: 1fr 200px;
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .form-main-content {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .edit-layout {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>
