import api from './api';
import { Delivery } from '../types';

export const deliveryService = {
  async getByOrderId(orderId: string): Promise<Delivery> {
    const response = await api.get<Delivery>(`/delivery/order/${orderId}`);
    return response.data;
  },

  async getAllDeliveries(): Promise<Delivery[]> {
    const response = await api.get<Delivery[]>('/admin/deliveries');
    return response.data;
  },

  async updateStatus(deliveryId: string, status: string): Promise<Delivery> {
    const response = await api.patch<Delivery>(`/admin/deliveries/${deliveryId}/status`, { status });
    return response.data;
  },

  async assign(deliveryId: string, data: {
    deliveryPersonName: string;
    deliveryPersonPhone: string;
  }): Promise<Delivery> {
    const response = await api.patch<Delivery>(`/admin/deliveries/${deliveryId}/assign`, data);
    return response.data;
  },
};
