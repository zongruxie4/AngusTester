import { ref, computed, reactive } from 'vue';
import { useI18n } from 'vue-i18n';
import { appContext } from '@xcan-angus/infra';
import { ProjectType } from '@/enums/enums';
import type { Project, ProjectMember, ProjectTypeConfig, CreateProjectParams, UpdateProjectParams } from '../types';

export function useManagement () {
  const { t } = useI18n();
  const userInfo = ref(appContext.getUser());

  const getProjectTypeTipConfig = (): ProjectTypeConfig => {
    return {
      AGILE: [
        t('project.projectEdit.projectTypeTip.agile.features'),
        t('project.projectEdit.projectTypeTip.agile.scenarios')
      ],
      GENERAL: [
        t('project.projectEdit.projectTypeTip.general.features'),
        t('project.projectEdit.projectTypeTip.general.scenarios')
      ],
      TESTING: [
        t('project.projectEdit.projectTypeTip.testing.features'),
        t('project.projectEdit.projectTypeTip.testing.scenarios')
      ]
    };
  };

  const getProjectTypeNames = () => {
    return {
      AGILE: t('project.projectEdit.projectTypeName.agile'),
      GENERAL: t('project.projectEdit.projectTypeName.general'),
      TESTING: t('project.projectEdit.projectTypeName.testing')
    };
  };

  const memberType = ref<'user' | 'dept' | 'group'>('user');
  const projectType = ref<ProjectType>(ProjectType.AGILE);

  const members = reactive<{
    USER: string[];
    DEPT: string[];
    GROUP: string[];
  }>({
    USER: [],
    DEPT: [],
    GROUP: []
  });

  const defaultOptionsUser = ref<Record<string, ProjectMember>>({});
  const defaultOptionsDept = ref<Record<string, ProjectMember>>({});
  const defaultOptionsGroup = ref<Record<string, ProjectMember>>({});

  const formData = reactive<{
    id?: string;
    name: string;
    ownerId?: string;
    description: string;
    avatar: string;
    dateRange?: [string, string];
    importExample: boolean;
  }>({
    name: '',
    ownerId: undefined,
    description: '',
    avatar: '',
    dateRange: undefined,
    importExample: true
  });

  const projectTypeTipConfig = computed(() => getProjectTypeTipConfig());
  const projectTypeNames = computed(() => getProjectTypeNames());

  const isFormValid = computed(() => {
    return !!(formData.name?.trim() &&
             formData.ownerId &&
             formData.dateRange &&
             formData.dateRange[0] &&
             formData.dateRange[1]);
  });

  const totalMemberCount = computed(() => {
    return members.USER.length + members.DEPT.length + members.GROUP.length;
  });

  const selectProjectType = (type: ProjectType): void => {
    projectType.value = type;
  };

  const initializeForm = (): void => {
    formData.name = '';
    formData.ownerId = undefined;
    formData.description = '';
    formData.avatar = '';
    formData.dateRange = undefined;
    formData.importExample = true;

    projectType.value = ProjectType.AGILE;
    memberType.value = 'user';

    members.USER = [];
    members.DEPT = [];
    members.GROUP = [];

    defaultOptionsUser.value = {};
    defaultOptionsDept.value = {};
    defaultOptionsGroup.value = {};

    if (userInfo.value?.id) {
      const userId = String(userInfo.value.id);
      members.USER = [userId];
      defaultOptionsUser.value = {
        [userId]: {
          id: userId,
          name: userInfo.value.fullName || userInfo.value.username || '',
          fullName: userInfo.value.fullName || userInfo.value.username || '',
          avatar: userInfo.value.avatar,
          disabled: true
        }
      };
    }
  };

  const loadProjectIntoForm = (project: Project): void => {
    formData.id = project.id;
    formData.name = project.name || '';
    formData.ownerId = project.ownerId;
    formData.description = project.description || '';
    formData.avatar = project.avatar || '';
    formData.dateRange = project.dateRange || [project.startDate || '', project.deadlineDate || ''];
    formData.importExample = project.importExample ?? false;

    projectType.value = project.type?.value || ProjectType.AGILE;

    if (project.members) {
      members.USER = project.members.USER?.map(m => m.id) || [];
      members.DEPT = project.members.DEPT?.map(m => m.id) || [];
      members.GROUP = project.members.GROUP?.map(m => m.id) || [];
    }
  };

  const buildCreateParams = (): CreateProjectParams => {
    const { dateRange, ...otherFormData } = formData;

    return {
      ...otherFormData,
      name: formData.name,
      ownerId: formData.ownerId!,
      type: projectType.value,
      startDate: dateRange?.[0],
      deadlineDate: dateRange?.[1],
      memberTypeIds: {
        USER: members.USER.length ? members.USER : undefined,
        DEPT: members.DEPT.length ? members.DEPT : undefined,
        GROUP: members.GROUP.length ? members.GROUP : undefined
      }
    };
  };

  const buildUpdateParams = (): UpdateProjectParams => {
    return {
      ...buildCreateParams(),
      id: formData.id!
    };
  };

  const validateForm = async (): Promise<boolean> => {
    return isFormValid.value;
  };

  return {
    memberType,
    projectType,
    members,
    defaultOptionsUser,
    defaultOptionsDept,
    defaultOptionsGroup,
    formData,
    userInfo,
    projectTypeTipConfig,
    projectTypeNames,
    isFormValid,
    totalMemberCount,
    selectProjectType,
    initializeForm,
    loadProjectIntoForm,
    buildCreateParams,
    buildUpdateParams,
    validateForm
  };
}

export type UseProjectReturn = ReturnType<typeof useManagement>;
