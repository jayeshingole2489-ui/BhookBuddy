import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import { Cart } from '../types';
import api from '../services/api';

interface CartContextType {
  cart: Cart | null;
  loading: boolean;
  isCartOpen: boolean;
  cartItemCount: number;
  toggleCart: () => void;
  addToCart: (menuItemId: number, restaurantId: number, quantity?: number) => Promise<void>;
  removeFromCart: (itemId: number) => Promise<void>;
  updateQuantity: (itemId: number, quantity: number) => Promise<void>;
  clearCart: () => Promise<void>;
  refreshCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | null>(null);

export const CartProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(false);
  const [isCartOpen, setIsCartOpen] = useState(false);

  const refreshCart = useCallback(async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      setCart(null);
      return;
    }
    try {
      setLoading(true);
      const response = await api.get<Cart>('/cart');
      setCart(response.data);
    } catch {
      setCart(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshCart();
  }, [refreshCart]);

  const addToCart = async (menuItemId: number, restaurantId: number, quantity = 1) => {
    try {
      const response = await api.post<Cart>('/cart/items', { menuItemId, restaurantId, quantity });
      setCart(response.data);
      setIsCartOpen(true);
    } catch (error) {
      throw error;
    }
  };

  const removeFromCart = async (itemId: number) => {
    try {
      const response = await api.delete<Cart>(`/cart/items/${itemId}`);
      setCart(response.data);
    } catch (error) {
      throw error;
    }
  };

  const updateQuantity = async (itemId: number, quantity: number) => {
    try {
      const response = await api.patch<Cart>(`/cart/items/${itemId}`, { quantity });
      setCart(response.data);
    } catch (error) {
      throw error;
    }
  };

  const clearCart = async () => {
    try {
      await api.delete('/cart');
      setCart(null);
    } catch (error) {
      throw error;
    }
  };

  const toggleCart = () => setIsCartOpen((prev) => !prev);

  const value: CartContextType = {
    cart,
    loading,
    isCartOpen,
    cartItemCount: cart?.itemCount ?? 0,
    toggleCart,
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart,
    refreshCart,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

export const useCart = (): CartContextType => {
  const context = useContext(CartContext);
  if (!context) throw new Error('useCart must be used within CartProvider');
  return context;
};
