import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Order, Delivery } from '../types';
import { orderService } from '../services/orderService';
import { deliveryService } from '../services/deliveryService';
import LoadingSpinner from '../components/common/LoadingSpinner';

const ORDER_STEPS = [
  { key: 'CREATED', label: 'Order Placed', icon: '📋', desc: 'Your order has been received' },
  { key: 'CONFIRMED', label: 'Confirmed', icon: '✅', desc: 'Restaurant confirmed your order' },
  { key: 'PREPARING', label: 'Preparing', icon: '👨‍🍳', desc: 'Your food is being prepared' },
  { key: 'READY_FOR_PICKUP', label: 'Ready', icon: '📦', desc: 'Order ready for pickup' },
  { key: 'OUT_FOR_DELIVERY', label: 'On the Way', icon: '🛵', desc: 'Your order is out for delivery' },
  { key: 'DELIVERED', label: 'Delivered', icon: '🎉', desc: 'Order delivered successfully' },
];

const STATUS_ORDER = ['CREATED', 'PAYMENT_PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY', 'DELIVERED'];

const OrderTracking: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [delivery, setDelivery] = useState<Delivery | null>(null);
  const [loading, setLoading] = useState(true);
  const [lastRefreshed, setLastRefreshed] = useState(new Date());

  const fetchData = useCallback(async () => {
    if (!orderId) return;
    try {
      const [orderData] = await Promise.all([
        orderService.getById(orderId),
      ]);
      setOrder(orderData);
      if (orderData.deliveryId) {
        deliveryService.getByOrderId(orderId).then(setDelivery).catch(() => {});
      }
      setLastRefreshed(new Date());
    } catch {
      // silently fail on refresh
    } finally {
      setLoading(false);
    }
  }, [orderId]);

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 30000);
    return () => clearInterval(interval);
  }, [fetchData]);

  const getStepStatus = (stepKey: string): 'completed' | 'active' | 'upcoming' => {
    if (!order) return 'upcoming';
    if (order.status === 'CANCELLED' || order.status === 'PAYMENT_FAILED') return 'upcoming';
    const currentIndex = STATUS_ORDER.indexOf(order.status);
    const stepIndex = STATUS_ORDER.indexOf(stepKey);
    if (stepIndex < currentIndex) return 'completed';
    if (stepIndex === currentIndex) return 'active';
    return 'upcoming';
  };

  if (loading) return <LoadingSpinner fullPage />;
  if (!order) {
    return (
      <div className="empty-state" style={{ paddingTop: '80px' }}>
        <div className="empty-icon">😕</div>
        <h3>Order not found</h3>
        <Link to="/orders" className="btn-primary" style={{ display: 'inline-flex', marginTop: '16px' }}>My Orders</Link>
      </div>
    );
  }

  const isCancelled = order.status === 'CANCELLED' || order.status === 'PAYMENT_FAILED';

  return (
    <div style={{ maxWidth: '680px', margin: '0 auto', padding: '32px 24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '32px', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <h1 style={{ fontSize: '26px', fontWeight: 800, color: '#1A1A2E', marginBottom: '4px' }}>Track Order</h1>
          <p style={{ fontSize: '14px', color: '#999', fontFamily: 'monospace' }}>#{order.orderId}</p>
        </div>
        <div style={{ textAlign: 'right' }}>
          <span className={`status-badge status-${order.status}`}>{order.status.replace(/_/g, ' ')}</span>
          <p style={{ fontSize: '12px', color: '#999', marginTop: '6px' }}>
            Last updated: {lastRefreshed.toLocaleTimeString()}
          </p>
        </div>
      </div>

      {/* Status timeline */}
      {isCancelled ? (
        <div style={{ background: '#FDEDEC', borderRadius: '16px', padding: '32px', textAlign: 'center', marginBottom: '24px' }}>
          <div style={{ fontSize: '48px', marginBottom: '12px' }}>❌</div>
          <h2 style={{ color: '#C0392B', fontWeight: 700, marginBottom: '8px' }}>Order {order.status.replace(/_/g, ' ')}</h2>
          <p style={{ color: '#666', fontSize: '14px' }}>This order has been cancelled or payment failed.</p>
        </div>
      ) : (
        <div style={{ background: 'white', borderRadius: '16px', padding: '28px', boxShadow: '0 2px 16px rgba(0,0,0,0.06)', marginBottom: '24px' }}>
          {ORDER_STEPS.map((step, index) => {
            const status = getStepStatus(step.key);
            return (
              <div key={step.key} style={{ display: 'flex', gap: '16px', marginBottom: index < ORDER_STEPS.length - 1 ? '0' : '0' }}>
                {/* Line + icon */}
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                  <div style={{
                    width: '40px', height: '40px', borderRadius: '50%', flexShrink: 0,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: '18px',
                    background: status === 'completed' ? '#EAFAF1' : status === 'active' ? '#FFF3EE' : '#f5f5f5',
                    border: status === 'active' ? '2px solid #FF6B35' : '2px solid transparent',
                    boxShadow: status === 'active' ? '0 0 0 4px rgba(255,107,53,0.15)' : 'none',
                  }}>
                    {status === 'completed' ? '✓' : step.icon}
                  </div>
                  {index < ORDER_STEPS.length - 1 && (
                    <div style={{
                      width: '2px', height: '32px',
                      background: status === 'completed' ? '#2ecc71' : '#E5E5E5',
                      margin: '4px 0',
                    }} />
                  )}
                </div>
                {/* Content */}
                <div style={{ paddingBottom: index < ORDER_STEPS.length - 1 ? '8px' : '0', flex: 1 }}>
                  <p style={{
                    fontSize: '15px', fontWeight: status === 'active' ? 700 : 600,
                    color: status === 'active' ? '#FF6B35' : status === 'completed' ? '#1E8449' : '#999',
                    marginBottom: '2px',
                  }}>
                    {step.label}
                  </p>
                  <p style={{ fontSize: '13px', color: '#999' }}>{step.desc}</p>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Delivery person */}
      {delivery && delivery.deliveryPersonName && (
        <div style={{ background: 'white', borderRadius: '16px', padding: '20px 24px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)', marginBottom: '20px' }}>
          <h3 style={{ fontSize: '15px', fontWeight: 700, marginBottom: '14px', color: '#1A1A2E' }}>🛵 Delivery Agent</h3>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{
              width: '48px', height: '48px', borderRadius: '50%',
              background: '#FFF3EE', display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: '22px',
            }}>👤</div>
            <div>
              <p style={{ fontWeight: 600, fontSize: '15px', color: '#1A1A2E' }}>{delivery.deliveryPersonName}</p>
              {delivery.deliveryPersonPhone && (
                <p style={{ fontSize: '13px', color: '#666' }}>📞 {delivery.deliveryPersonPhone}</p>
              )}
              {delivery.estimatedMinutes && (
                <p style={{ fontSize: '13px', color: '#FF6B35', fontWeight: 600 }}>⏱ ~{delivery.estimatedMinutes} min away</p>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Order summary */}
      <div style={{ background: 'white', borderRadius: '16px', padding: '20px 24px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)' }}>
        <h3 style={{ fontSize: '15px', fontWeight: 700, marginBottom: '14px', color: '#1A1A2E' }}>📋 Order Items</h3>
        {order.items.map((item, idx) => (
          <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', fontSize: '14px' }}>
            <span style={{ color: '#666' }}>{item.name} × {item.quantity}</span>
            <span>₹{item.subtotal.toFixed(2)}</span>
          </div>
        ))}
        <div style={{ borderTop: '1px solid #E5E5E5', paddingTop: '12px', marginTop: '8px', display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '15px' }}>
          <span>Total</span>
          <span style={{ color: '#FF6B35' }}>₹{order.total.toFixed(2)}</span>
        </div>
        <p style={{ fontSize: '13px', color: '#666', marginTop: '12px' }}>
          📍 {order.deliveryAddress}
        </p>
      </div>

      <div style={{ marginTop: '24px', display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
        <Link to="/orders" style={{ color: '#FF6B35', fontWeight: 600, fontSize: '14px', textDecoration: 'none' }}>← All Orders</Link>
        <span style={{ color: '#ccc' }}>|</span>
        <span style={{ fontSize: '12px', color: '#999' }}>Auto-refreshes every 30 seconds</span>
      </div>
    </div>
  );
};

export default OrderTracking;
