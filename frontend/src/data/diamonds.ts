export interface DiamondItem {
  id: string;
  title: string;
  shape: string;
  carat: number;
  color: string;
  clarity: string;
  cut: string;
  polish: string;
  price: number;
  certified: string;
  image: string;
  available: boolean;
}

export const DIAMOND_SHAPES = [
  'Round',
  'Princess',
  'Emerald',
  'Oval',
  'Cushion',
  'Pear',
  'Marquise',
  'Radiant',
] as const;

export const CLARITY_GRADES = ['FL', 'IF', 'VVS1', 'VVS2', 'VS1', 'VS2', 'SI1'] as const;

export const DIAMONDS: DiamondItem[] = [
  {
    id: 'd-124-round',
    title: '1.24 CT Round Brilliant',
    shape: 'Round',
    carat: 1.24,
    color: 'D',
    clarity: 'VVS1',
    cut: 'Ideal',
    polish: 'Excellent',
    price: 8420,
    certified: 'GIA',
    image: 'https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?auto=format&fit=crop&w=900&q=80',
    available: true,
  },
  {
    id: 'd-210-emerald',
    title: '2.10 CT Emerald Cut',
    shape: 'Emerald',
    carat: 2.1,
    color: 'E',
    clarity: 'VVS2',
    cut: 'Excellent',
    polish: 'Excellent',
    price: 18500,
    certified: 'GIA',
    image: 'https://images.unsplash.com/photo-1605100804763-247f67b3557e?auto=format&fit=crop&w=900&q=80',
    available: true,
  },
  {
    id: 'd-123-round',
    title: '1.23 CT Round Brilliant',
    shape: 'Round',
    carat: 1.23,
    color: 'F',
    clarity: 'VS1',
    cut: 'Excellent',
    polish: 'Very Good',
    price: 2840,
    certified: 'IGI',
    image: 'https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?auto=format&fit=crop&w=900&q=80',
    available: true,
  },
  {
    id: 'd-150-oval',
    title: '1.50 CT Oval Brilliant',
    shape: 'Oval',
    carat: 1.5,
    color: 'D',
    clarity: 'IF',
    cut: 'Ideal',
    polish: 'Excellent',
    price: 11200,
    certified: 'GIA',
    image: 'https://images.unsplash.com/photo-1617038260897-da713e940c4a?auto=format&fit=crop&w=900&q=80',
    available: true,
  },
  {
    id: 'd-180-cushion',
    title: '1.80 CT Cushion',
    shape: 'Cushion',
    carat: 1.8,
    color: 'E',
    clarity: 'VVS1',
    cut: 'Very Good',
    polish: 'Excellent',
    price: 9800,
    certified: 'HRD',
    image: 'https://images.unsplash.com/photo-1602173574767-37ac01994b2a?auto=format&fit=crop&w=900&q=80',
    available: true,
  },
  {
    id: 'd-095-pear',
    title: '0.95 CT Pear Shape',
    shape: 'Pear',
    carat: 0.95,
    color: 'G',
    clarity: 'VS2',
    cut: 'Excellent',
    polish: 'Excellent',
    price: 2100,
    certified: 'IGI',
    image: 'https://images.unsplash.com/photo-1573408301185-9146fe634ad0?auto=format&fit=crop&w=900&q=80',
    available: true,
  },
];

export function formatUsd(value: number) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(value);
}
