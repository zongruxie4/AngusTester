import { useI18n } from 'vue-i18n';
import { enumUtils } from '@xcan-angus/infra';
import { ProjectType } from '@/enums/enums';
import { TreeData } from '@/views/project/project/types';

// Configuration objects - exported as functions to use i18n properly in component context
export const getProjectTypeTipConfig = () => {
  const { t } = useI18n();
  return {
    AGILE: [t('project.projectEdit.projectTypeTip.agile.features'), t('project.projectEdit.projectTypeTip.agile.scenarios')],
    GENERAL: [t('project.projectEdit.projectTypeTip.general.features'), t('project.projectEdit.projectTypeTip.general.scenarios')],
    TESTING: [t('project.projectEdit.projectTypeTip.testing.features'), t('project.projectEdit.projectTypeTip.testing.scenarios')]
  };
};

/** Project type name mapping for UI display */
export const getProjectTypeName = () => {
  return enumUtils.enumToMap(ProjectType);
};

export const toolbarOptions = ['title', 'color', 'weight', 'block', 'link', 'list', 'direction', 'table', 'zoom'];

export const uploadParams = {
  bizKey: 'angusTesterProjectAvatar'
};

/**
 * Recursively traverse tree data and apply callback to each node
 * @param treeData - Array of tree nodes to traverse
 * @param callback - Function to apply to each node
 * @returns Processed tree data
 */
export const travelTreeData = (treeData: TreeData[], callback = (item: TreeData) => item): TreeData[] => {
  function travel (treeData: TreeData[], level = 0, ids: string[] = []) {
    treeData.forEach((item, idx) => {
      // Set node properties for tree structure
      item.level = level;
      item.index = idx;
      item.ids = [...ids, item.id];
      item.isLast = idx === (treeData.length - 1);

      // Recursively process children
      travel(item.children || [], level + 1, item.ids);

      // Calculate child levels for depth validation
      item.childLevels = (item.children?.length ? Math.max(...item.children.map(i => i.childLevels || 0)) : 0) + 1;

      // Apply callback function to current item
      const processedItem = callback(item);
      if (processedItem !== item) {
        Object.assign(item, processedItem);
      }
    });
  }
  travel(treeData);
  return treeData;
};
