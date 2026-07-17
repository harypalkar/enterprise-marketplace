import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';

interface FlowState {
  sellerId?: string;
  buyerId?: string;
  categoryId?: string;
  productId?: string;
  otpSessionId?: string;
  otpCode?: string;
  verificationToken?: string;
  mobileUserId?: string;
  accessToken?: string;
  qrSessionId?: string;
  mobileNumber?: string;
}

interface AppContextValue {
  token: string;
  setToken: (token: string) => void;
  flow: FlowState;
  setFlow: (patch: Partial<FlowState>) => void;
}

const AppContext = createContext<AppContextValue | null>(null);

export function AppProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState(() => localStorage.getItem('marketplace.token') ?? '');
  const [flow, setFlowState] = useState<FlowState>({});

  const value = useMemo<AppContextValue>(
    () => ({
      token,
      setToken: (next) => {
        setTokenState(next);
        localStorage.setItem('marketplace.token', next);
      },
      flow,
      setFlow: (patch) => setFlowState((prev) => ({ ...prev, ...patch })),
    }),
    [token, flow],
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useAppContext() {
  const ctx = useContext(AppContext);
  if (!ctx) {
    throw new Error('useAppContext must be used within AppProvider');
  }
  return ctx;
}
