import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Order } from '../types';
import { orderService } from '../services/orderService';
import { paymentService } from '../services/paymentService';
import { useToast } from '../App';
import LoadingSpinner from '../components/common/LoadingSpinner';

const PAYMENT_METHODS = [
  { id: 'CARD', label: 'Credit / Debit Card', icon: '💳', desc: 'Visa, Mastercard, Rupay' },
  { id: 'UPI', label: 'UPI', icon: '📱', desc: 'Google Pay, PhonePe, Paytm' },
  { id: 'NET_BANKING', label: 'Net Banking', icon: '🏦', desc: 'All major banks' },
  { id: 'WALLET', label: 'Wallet', icon: '👜', desc: 'Mobikwik, Amazon Pay' },
  { id: 'COD', label: 'Cash on Delivery', icon: '💵', desc: 'Pay when delivered' },
];

const Payment: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedMethod, setSelectedMethod] = useState('CARD');
  const [simulateFailure, setSimulateFailure] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [paymentError, setPaymentError] = useState('');

  useEffect(() => {
    if (!orderId) return;
    orderService.getById(orderId)
      .then(setOrder)
      .catch(() => showToast('Failed to load order', 'error'))
      .finally(() => setLoading(false));
  }, [orderId]);

  const handlePay = async () => {
    if (!orderId || !order) return;
    setPaymentError('');
    try {
      setProcessing(true);
      const payment = await paymentService.process({
        orderId,
        amount: order.total,
        paymentMethod: selectedMethod,
        simulateFailure,
      });
      if (payment.status === 'SUCCESS') {
        showToast('Payment successful!', 'success');
        navigate(`/orders/${orderId}/confirmation`);
      } else {
        setPaymentError(payment.failureReason ?? 'Payment failed. Please try again.');
      }
    } catch (error: unknown) {
      const msg = error instanceof Error ? error.message : 'Payment failed';
      setPaymentError(msg);
    } finally {
      setProcessing(false);
    }
  };

  if (loading) return <LoadingSpinner fullPage />;
  if (!order) return (
    <div className="empty-state" style={{ paddingTop: '80px' }}>
      <div className="empty-icon">😕</div>
      <h3>Order not found</h3>
    </div>
  );

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', padding: '40px 24px' }}>
      <h1 style={{ fontSize: '28px', fontWeight: 800, color: '#1A1A2E', marginBottom: '8px' }}>Payment</h1>
      <p style={{ fontSize: '15px', color: '#666', marginBottom: '32px' }}>Order #{order.orderId}</p>

      {/* Amount */}
      <div style={{
        background: 'linear-gradient(135deg, #1A1A2E, #FF6B35)',
        borderRadius: '16px',
        padding: '24px',
        color: 'white',
        marginBottom: '24px',
        textAlign: 'center',
      }}>
        <p style={{ fontSize: '14px', opacity: 0.8, marginBottom: '8px' }}>Amount to pay</p>
        <p style={{ fontSize: '40px', fontWeight: 800 }}>₹{order.total.toFixed(2)}</p>
        <p style={{ fontSize: '13px', opacity: 0.7, marginTop: '4px' }}>{order.restaurantName}</p>
      </div>

      {/* Payment method */}
      <div style={{ background: 'white', borderRadius: '16px', padding: '24px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)', marginBottom: '24px' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '16px', color: '#1A1A2E' }}>Select Payment Method</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {PAYMENT_METHODS.map((method) => (
            <label
              key={method.id}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '14px',
                padding: '14px 16px',
                border: '2px solid',
                borderColor: selectedMethod === method.id ? '#FF6B35' : '#E5E5E5',
                borderRadius: '10px',
                cursor: 'pointer',
                background: selectedMethod === method.id ? '#FFF3EE' : 'white',
                transition: 'all 0.15s',
              }}
              onClick={() => setSelectedMethod(method.id)}
            >
              <input
                type="radio"
                checked={selectedMethod === method.id}
                onChange={() => setSelectedMethod(method.id)}
                style={{ accentColor: '#FF6B35' }}
              />
              <span style={{ fontSize: '24px' }}>{method.icon}</span>
              <div>
                <div style={{ fontWeight: 600, fontSize: '14px', color: '#1A1A2E' }}>{method.label}</div>
                <div style={{ fontSize: '12px', color: '#999' }}>{method.desc}</div>
              </div>
            </label>
          ))}
        </div>
      </div>

      {/* Testing toggle */}
      <div style={{
        background: '#FEF9E7',
        border: '1px solid #F9E79F',
        borderRadius: '12px',
        padding: '16px 20px',
        marginBottom: '24px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}>
        <div>
          <p style={{ fontSize: '14px', fontWeight: 600, color: '#1A1A2E', marginBottom: '2px' }}>🧪 Simulate Payment Failure</p>
          <p style={{ fontSize: '12px', color: '#888' }}>For testing — toggle to force a failed payment</p>
        </div>
        <label className="toggle-switch">
          <input type="checkbox" checked={simulateFailure} onChange={(e) => setSimulateFailure(e.target.checked)} />
          <span className="toggle-slider" />
        </label>
      </div>

      {paymentError && (
        <div style={{
          background: '#FDEDEC',
          border: '1px solid #F1948A',
          borderRadius: '10px',
          padding: '14px 18px',
          marginBottom: '20px',
          color: '#c0392b',
          fontSize: '14px',
        }}>
          ❌ {paymentError}
        </div>
      )}

      <button
        onClick={handlePay}
        className="btn-primary"
        disabled={processing}
        style={{ width: '100%', justifyContent: 'center', padding: '16px', fontSize: '16px' }}
      >
        {processing ? (
          <span style={{ display: 'flex', alignItems: 'center', gap: '10px', justifyContent: 'center' }}>
            <span style={{ width: '18px', height: '18px', border: '2px solid rgba(255,255,255,0.3)', borderTop: '2px solid white', borderRadius: '50%', animation: 'spin 0.8s linear infinite', display: 'inline-block' }} />
            Processing...
          </span>
        ) : `Pay ₹${order.total.toFixed(2)}`}
      </button>
      <style>{`@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }`}</style>
    </div>
  );
};

export default Payment;
