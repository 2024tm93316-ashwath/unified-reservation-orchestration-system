import api from './axiosConfig';

// Resource Types
export const getResourceTypes = () => api.get('/resource-types');
export const createResourceType = (data) => api.post('/resource-types', data);
export const updateResourceType = (id, data) => api.put(`/resource-types/${id}`, data);
export const deleteResourceType = (id) => api.delete(`/resource-types/${id}`);

// Resources
export const getResources = () => api.get('/resources');
export const getResourceById = (id) => api.get(`/resources/${id}`);
export const createResource = (data) => api.post('/resources', data);
export const updateResource = (id, data) => api.put(`/resources/${id}`, data);
export const deleteResource = (id) => api.delete(`/resources/${id}`);

// Time Slots
export const getTimeSlotsByResource = (resourceId) =>
  api.get(`/time-slots/resource/${resourceId}`);
export const createTimeSlot = (data) => api.post('/time-slots', data);

// Seat Maps
export const getSeatsByResource = (resourceId) =>
  api.get(`/seat-maps/resource/${resourceId}`);
export const getAvailableSeatsByResource = (resourceId) =>
  api.get(`/seat-maps/resource/${resourceId}/available`);
export const createSeat = (data) => api.post('/seat-maps', data);

// Quota Definitions
export const getQuotasByResource = (resourceId) =>
  api.get(`/quotas/resource/${resourceId}`);
export const createQuota = (data) => api.post('/quotas', data);
