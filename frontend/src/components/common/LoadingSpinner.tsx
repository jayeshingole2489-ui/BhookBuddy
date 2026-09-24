import React from 'react';

interface LoadingSpinnerProps {
  size?: number;
  color?: string;
  fullPage?: boolean;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 40,
  color = '#FF6B35',
  fullPage = false,
}) => {
  const spinner = (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: fullPage ? 0 : '40px' }}>
      <div
        style={{
          width: size,
          height: size,
          border: `3px solid #f3f3f3`,
          borderTop: `3px solid ${color}`,
          borderRadius: '50%',
          animation: 'spin 0.8s linear infinite',
        }}
      />
      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );

  if (fullPage) {
    return (
      <div style={{
        position: 'fixed',
        inset: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'rgba(255,255,255,0.8)',
        zIndex: 500,
      }}>
        {spinner}
      </div>
    );
  }

  return spinner;
};

export default LoadingSpinner;
