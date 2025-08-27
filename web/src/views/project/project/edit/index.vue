<script lang="ts" setup>
import { defineAsyncComponent, inject, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  Button,
  Form,
  FormItem,
  Popover,
  RadioButton,
  RadioGroup,
  TabPane,
  Tabs
} from 'ant-design-vue';
import { DatePicker, Icon, Image, Input, notification, Select, SelectUser, Cropper } from '@xcan-angus/vue-ui';
import { GM } from '@xcan-angus/infra';
import { project } from 'src/api/tester';

const { t } = useI18n();

interface Props {
  projectId: string;
  data?: {
    tab: string
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  data: undefined
});
const emit = defineEmits<{(e: 'ok'): void}>();

const changeProjectInfo = inject('changeProjectInfo', (projectId: string, force: boolean) => ({ projectId, force }));
const getNewCurrentProject = inject('getNewCurrentProject', () => (undefined));
const projectInfo = inject('projectInfo', ref({ id: '' }));
const delTabPane = inject('delTabPane');

const activeKey = ref('basic');
const projectTypeTipConfig = {
  AGILE: [t('project.projectEdit.projectTypeTip.agile.features'), t('project.projectEdit.projectTypeTip.agile.scenarios')],
  GENERAL: [t('project.projectEdit.projectTypeTip.general.features'), t('project.projectEdit.projectTypeTip.general.scenarios')],
  TESTING: [t('project.projectEdit.projectTypeTip.testing.features'), t('project.projectEdit.projectTypeTip.testing.scenarios')]
};

const projectTypeName = {
  AGILE: t('project.projectEdit.projectTypeName.agile'),
  GENERAL: t('project.projectEdit.projectTypeName.general'),
  TESTING: t('project.projectEdit.projectTypeName.testing')
};

const members = ref({
  USER: [],
  DEPT: [],
  GROUP: []
});
const memberType = ref('user');
const descRef = ref();
const uploadAvatarVisible = ref(false);
const uploadParams = {
  bizKey: 'angusTesterProjectAvatar'
};

const uploadOption = {
  outputSize: 1,
  outputType: 'png',
  info: true,
  canScale: true,
  autoCrop: true,
  autoCropWidth: '280',
  autoCropHeight: '280',
  fixed: true,
  fixedNumber: [1, 1],
  full: false,
  fixedBox: false,
  canMove: true,
  canMoveBox: true,
  original: false,
  centerBox: false,
  high: true,
  infoTrue: false,
  maxImgSize: 2000,
  enlarge: 1,
  mode: 'contain',
  preW: 0,
  limitMinSize: [100, 100]
};

const defaultOptionsUser = ref<{ [key: string]: any }>({});
const defaultOptionsDept = ref<{ [key: string]: any }>({});
const defaultOptionsGroup = ref<{ [key: string]: any }>({});

const Tags = defineAsyncComponent(() => import('@/views/project/project/edit/tag/index.vue'));
const Module = defineAsyncComponent(() => import('@/views/project/project/edit/module/index.vue'));
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));
const Version = defineAsyncComponent(() => import('@/views/task/version/list/index.vue'));

