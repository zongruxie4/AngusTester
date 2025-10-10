import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/task';
  }

  addTask (parmas): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, parmas);
  }

  putTask (id: number, parmas): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}`, parmas);
  }

  updateTask (taskId: number, parmas): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${taskId}`, parmas);
  }

  getTaskDetail (id: number): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  getTaskList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  moveTask<T> (params: T) {
    return http.patch(`${baseUrl}/move`, params, { paramsType: false });
  }

  startTask (id: number): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/start`);
  }

  cancelTask (id: number, option = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/cancel`, null, option);
  }

  completeTask (id: number): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/complete`);
  }

  deleteTask (ids: number[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  processedTask (id: number): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/processed`);
  }

  confirmTask (id: number, status: 'SUCCESS' | 'FAIL'): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/result/${status}/confirm`);
  }

  reopenTask (id: number): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/reopen`);
  }

  restartTask (id: number): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/restart`);
  }

  editTaskName (id: number, name: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/name?name=${name}`);
  }

  editTaskDescription (id: number, parmas: {description: string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/description`, parmas);
  }

  editEvalWorkloadApi<T> (id: number, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/evalWorkload`, params);
  }

  editActualWorkload<T> (id: number, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/actualWorkload`, params);
  }

  editDeadlineDateApi (id: number, deadlineDate: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/deadline/${deadlineDate}`);
  }

  editTaskAssignees (id: number, parmas: { assigneeId: number }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/assignee`, parmas);
  }

  editTaskConfirmer (id: number, parmas: { confirmerId: number|null }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/confirmer`, parmas);
  }

  editTaskPriority (id: number, priority: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/priority/${priority}`);
  }

  editTaskTaskType (id: number, taskType: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/type?type=${taskType}`);
  }

  editTaskAttachment (id: number, parmas: { attachments: { name: string, url: string }[] }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/attachment`, parmas);
  }

  editTaskTags (id: number, parmas: { tagIds: number[] | null }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/tag`, parmas);
  }

  importTask (param: FormData) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, param);
  }

  associationTask (taskId: number, assocTaskIds: number[], axiosConf = {}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/association/task`, {
      assocTaskIds
    }, axiosConf);
  }

  cancelAssociationTask (taskId: number, assocTaskIds: number[], axiosConf = {}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/association/task/cancel`, {
      assocTaskIds
    }, axiosConf);
  }

  associationCase (taskId: number, assocCaseIds: number[], axiosConf = {}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/association/case`, {
      assocCaseIds
    }, axiosConf);
  }

  cancelAssociationCase (taskId: number, assocCaseIds: number[], axiosConf = {}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/association/case/cancel`, {
      assocCaseIds
    }, axiosConf);
  }

  addTaskRemark (params: { taskId: number, content: string }): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/remark`, params);
  }

  deleteTaskRemark (remarkId: number): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/remark/${remarkId}`);
  }

  favouriteTask (id:number, option?: {[key: string]: any}): Promise<[Error | null, any]> {
    if (!option) { option = {}; }
    return http.post(`${baseUrl}/${id}/favourite`, null, { ...option });
  }

  followTask (id:number, option = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/follow`, null, option);
  }

  cancelFavouriteTask (id:number, option = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/favourite`, null, option);
  }

  cancelFollowTask (id:number, option = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/follow`, null, option);
  }

  deleteTrashTask (taskId: number) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${taskId}`);
  }

  deleteAllTrashTask (params: {projectId: number}) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash`, params);
  }

  backTrashTask (taskId: number) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${taskId}/back`);
  }

  backAllTrashTask (params: {projectId: number}) : Promise<[Error | null, any]> {
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

  getSprintDetail (sprintId: number) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/sprint/${sprintId}`);
  }

  getSprintList <T> (params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/sprint`, { ...params, fullTextSearch: true });
  }

  deleteSprint (sprintId: number) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/sprint/${sprintId}`);
  }

  cloneSprint (sprintId: number) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/${sprintId}/clone`);
  }

  startSprint (sprintId: number, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/${sprintId}/start`, null, axiosConf);
  }

  endSprint (sprintId: number, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/${sprintId}/end`, null, axiosConf);
  }

  blockSprint (sprintId: number, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/${sprintId}/block`, null, axiosConf);
  }

  reopenSprint <T> (params: T, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/task/reopen`, params, axiosConf);
  }

  restartSprint <T> (params: T, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/sprint/task/restart`, params, axiosConf);
  }

  getUserSprintAuth<T> (sprintId: number, userId: number, params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/sprint/${sprintId}/user/${userId}/auth`, params);
  }

  getCurrentUserSprintAuth<T> (sprintId: number, params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/sprint/${sprintId}/user/auth/current`, params);
  }

  getTaskResult (taskType: string, targetId: number, testType: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${taskType}/${targetId}/${testType}/result`);
  }

  setSubTask<T> (taskId: number, params:T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/subtask/set`, params, { paramsType: true });
  }

  cancelSubTask<T> (taskId: number, params:T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/subtask/cancel`, params, { paramsType: true });
  }

  addMeeting <T> (params: T) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/meeting`, params);
  }

  updateMeeting <T> (params: T) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/meeting`, params);
  }

  deleteMeeting (meetingId: number) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/meeting/${meetingId}`);
  }

  getMeetingDetail (meetingId: number) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/meeting/${meetingId}`);
  }

  getMeetingList <T> (params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/meeting`, { ...params, fullTextSearch: true });
  }
}
