export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role: 'ROLE_USER' | 'ROLE_ADMIN';
  active: boolean;
  createdAt: string;
}

export interface Address {
  id: number;
  label: string;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  isDefault: boolean;
}

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

export interface Restaurant {
  id: number;
  name: string;
  description?: string;
  cuisineType: string;
  address: string;
  city: string;
  phone?: string;
  rating: number;
  active: boolean;
  deliveryFee: number;
  minOrderAmount: number;
  estimatedDeliveryMinutes: number;
  imageUrl?: string;
  menuItemCount?: number;
}

export interface MenuItem {
  id: number;
  restaurantId: number;
  restaurantName: string;
  name: string;
  description?: string;
  price: number;
  category: string;
  available: boolean;
  vegetarian: boolean;
  imageUrl?: string;
}

export interface CartItem {
  id: number;
  menuItemId: number;
  name: string;
  price: number;
  quantity: number;
  subtotal: number;
  vegetarian: boolean;
  imageUrl?: string;
}

export interface Cart {
  id: number;
  restaurantId: number;
  restaurantName: string;
  items: CartItem[];
  subtotal: number;
  deliveryFee: number;
  total: number;
  itemCount: number;
}

export interface Order {
  id: number;
  orderId: string;
  restaurantId: number;
  restaurantName: string;
  items: OrderItem[];
  deliveryAddress: string;
  subtotal: number;
  deliveryFee: number;
  total: number;
  status: OrderStatus;
  paymentId?: string;
  deliveryId?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  menuItemId: number;
  name: string;
  price: number;
  quantity: number;
  subtotal: number;
}

export type OrderStatus =
  | 'CREATED' | 'PAYMENT_PENDING' | 'CONFIRMED' | 'PREPARING'
  | 'READY_FOR_PICKUP' | 'OUT_FOR_DELIVERY' | 'DELIVERED'
  | 'CANCELLED' | 'PAYMENT_FAILED';

export interface Payment {
  paymentId: string;
  orderId: string;
  amount: number;
  status: 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
  paymentMethod: string;
  transactionId?: string;
  failureReason?: string;
  createdAt: string;
}

export interface Delivery {
  deliveryId: string;
  orderId: string;
  restaurantAddress: string;
  deliveryAddress: string;
  status: DeliveryStatus;
  deliveryPersonName?: string;
  deliveryPersonPhone?: string;
  estimatedMinutes?: number;
  assignedAt?: string;
  pickedUpAt?: string;
  deliveredAt?: string;
}

export type DeliveryStatus =
  | 'WAITING_FOR_ASSIGNMENT' | 'ASSIGNED' | 'PICKED_UP'
  | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED';

export interface Notification {
  id: number;
  orderId?: string;
  type: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
}

export interface Category {
  id: number;
  name: string;
  icon: string;
}

export interface ToastMessage {
  id: string;
  type: 'success' | 'error' | 'info';
  message: string;
}
