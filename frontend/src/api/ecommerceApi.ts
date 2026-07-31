import axios from 'axios';

export type Product = {
    id: number;
    name: string;
    description?: string;
    price: number;
    stock: number;
    imageUrl?: string;
};

export type User = {
    id: number;
    name: string;
    email: string;
};

export type CartItem = {
    id: number;
    productId: number;
    productName: string;
    imageUrl?: string;
    quantity: number;
    price: number;
};

export type Cart = {
    id: number;
    userId: number;
    items: CartItem[];
    total: number;
};

export type OrderItem = {
    id: number;
    orderId: number;
    productId: number;
    productName: string;
    imageUrl?: string;
    quantity: number;
    price: number;
};

export type Order = {
    id: number;
    userId: number;
    createdAt: string;
    status: string;
    items: OrderItem[];
    total: number;
};

export type AuthenticatedUser = {
    id: number;
    username: string;
    name?: string;
};

export type RegisterUserRequest = {
    name: string;
    email: string;
    password: string;
};

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL ?? '/api',
});

const authStorageKey = 'ecommerceShopAuth';
const userStorageKey = 'ecommerceShopUser';

api.interceptors.request.use((config) => {
    const authHeader = sessionStorage.getItem(authStorageKey);

    if (authHeader) {
        config.headers.Authorization = authHeader;
    }

    return config;
});

export function hasStoredAuth() {
    return Boolean(sessionStorage.getItem(authStorageKey));
}

export function getStoredUser() {
    const storedUser = sessionStorage.getItem(userStorageKey);
    return storedUser ? JSON.parse(storedUser) as AuthenticatedUser : null;
}

export function clearAuth() {
    sessionStorage.removeItem(authStorageKey);
    sessionStorage.removeItem(userStorageKey);
}

export async function authenticate(username: string, password: string) {
    const authHeader = `Basic ${btoa(`${username}:${password}`)}`;
    sessionStorage.setItem(authStorageKey, authHeader);

    try {
        const response = await api.get<AuthenticatedUser>('/auth/me');
        sessionStorage.setItem(userStorageKey, JSON.stringify(response.data));
        return response.data;
    } catch (error) {
        clearAuth();
        throw error;
    }
}

export async function registerUser(request: RegisterUserRequest) {
    const response = await api.post<User>('/public/users', request);
    return response.data;
}

export function getCurrentUserId() {
    const userId = Number(getStoredUser()?.id);

    if (!Number.isInteger(userId) || userId < 1) {
        throw new Error('Logged in user was not found in the database');
    }

    return userId;
}

export function getApiErrorMessage(error: unknown, fallback: string) {
    if (axios.isAxiosError(error)) {
        const responseMessage = error.response?.data?.message;

        if (typeof responseMessage === 'string' && responseMessage.length > 0) {
            return responseMessage;
        }
    }

    return error instanceof Error ? error.message : fallback;
}

export async function getProducts() {
    const response = await api.get<Product[]>('/public/products');
    return response.data;
}

export async function getCart(userId: number) {
    const response = await api.get<Cart>(`/users/${userId}/cart`);
    return response.data;
}

export async function addCartItem(userId: number, productId: number, quantity: number) {
    const response = await api.post<Cart>(`/users/${userId}/cart/items`, {
        productId,
        quantity,
    });
    return response.data;
}

export async function removeCartItem(userId: number, productId: number) {
    const response = await api.delete<Cart>(`/users/${userId}/cart/items/${productId}`);
    return response.data;
}

export async function checkoutCart(userId: number) {
    const response = await api.post<Order>(`/users/${userId}/checkout`);
    return response.data;
}

export async function getOrders(userId: number) {
    const response = await api.get<Order[]>(`/users/${userId}/orders`);
    return response.data;
}