const toolbarOptions = ['title', 'color', 'weight', 'block', 'link', 'list', 'direction', 'table', 'zoom'];
const projectData = ref<{[key: string]: string | boolean | string[] | object}>({ type: 'AGILE', importExample: false });
const formRef = ref();
const loading = ref(false);
const loadProjectData = async () => {
  const [error, { data }] = await project.getProjectDetail(props.projectId);
  if (error) {
    return;
  }
  // projectData.value = data;
  const { startDate, deadlineDate, type, id, name, ownerId, description, avatar } = data;
  projectData.value = { id, name, ownerId, description, avatar };
  projectData.value.members = data.members;
  projectData.value.type = type?.value || 'AGILE';
  projectData.value.dateRange = [startDate, deadlineDate];
  if (projectData.value.members?.USER) {
    members.value.USER = projectData.value.members?.USER.map(i => {
      defaultOptionsUser.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }

  if (projectData.value.members?.DEPT) {
    members.value.DEPT = projectData.value.members?.DEPT.map(i => {
      defaultOptionsDept.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }

  if (projectData.value.members?.GROUP) {
    members.value.GROUP = projectData.value.members?.GROUP.map(i => {
      defaultOptionsGroup.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }
};

const openCropper = () => {
  uploadAvatarVisible.value = true;
};

const uploadImgSuccess = (resp) => {
  projectData.value.avatar = resp?.data?.[0]?.url;
};

const delImg = () => {
  projectData.value.avatar = '';
};

const selectProjectType = (value) => {
  projectData.value.type = value;
};

const validateDesc = () => {
  if (descRef.value && descRef.value.getLength() > 2000) {
    return Promise.reject(t('project.projectEdit.validation.maxCharacters'));
  }
  return Promise.resolve();
};

const ok = async () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const { USER, DEPT, GROUP } = members.value;
    const { dateRange, ...otherProject } = projectData.value;
    const [error] = props.projectId
      ? await project.updateProject({ ...otherProject, startDate: dateRange[0], deadlineDate: dateRange[1], memberTypeIds: { USER: USER.length ? USER : undefined, DEPT: DEPT.length ? DEPT : undefined, GROUP: GROUP.length ? GROUP : undefined } })
      : await project.addProject({ ...otherProject, startDate: dateRange[0], deadlineDate: dateRange[1], memberTypeIds: { USER: USER.length ? USER : undefined, DEPT: DEPT.length ? DEPT : undefined, GROUP: GROUP.length ? GROUP : undefined } });
    loading.value = false;
    if (error) {
      return;
    }
    if (props.projectId === projectInfo.value.id) {
      changeProjectInfo(props.projectId, true);
    }
    emit('ok');
    if (props.projectId) {
      notification.success(t('project.projectEdit.messages.updateSuccess'));
    } else {
      notification.success(t('project.projectEdit.messages.createSuccess'));
      delTabPane('addProject');
    }
    getNewCurrentProject();
  });
};

const cancel = () => {
  delTabPane(`${props.projectId}_project`);
  delTabPane('addProject');
};

onMounted(() => {
  if (props.projectId) {
    loadProjectData();
    if (props.data?.tab) {
      activeKey.value = props.data.tab;
    }
  }
});

</script>
<template>
  <div class="project-edit-container">
    <div v-if="!props.projectId" class="project-create-layout">
      <div class="form-content-wrapper">
        <Form
          ref="formRef"
          labelAlign="right"
          :colon="false"
          class="project-form"
          :model="projectData"
          :labelCol="{ style: {width: '80px'} }">
          <div class="form-main-content">
            <!-- 项目类型选择区域 -->
            <div class="project-type-section">
              <h3 class="section-title">{{ t('project.projectEdit.tabs.projectType') }}</h3>
              <div class="project-type-cards">
                <div
                  class="project-type-card"
                  :class="{ 'selected': projectData.type === 'AGILE' }"
                  @click="selectProjectType('AGILE')">
                  <Icon
                    v-show="projectData.type === 'AGILE'"
                    icon="icon-xuanzhongduigou"
                    class="selection-icon" />
                  <Icon icon="icon-minjiexiangmuguanli" class="type-icon" />
                  <div class="type-name">{{ t('project.projectEdit.projectTypeName.agile') }}</div>
                </div>
                <div
                  class="project-type-card"
                  :class="{ 'selected': projectData.type === 'GENERAL' }"
                  @click="selectProjectType('GENERAL')">
                  <Icon
                    v-show="projectData.type === 'GENERAL'"
                    icon="icon-xuanzhongduigou"
                    class="selection-icon" />
                  <Icon icon="icon-jiandanxiangmuguanli" class="type-icon" />
                  <div class="type-name">{{ t('project.projectEdit.projectTypeName.general') }}</div>
                </div>
                <div
                  class="project-type-card"
                  :class="{ 'selected': projectData.type === 'TESTING' }"
                  @click="selectProjectType('TESTING')">
                  <Icon
                    v-show="projectData.type === 'TESTING'"
                    icon="icon-xuanzhongduigou"
                    class="selection-icon" />
                  <Icon icon="icon-ceshixiangmuguanli" class="type-icon" />
                  <div class="type-name">{{ t('project.projectEdit.projectTypeName.testing') }}</div>
                </div>
              </div>
            </div>

            <!-- 表单填写区域 -->
            <div class="form-fields-section">
              <h3 class="section-title">{{ t('project.projectEdit.tabs.basicInfo') }}</h3>
              <div class="form-fields-content">
                <FormItem label=" " class="avatar-upload-item">
                  <div class="avatar-upload-container">
                    <div v-if="projectData.avatar" class="avatar-preview">
                      <div class="avatar-wrapper">
                        <Image
                          :src="projectData.avatar"
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
                      <div
                        class="upload-area"
                        @click="openCropper">
                        <img src="../images/default.png" class="upload-icon" />
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
                    v-model:value="projectData.name"
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
                    v-model:value="projectData.dateRange"
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
                      v-model:value="projectData.ownerId"
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
                      v-model:value="projectData.importExample"
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
                  :rules="[{validator: validateDesc}]"
                  class="form-field description-field">
                  <RichEditor
                    ref="descRef"
                    v-model:value="projectData.description"
                    :toolbarOptions="toolbarOptions"
                    :options="{placeholder: t('project.projectEdit.form.descriptionPlaceholder')}"
                    class="enhanced-editor" />
                </FormItem>
              </div>
            </div>

            <!-- 项目类型预览区域 -->
            <div class="project-preview-section">
              <h3 class="section-title">{{ projectTypeName[projectData.type] }}</h3>
              <div class="preview-content">
                <div class="preview-image">
                  <img
                    v-show="projectData.type==='AGILE'"
                    src="../../add/agile.png"
                    class="preview-img agile" />
                  <img
                    v-show="projectData.type==='GENERAL'"
                    src="../../add/general.png"
                    class="preview-img general" />
                  <img
                    v-show="projectData.type==='TESTING'"
                    src="../../add/testing.png"
                    class="preview-img testing" />
                </div>
                <div class="preview-features">
                  <div
                    v-for="(item, index) in projectTypeTipConfig[projectData.type]"
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
                @click="ok">
                <Icon icon="icon-dangqianxuanzhong" class="button-icon" />
                {{ t('actions.save') }}
              </Button>
              <Button
                size="small"
                class="secondary-button"
                @click="cancel">
                {{ t('actions.cancel') }}
              </Button>
            </div>
          </FormItem>
        </Form>
      </div>
    </div>

    <Tabs
      v-if="props.projectId"
      v-model:activeKey="activeKey"
      size="small">
      <TabPane key="basic" :tab="t('project.projectEdit.tabs.basicInfo')">
        <Form
          ref="formRef"
          labelAlign="right"
          :colon="false"
          class="w-300 pb-10"
          :model="projectData"
          :labelCol="{ style: {width: '70px'} }">
          <div class="flex space-x-10">
            <div class="w-170">
              <FormItem label=" ">
                <div v-if="projectData.avatar" class="flex space-x-2 justify-start">
                  <div class="relative">
                    <Image
                      :src="projectData.avatar"
                      class="w-25 h-25"
                      alt="avatar" />
                    <div class="absolute left-0 top-0 right-0 bottom-0 text-center opacity-0 hover:opacity-100" style="background-color: rgba(15, 23, 35, 50%);">
                      <Icon
                        icon="icon-qingchu"
                        class="mt-10 cursor-pointer text-5 text-white"
                        @click="delImg" />
                    </div>
                  </div>
                </div>
                <div v-else class="text-left">
                  <div
                    class="ant-upload-text w-25 h-25 rounded cursor-pointer bg-blue-2  flex flex-col items-center spacey-2 justify-around"
                    @click="openCropper">
                    <img src="../images/default.png" class="w-15" />
                    <div class="text-3">{{ t('project.projectEdit.actions.clickToReplaceIcon') }}</div>
                  </div>
                </div>
              </FormItem>
              <FormItem
                :label="t('project.projectEdit.form.projectName')"
                name="name"
                class="input_select"
                required>
                <Input
                  v-model:value="projectData.name"
                  :placeholder="t('project.projectEdit.form.projectNamePlaceholder')"
                  :maxlength="100">
                </Input>
              </FormItem>

              <FormItem
                :label="t('project.projectEdit.form.timePlan')"
                name="dateRange"
                class="input_select"
                required>
                <DatePicker
                  v-model:value="projectData.dateRange"
                  class="w-full"
                  :allowClear="false"
                  type="date-range">
                </DatePicker>
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-100">
                      {{ t('project.projectEdit.form.timePlanDescription') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips absolute -right-5 top-2 text-3.5" />
                </Popover>
              </FormItem>

              <FormItem
                :label="t('project.projectEdit.form.owner')"
                name="ownerId"
                required
                class="relative input_select">
                <SelectUser
                  v-model:value="projectData.ownerId"
                  size="small"
                  :placeholder="t('project.projectEdit.form.ownerPlaceholder')"
                  :allowClear="false" />
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-100">
                      {{ t('project.projectEdit.form.ownerDescription') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips absolute -right-5 top-2 text-3.5" />
                </Popover>
              </FormItem>

              <FormItem
                :label="t('project.projectEdit.form.projectMembers')"
                class="input_select"
                required>
                <RadioGroup
                  v-model:value="memberType"
                  buttonStyle="solid"
                  size="small">
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
                <Popover placement="right">
                  <template #content>
                    <div class="max-w-110">
                      {{ t('project.projectEdit.form.membersDescription') }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips text-3.5 ml-2" />
                </Popover>
                <div class="mt-3.5">
                  <Select
                    v-show="memberType === 'user'"
                    v-model:value="members.USER"
                    :showSearch="true"
                    :placeholder="t('project.projectEdit.form.selectUser')"
                    :action="`${GM}/user?fullTextSearch=true`"
                    :defaultOptions="defaultOptionsUser"
                    mode="multiple"
                    :fieldNames="{ label: 'fullName', value: 'id' }">
                  </Select>

                  <Select
                    v-show="memberType === 'dept'"
                    v-model:value="members.DEPT"
                    :placeholder="t('project.projectEdit.form.selectDepartment')"
                    :showSearch="true"
                    :action="`${GM}/dept?fullTextSearch=true`"
                    :defaultOptions="defaultOptionsDept"
                    mode="multiple"
                    :fieldNames="{ label: 'name', value: 'id' }">
                  </Select>

                  <Select
                    v-show="memberType === 'group'"
                    v-model:value="members.GROUP"
                    :placeholder="t('project.projectEdit.form.selectGroup')"
                    :showSearch="true"
                    :action="`${GM}/group?fullTextSearch=true`"
                    :defaultOptions="defaultOptionsGroup"
                    mode="multiple"
                    :fieldNames="{ label: 'name', value: 'id' }">
                  </Select>
                </div>
              </FormItem>

              <FormItem
                :label="t('project.projectEdit.form.description')"
                name="description"
                :rules="[{validator: validateDesc}]">
                <RichEditor
                  ref="descRef"
                  v-model:value="projectData.description"
                  :toolbarOptions="toolbarOptions"
                  :options="{placeholder: t('project.projectEdit.form.descriptionPlaceholder')}" />
              </FormItem>
            </div>
            <div class="w-100 pt-12">
              <div class="text-5 font-semibold mb-2">{{ projectTypeName[projectData.type] }}</div>
              <div class="py-3 border rounded min-h-40 flex flex-col justify-center items-center">
                <img
                  v-show="projectData.type==='AGILE'"
                  src="../../add/agile.png"
                  class="inline-block"
                  style="width: 74%" />
                <img
                  v-show="projectData.type==='GENERAL'"
                  src="../../add/general.png"
                  class="inline-block"
                  style="width: 92%" />
                <img
                  v-show="projectData.type==='TESTING'"
                  src="../../add/testing.png"
                  class="inline-block"
                  style="width: 70%" />
              </div>
              <div class="space-y-2 mt-7">
                <div
                  v-for="(item, index) in projectTypeTipConfig[projectData.type]"
                  :key="index"
                  class="flex space-x-1">
                  <Icon icon="icon-duihao-copy" class="text-3.5 mt-1" />
                  <p class="text-3.5">{{ item }}</p>
                </div>
              </div>
            </div>
          </div>
          <FormItem>
            <div class="flex items-center">
              <Button
                type="primary"
                size="small"
                htmlType="submit"
                :loading="loading"
                @click="ok">
                <Icon icon="icon-dangqianxuanzhong" class="mr-1" />
                {{ t('actions.save') }}
              </Button>
              <Button
                size="small"
                class="ml-2"
                @click="cancel">
                {{ t('actions.cancel') }}
              </Button>
            </div>
          </FormItem>
        </Form>
      </TabPane>
      <TabPane key="module" :tab="t('project.projectEdit.tabs.module')">
        <Module :projectId="props.projectId" :projectName="projectData.name" />
      </TabPane>
      <TabPane key="version" :tab="t('project.projectEdit.tabs.version')">
        <Version
          :projectId="props.projectId"
          class="!p-0"
          :showDetail="false" />
      </TabPane>
      <TabPane key="biaoqian" :tab="t('project.projectEdit.tabs.tag')">
        <Tags :projectId="props.projectId" />
      </TabPane>
    </Tabs>
    <Cropper
      v-model:visible="uploadAvatarVisible"
      :title="t('project.projectEdit.actions.uploadIcon')"
      :params="uploadParams"
      :options="uploadOption"
      @success="uploadImgSuccess" />
  </div>
</template>
<style scoped>
/* 整体容器样式 */
.project-edit-container {
  padding: 24px;
  height: 100%;
  overflow: auto;
  background: transparent;
}

/* 项目创建布局 */
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

/* 项目类型选择区域 */
.project-type-section {
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e5e7eb;
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
  border: 2px solid #e5e7eb;
  border-radius: 12px;
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

/* 表单字段区域 */
.form-fields-section {
  display: flex;
  flex-direction: column;
}

.form-fields-content {
  flex: 1;
}

.form-field {
  margin-bottom: 24px;
}

.form-field.with-tooltip {
  position: relative;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

/* 头像上传样式 */
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

/* 增强的表单控件 */
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
}

/* 成员选择器样式 */
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

/* 描述字段样式 */
.description-field {
  margin-bottom: 32px;
}

/* 工具提示样式 */
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

/* 项目预览区域 */
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

/* 操作按钮样式 */
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
  border-radius: 8px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.secondary-button {
  padding: 8px 24px;
  height: auto;
  border-radius: 8px;
  font-weight: 500;
}

.button-icon {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .form-main-content {
    grid-template-columns: 150px 1fr 200px;
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .form-main-content {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>
