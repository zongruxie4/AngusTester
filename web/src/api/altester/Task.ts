import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/task';
  }

  // TODO Q3 API未提取出来

  loadTaskList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  addTag (names: string[]): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/tag`, names);
  }

  addTask (parmas): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, parmas);
  }

  editTask (id: string, parmas): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}`, parmas);
  }

  editTaskName (id: string, name: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/name?name=${name}`);
  }

  editTaskDescription (id: string, description: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/description`, { description: description });
  }

  editEvalWorkloadApi (id: string, workload: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/evalWorkload`, { workload: workload });
  }

  editActualWorkload (id: string, workload: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/actualWorkload`, { workload: workload });
  }

  editDeadlineDateApi (id: string, deadlineDate: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/deadline/${deadlineDate}`);
  }

  editTaskAssignees (id: string, parmas: { assigneeIds: string[] }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/assignee`, parmas);
  }

  editConfirmors (id: string, parmas: { confirmorIds: string[]|null }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/confirmor`, parmas);
  }

  editTaskPriority (id: string, priority: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/priority/${priority}`);
  }

  editTaskTaskType (id: string, taskType: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/type?type=${taskType}`);
  }

  updateAttachment (id: string, parmas: { attachments: { name: string, url: string }[] }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/attachment`, parmas);
  }

  editTagsApi (id: string, parmas: { tagIds: string[] | null }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/tag`, parmas);
  }

  loadTaskInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  startProcessing (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/start`);
  }

  cancelTask (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/cancel`);
  }

  completeTask (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/complete`);
  }

  deleteTask (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}?ids=${[id]}`);
  }

  processedTask (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/processed`);
  }

  reopenTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/reopen`);
  }

  restartTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/restart`);
  }

  loadTaskRemark (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/remark`, params);
  }

  addTaskRemark (params: { taskId: string, content: string }): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/remark`, params);
  }

  delTaskRemark (remarkId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/remark/${remarkId}`);
  }

  // 确认任务
  confirmTask (id: string, status: 'SUCCESS' | 'FAIL'): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/result/${status}/confirm`);
  }

  // 更改结果
  updateResult (id, result: 'SUCCESS' | 'FAIL'): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/process/result/${result}`);
  }

  favouriteTask (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/favourite`);
  }

  followTask (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/follow`);
  }

  cancelFavouriteTask (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/favourite`);
  }

  cancelFollowTask (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/follow`);
  }
}
