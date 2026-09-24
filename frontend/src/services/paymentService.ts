import api from './api';
import { Payment } from '../types';

export const paymentService = {
  async process(data: {
    orderId: string;
    amount: number;
    paymentMethod: string;
    simulateFailure?: boolean;
  }): Promise<Payment> {
    const response = await api.post<Payment>('/payments', data);
    return response.data;
  },

  async getByOrderId(orderId: string): Promise<Payment> {
    const response = await api.get<Payment>(`/payments/order/${orderId}`);
    return response.data;
  },

  async getAllPayments(): Promise<Payment[]> {
    const response = await api.get<Payment[]>('/admin/payments');
    return response.data;
  },

  async refund(paymentId: string): Promise<Payment> {
    const response = await api.post<Payment>(`/admin/payments/${paymentId}/refund`);
    return response.data;
  },
};
