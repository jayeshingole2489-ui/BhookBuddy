import api from './api';
import { Restaurant, MenuItem, Category } from '../types';

export const restaurantService = {
  async getAll(): Promise<Restaurant[]> {
    const response = await api.get<{ restaurants: Restaurant[]; total: number }>('/restaurants');
    return (response.data as any)?.restaurants ?? (Array.isArray(response.data) ? response.data : []);
  },

  async getActive(): Promise<Restaurant[]> {
    const response = await api.get<Restaurant[]>('/restaurants/active');
    return Array.isArray(response.data) ? response.data : [];
  },

  async getById(id: number): Promise<Restaurant> {
    const response = await api.get<Restaurant>(`/restaurants/${id}`);
    return response.data;
  },

  async search(query: string, cuisine?: string): Promise<Restaurant[]> {
    const params: Record<string, string> = { q: query };
    if (cuisine) params.cuisine = cuisine;
    const response = await api.get<{ restaurants: Restaurant[]; total: number }>('/restaurants/search', { params });
    return (response.data as any)?.restaurants ?? (Array.isArray(response.data) ? response.data : []);
  },

  async getMenuItems(restaurantId: number): Promise<MenuItem[]> {
    const response = await api.get<MenuItem[]>(`/restaurants/${restaurantId}/menu`);
    return response.data;
  },

  async getCategories(): Promise<Category[]> {
    const response = await api.get<Category[]>('/categories');
    return response.data;
  },

  async create(data: Partial<Restaurant>): Promise<Restaurant> {
    const response = await api.post<Restaurant>('/admin/restaurants', data);
    return response.data;
  },

  async update(id: number, data: Partial<Restaurant>): Promise<Restaurant> {
    const response = await api.put<Restaurant>(`/admin/restaurants/${id}`, data);
    return response.data;
  },

  async toggleActive(id: number): Promise<Restaurant> {
    const response = await api.patch<Restaurant>(`/admin/restaurants/${id}/toggle`);
    return response.data;
  },

  async addMenuItem(restaurantId: number, data: Partial<MenuItem>): Promise<MenuItem> {
    const response = await api.post<MenuItem>(`/admin/restaurants/${restaurantId}/menu`, data);
    return response.data;
  },

  async updateMenuItem(restaurantId: number, itemId: number, data: Partial<MenuItem>): Promise<MenuItem> {
    const response = await api.put<MenuItem>(`/admin/restaurants/${restaurantId}/menu/${itemId}`, data);
    return response.data;
  },

  async toggleMenuItemAvailability(restaurantId: number, itemId: number): Promise<MenuItem> {
    const response = await api.patch<MenuItem>(`/admin/restaurants/${restaurantId}/menu/${itemId}/toggle`);
    return response.data;
  },
};
