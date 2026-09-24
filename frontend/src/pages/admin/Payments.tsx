import React, { useState, useEffect } from 'react';
import AdminSidebar from '../../components/admin/AdminSidebar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Payment } from '../../types';
import { paymentService } from '../../services/paymentService';
import { useToast } from '../../App';

const AdminPayments: React.FC = () => {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [refundingId, setRefundingId] = useState<string | null>(null);
  const { showToast } = useToast();

  useEffect(() => {
    paymentService.getAllPayments()
      .then((data) => setPayments(data.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())))
      .catch(() => showToast('Failed to load payments', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const handleRefund = async (paymentId: string) => {
    if (!window.confirm('Are you sure you want to refund this payment?')) return;
    try {
      setRefundingId(paymentId);
      const updated = await paymentService.refund(paymentId);
      setPayments((prev) => prev.map((p) => p.paymentId === paymentId ? updated : p));
      showToast('Payment refunded!', 'success');
    } catch {
      showToast('Failed to refund payment', 'error');
    } finally {
      setRefundingId(null);
    }
  };

  const totalRevenue = payments.filter((p) => p.status === 'SUCCESS').reduce((sum, p) => sum + p.amount, 0);

  return (
    <div style={{ display: 'flex', gap: '28px', maxWidth: '1200px', margin: '0 auto', padding: '32px 24px', alignItems: 'flex-start' }}>
      <AdminSidebar />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ marginBottom: '28px' }}>
          <h1 style={{ fontSize: '24px', fontWeight: 800, color: '#1A1A2E', marginBottom: '4px' }}>Payments</h1>
          <p style={{ fontSize: '14px', color: '#666' }}>Total Revenue: <strong style={{ color: '#FF6B35' }}>₹{totalRevenue.toLocaleString('en-IN')}</strong></p>
        </div>

        {loading ? <LoadingSpinner /> : (
          <div style={{ background: 'white', borderRadius: '16px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)', overflow: 'hidden' }}>
            <div style={{ overflowX: 'auto' }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Payment ID</th>
                    <th>Order ID</th>
                    <th>Amount</th>
                    <th>Method</th>
                    <th>Status</th>
                    <th>Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {payments.length === 0 ? (
                    <tr><td colSpan={7} style={{ textAlign: 'center', padding: '40px', color: '#999' }}>No payments found</td></tr>
                  ) : payments.map((payment) => (
                    <tr key={payment.paymentId}>
                      <td style={{ fontFamily: 'monospace', fontSize: '12px' }}>{payment.paymentId.slice(0, 10)}...</td>
                      <td style={{ fontFamily: 'monospace', fontSize: '12px' }}>{payment.orderId.slice(0, 10)}...</td>
                      <td style={{ fontWeight: 600 }}>₹{payment.amount.toFixed(2)}</td>
                      <td style={{ color: '#666' }}>{payment.paymentMethod.replace('_', ' ')}</td>
                      <td><span className={`status-badge status-${payment.status}`}>{payment.status}</span></td>
                      <td style={{ color: '#999', fontSize: '13px' }}>{new Date(payment.createdAt).toLocaleDateString('en-IN')}</td>
                      <td>
                        {payment.status === 'SUCCESS' && (
                          <button
                            onClick={() => handleRefund(payment.paymentId)}
                            disabled={refundingId === payment.paymentId}
                            style={{
                              padding: '6px 14px', border: 'none', borderRadius: '6px',
                              background: '#EBF5FB', color: '#2980B9',
                              fontSize: '12px', fontWeight: 600, cursor: 'pointer',
                            }}
                          >
                            {refundingId === payment.paymentId ? '...' : 'Refund'}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminPayments;
