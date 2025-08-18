<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('project.aboutProject') }}</div>
    <div class="mb-6">
      <div>{{ t('project.projectDescription') }}</div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('project.manageAndOrganizeWork') }}</div>
            <div>{{ t('project.manageAndOrganizeWorkDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('project.promoteTeamCollaboration') }}</div>
            <div>{{ t('project.promoteTeamCollaborationDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('project.trackAndReportProgress') }}</div>
            <div>{{ t('project.trackAndReportProgressDesc') }}</div>
          </div>
        </div>
      </div>

      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('project.improveWorkTransparency') }}</div>
            <div>{{ t('project.improveWorkTransparencyDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/5.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('project.projectTeamInfoSharing') }}</div>
            <div>{{ t('project.projectTeamInfoSharingDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/6.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('project.riskAndIssueManagement') }}</div>
            <div>{{ t('project.riskAndIssueManagementDesc') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
