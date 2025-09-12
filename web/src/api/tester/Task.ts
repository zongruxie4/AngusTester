import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/task';
  }

  addTask (parmas): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, parmas);
  }

  putTask (id: string, parmas): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}`, parmas);
  }

  updateTask (taskId: string, parmas): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${taskId}`, parmas);
  }

  getTaskDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  getTaskList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  moveTask<T> (params: T) {
    return http.patch(`${baseUrl}/move`, params, { paramsType: false });
  }

  startTask (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/start`);
  }

  cancelTask (id: string, option = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/cancel`, null, option);
  }

  completeTask (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/complete`);
  }

  deleteTask (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  processedTask (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/processed`);
  }

  confirmTask (id: string, status: 'SUCCESS' | 'FAIL'): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/result/${status}/confirm`);
  }

  reopenTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/reopen`);
  }

  restartTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/restart`);
  }

  editTaskName (id: string, name: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/name?name=${name}`);
  }

  editTaskDescription (id: string, parmas: {description: string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/description`, parmas);
  }

  editEvalWorkloadApi<T> (id: string, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/evalWorkload`, params);
  }

  editActualWorkload<T> (id: string, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/actualWorkload`, params);
  }

  editDeadlineDateApi (id: string, deadlineDate: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/deadline/${deadlineDate}`);
  }

  editTaskAssignees (id: string, parmas: { assigneeId: string }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/assignee`, parmas);
  }

  editTaskConfirmer (id: string, parmas: { confirmerId: string|null }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/confirmer`, parmas);
  }

  editTaskPriority (id: string, priority: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/priority/${priority}`);
  }

  editTaskTaskType (id: string, taskType: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/type?type=${taskType}`);
  }

  editTaskAttachment (id: string, parmas: { attachments: { name: string, url: string }[] }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/attachment`, parmas);
  }

  editTaskTags (id: string, parmas: { tagIds: string[] | null }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/tag`, parmas);
  }

  importTask (param: FormData) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, param);
  }

  associationTask (taskId: string, assocTaskIds: string[], axiosConf = {}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/association/task`, {
      assocTaskIds
    }, axiosConf);
  }

  cancelAssociationTask (taskId: string, assocTaskIds: string[], axiosConf = {}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/association/task/cancel`, {
      assocTaskIds
    }, axiosConf);
  }

  associationCase (taskId: string, assocCaseIds: string[], axiosConf = {}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/association/case`, {
      assocCaseIds
    }, axiosConf);
  }

  cancelAssociationCase (taskId: string, assocCaseIds: string[], axiosConf = {}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/association/case/cancel`, {
      assocCaseIds
    }, axiosConf);
  }

  addTaskRemark (params: { taskId: string, content: string }): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/remark`, params);
  }

  deleteTaskRemark (remarkId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/remark/${remarkId}`);
  }

  favouriteTask (id:string, option?: {[key: string]: any}): Promise<[Error | null, any]> {
    if (!option) { option = {}; }
    return http.post(`${baseUrl}/${id}/favourite`, null, { ...option });
  }

  followTask (id:string, option = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/follow`, null, option);
  }

  cancelFavouriteTask (id:string, option = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/favourite`, null, option);
  }

  cancelFollowTask (id:string, option = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/follow`, null, option);
  }

  deleteTrashTask (taskId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${taskId}`);
  }

  deleteAllTrashTask (params: {projectId: string}) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash`, params);
  }

  backTrashTask (taskId: string) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${taskId}/back`);
  }

  backAllTrashTask (params: {projectId: string}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/back`, params, { paramsType: true });
  }

  getTrashTaskList <T> (params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash`, { ...params, fullTextSearch: true });
  }

  addSprint <T> (params: T): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/sprint`, params);
  }

  putSprint <T> (params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/sprint`, params);
  }

  getSprintDetail (sprintId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/sprint/${sprintId}`);
  }

  getSprintList <T> (params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/sprint`, { ...params, fullTextSearch: true });
  }

  deleteSprint (sprintId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/sprint/${sprintId}`);
  }

  cloneSprint (sprintId: string) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/${sprintId}/clone`);
  }

  startSprint (sprintId: string, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/${sprintId}/start`, null, axiosConf);
  }

  endSprint (sprintId: string, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/${sprintId}/end`, null, axiosConf);
  }

  blockSprint (sprintId: string, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/${sprintId}/block`, null, axiosConf);
  }

  reopenSprint <T> (params: T, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/task/reopen`, params, axiosConf);
  }

  restartSprint <T> (params: T, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/task/restart`, params, axiosConf);
  }

  getUserSprintAuth<T> (sprintId: string, userId: string, params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/sprint/${sprintId}/user/${userId}/auth`, params);
  }

  getCurrentUserSprintAuth<T> (sprintId: string, params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/sprint/${sprintId}/user/auth/current`, params);
  }

  getTaskResult (taskType: string, targetId: string, testType: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${taskType}/${targetId}/${testType}/result`);
  }

  setSubTask<T> (taskId: string, params:T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/subtask/set`, params, { paramsType: true });
  }

  cancelSubTask<T> (taskId: string, params:T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/subtask/cancel`, params, { paramsType: true });
  }

  addMeeting <T> (params: T) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/meeting`, params);
  }

  updateMeeting <T> (params: T) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/meeting`, params);
  }

  deleteMeeting (meetingId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/meeting/${meetingId}`);
  }

  getMeetingDetail (meetingId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/meeting/${meetingId}`);
  }

  getMeetingList <T> (params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/meeting`, { ...params, fullTextSearch: true });
  }
}
