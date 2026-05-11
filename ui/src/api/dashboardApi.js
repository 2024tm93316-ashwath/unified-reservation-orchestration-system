import api from './axiosConfig';

export const getDashboardStats = () => api.get('/dashboard/stats');
export const getResourceUtilization = () => api.get('/dashboard/utilization');
