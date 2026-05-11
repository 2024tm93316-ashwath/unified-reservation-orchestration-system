import api from './axiosConfig';

export const checkAvailability = (data) =>
  api.post('/reservations/availability', data);

export const createReservation = (data) =>
  api.post('/reservations', data);

export const confirmReservation = (id) =>
  api.post(`/reservations/${id}/confirm`);

export const cancelReservation = (id) =>
  api.post(`/reservations/${id}/cancel`);

export const getReservationById = (id) =>
  api.get(`/reservations/${id}`);

export const getReservationsByUser = (userId) =>
  api.get('/reservations', { params: { userId } });

export const getAllReservations = () =>
  api.get('/reservations');
