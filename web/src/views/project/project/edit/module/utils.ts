
type TreeData = {
  name: string;
  sequece: string;
  level: number;
  children?: TreeData[];
  id: string;
  pid?: string
}

export const travelTreeData = (treeData, callback = (item) => item) => {
  function travel (treeData, level = 0, ids: string[] = []) {
    treeData.forEach((item, idx) => {
      item.level = level;
      item.index = idx;
      item.ids = [...ids, item.id];
      item.isLast = idx === (treeData.length - 1);
      travel(item.children || [], level + 1, item.ids),
      item.childLevels = (item.children?.length ? Math.max(...item.children.map(i => i.childLevels)) : 0) + 1;
      item = callback(item);
    });
  }
  travel(treeData);
  return treeData;
};
