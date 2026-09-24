import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Order, Payment } from '../types';
import { orderService } from '../services/orderService';
import { paymentService } from '../services/paymentService';
import { useCart } from '../contexts/CartContext';
import LoadingSpinner from '../components/common/LoadingSpinner';

const OrderConfirmation: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();
  const { refreshCart } = useCart();

  const [order, setOrder] = useState<Order | null>(null);
  const [payment, setPayment] = useState<Payment | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!orderId) return;
    Promise.all([
      orderService.getById(orderId),
      paymentService.getByOrderId(orderId),
    ])
      .then(([o, p]) => {
        setOrder(o);
        setPayment(p);
        if (p.status === 'SUCCESS') {
          refreshCart();
        }
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [orderId]);

  if (loading) return <LoadingSpinner fullPage />;
  if (!order || !payment) {
    return (
      <div className="empty-state" style={{ paddingTop: '80px' }}>
        <div className="empty-icon">😕</div>
        <h3>Order not found</h3>
        <Link to="/" className="btn-primary" style={{ marginTop: '16px', display: 'inline-flex' }}>Go Home</Link>
      </div>
    );
  }

  const isSuccess = payment.status === 'SUCCESS';

  return (
    <div style={{ maxWidth: '580px', margin: '0 auto', padding: '48px 24px', textAlign: 'center' }}>
      {/* Status icon */}
      <div style={{
        width: '96px', height: '96px', borderRadius: '50%',
        background: isSuccess ? '#EAFAF1' : '#FDEDEC',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        margin: '0 auto 24px',
        fontSize: '48px',
      }}>
        {isSuccess ? '✅' : '❌'}
      </div>

      <h1 style={{
        fontSize: '28px', fontWeight: 800, marginBottom: '10px',
        color: isSuccess ? '#1E8449' : '#C0392B',
      }}>
        {isSuccess ? 'Order Confirmed!' : 'Payment Failed'}
      </h1>
      <p style={{ fontSize: '16px', color: '#666', marginBottom: '32px', lineHeight: 1.5 }}>
        {isSuccess
          ? `Your order from ${order.restaurantName} has been placed successfully. Get ready for some delicious food!`
          : `Your payment could not be processed. Please try again.`}
      </p>

      {/* Order card */}
      <div style={{
        background: 'white', borderRadius: '16px', padding: '24px',
        boxShadow: '0 2px 16px rgba(0,0,0,0.08)', textAlign: 'left', marginBottom: '28px',
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px', paddingBottom: '16px', borderBottom: '1px solid #E5E5E5' }}>
          <div>
            <p style={{ fontSize: '12px', color: '#999', marginBottom: '4px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Order ID</p>
            <p style={{ fontSize: '14px', fontWeight: 700, color: '#1A1A2E', fontFamily: 'monospace' }}>#{order.orderId}</p>
          </div>
          <div style={{ textAlign: 'right' }}>
            <p style={{ fontSize: '12px', color: '#999', marginBottom: '4px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Payment</p>
            <span className={`status-badge status-${payment.status}`}>{payment.status}</span>
          </div>
        </div>

        <div style={{ marginBottom: '16px' }}>
          <p style={{ fontSize: '12px', color: '#999', marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Items</p>
          {order.items.map((item, idx) => (
            <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px', fontSize: '14px' }}>
              <span style={{ color: '#666' }}>{item.name} × {item.quantity}</span>
              <span>₹{item.subtotal.toFixed(2)}</span>
            </div>
          ))}
        </div>

        <div style={{ borderTop: '1px solid #E5E5E5', paddingTop: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px', fontSize: '14px' }}>
            <span style={{ color: '#666' }}>Subtotal</span>
            <span>₹{order.subtotal.toFixed(2)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px', fontSize: '14px' }}>
            <span style={{ color: '#666' }}>Delivery fee</span>
            <span>₹{order.deliveryFee.toFixed(2)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '17px' }}>
            <span>Total Paid</span>
            <span style={{ color: '#FF6B35' }}>₹{order.total.toFixed(2)}</span>
          </div>
        </div>

        {isSuccess && (
          <div style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px solid #E5E5E5', fontSize: '13px', color: '#666', display: 'flex', alignItems: 'center', gap: '8px' }}>
            📍 {order.deliveryAddress}
          </div>
        )}
      </div>

      {/* Actions */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {isSuccess ? (
          <>
            <button
              onClick={() => navigate(`/orders/${orderId}/track`)}
              className="btn-primary"
              style={{ width: '100%', justifyContent: 'center', padding: '14px', fontSize: '15px' }}
            >
              📍 Track Your Order
            </button>
            <Link
              to="/restaurants"
              style={{
                display: 'block', textAlign: 'center', padding: '12px',
                color: '#FF6B35', fontWeight: 600, fontSize: '15px',
                textDecoration: 'none',
              }}
            >
              Continue Shopping →
            </Link>
          </>
        ) : (
          <>
            <button
              onClick={() => navigate(`/payment/${orderId}`)}
              className="btn-primary"
              style={{ width: '100%', justifyContent: 'center', padding: '14px', fontSize: '15px' }}
            >
              Retry Payment
            </button>
            <Link to="/orders" style={{ display: 'block', textAlign: 'center', padding: '12px', color: '#FF6B35', fontWeight: 600, fontSize: '15px', textDecoration: 'none' }}>
              View My Orders
            </Link>
          </>
        )}
      </div>
    </div>
  );
};

export default OrderConfirmation;
