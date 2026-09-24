import api from './api';
import { Order, Address } from '../types';

export const orderService = {
  async create(data: {
    cartId: number;
    deliveryAddress: string;
    notes?: string;
  }): Promise<Order> {
    const response = await api.post<Order>('/orders', data);
    return response.data;
  },

  async getById(orderId: string): Promise<Order> {
    const response = await api.get<Order>(`/orders/${orderId}`);
    return response.data;
  },

  async getMyOrders(): Promise<Order[]> {
    const response = await api.get<Order[]>('/orders');
    return Array.isArray(response.data) ? response.data : [];
  },

  async getAllOrders(status?: string): Promise<Order[]> {
    const params = status ? { status } : {};
    const response = await api.get<Order[]>('/admin/orders', { params });
    return response.data;
  },

  async updateStatus(orderId: string, status: string): Promise<Order> {
    const response = await api.patch<Order>(`/admin/orders/${orderId}/status`, { status });
    return response.data;
  },

  async getUserAddresses(userId: number): Promise<Address[]> {
    const response = await api.get<Address[]>(`/users/${userId}/addresses`);
    return response.data;
  },

  async addAddress(userId: number, data: Omit<Address, 'id'>): Promise<Address> {
    const response = await api.post<Address>(`/users/${userId}/addresses`, data);
    return response.data;
  },

  async deleteAddress(userId: number, addressId: number): Promise<void> {
    await api.delete(`/users/${userId}/addresses/${addressId}`);
  },
};
