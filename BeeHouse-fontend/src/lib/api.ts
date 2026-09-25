export type Role = "USER" | "ADMINISTRATOR";

export type AuthResponse = {
  id?: number;
  username: string;
  name?: string;
  lastname?: string;
  role: Role;
  jwt: string;
};

export type TransactionType = "ADD" | "REDUCE";

export type UserAccount = {
  id: number;
  username: string;
  name: string;
  lastname: string;
  email: string;
};

export type UserPage = {
  content: UserAccount[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
};

export type Transaction = {
  id: number;
  title: string;
  description?: string;
  amount: number;
  date: string | number[];
  transactionType: TransactionType;
  category?: string;
  user?: UserAccount;
};

export type TransactionPage = {
  content?: Transaction[];
  transactions?: Transaction[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
};

export type Category = {
  id: number;
  categoryName: string;
};

export type Instalment = {
  id?: number;
  month?: number;
  year?: number;
  initialAmount?: number;
  currentBalance?: number;
};

export type InstalmentPeriod = {
  year: number;
  month: number;
};

export type AddTransactionPayload = {
  title: string;
  description?: string;
  amount: number;
  date: string;
  transactionType: TransactionType;
  category?: string;
};

export type RegistrationPayload = {
  name: string;
  lastname: string;
  email: string;
};

type ApiEnvelope<T> = {
  data?: T;
  message?: string;
  error?: string;
  errors?: Record<string, string>;
};

type JwtClaims = {
  sub?: string;
  role?: Role;
  name?: string;
  lastname?: string;
  userId?: number;
};

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "/api";

function decodeJwt(token: string): JwtClaims {
  try {
    const [, payload] = token.split(".");
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(normalized)
        .split("")
        .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join("")
    );
    return JSON.parse(json) as JwtClaims;
  } catch {
    return {};
  }
}

async function request<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const headers = new Headers(options.headers);
  if (!headers.has("Content-Type") && options.body) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const contentType = response.headers.get("content-type") ?? "";
  const payload = contentType.includes("application/json")
    ? ((await response.json()) as ApiEnvelope<T>)
    : ({ data: await response.text() } as ApiEnvelope<T>);

  if (!response.ok) {
    const validation = payload.errors ? Object.values(payload.errors).join(" ") : "";
    throw new Error(validation || payload.message || payload.error || "Richiesta non riuscita");
  }

  return (payload.data ?? payload.message ?? payload) as T;
}

export const api = {
  async login(username: string, password: string) {
    const jwt = await request<string>("/auth/users/login", {
      method: "POST",
      body: JSON.stringify({ username, password })
    });
    const claims = decodeJwt(jwt);
    return {
      id: claims.userId,
      username: claims.sub ?? username,
      name: claims.name,
      lastname: claims.lastname,
      role: claims.role ?? "USER",
      jwt
    } satisfies AuthResponse;
  },

  remindUsername: (email: string) =>
    request<string>(`/auth/users/remind?email=${encodeURIComponent(email)}`),

  resetPassword: (email: string) =>
    request<string>(`/auth/users/change/reset?email=${encodeURIComponent(email)}`, {
      method: "PATCH"
    }),

  getTransactions: (year: number, month: number, token: string, pageNumber = 0, pageSize = 25) =>
    request<TransactionPage>(
      `/user/transactions/${year}/${month}?pageNumber=${pageNumber}&pageSize=${pageSize}&name=date&direction=DESC`,
      {},
      token
    ),

  addTransaction: (payload: AddTransactionPayload, token: string) =>
    request<string>(
      "/user/transactions",
      {
        method: "POST",
        body: JSON.stringify(payload)
      },
      token
    ),

  deleteTransaction: (transactionId: number, token: string) =>
    request<string>(`/user/transactions/${transactionId}`, { method: "DELETE" }, token),

  getCategories: (token: string) => request<Category[]>("/shared/categories", {}, token),

  createCategory: (categoryName: string, token: string) =>
    request<Category>(`/administrator/categories?categoryName=${encodeURIComponent(categoryName)}`, {
      method: "POST"
    }, token),

  updateCategory: (oldCategoryName: string, newCategoryName: string, token: string) =>
    request<Category[]>(
      `/administrator/categories?oldCategoryName=${encodeURIComponent(oldCategoryName)}&newCategoryName=${encodeURIComponent(newCategoryName)}`,
      { method: "PATCH" },
      token
    ),

  deleteCategory: (id: number, token: string) =>
    request<Category[]>(`/administrator/categories/${id}`, { method: "DELETE" }, token),

  getInstalment: (year: number, month: number, token: string) =>
    request<Instalment>(`/shared/instalments/${year}/${month}`, {}, token),

  getInstalmentPeriods: (token: string) =>
    request<InstalmentPeriod[]>("/shared/instalments/periods", {}, token),

  updateInstalmentCost: (newCost: number, token: string) =>
    request<string>(
      `/administrator/instalment-template/cost?newCost=${encodeURIComponent(newCost.toFixed(2))}`,
      { method: "PATCH" },
      token
    ),

  registerUser: (payload: RegistrationPayload, token: string) =>
    request<string>(
      "/administrator/users/registration",
      {
        method: "POST",
        body: JSON.stringify(payload)
      },
      token
    ),

  getUsers: (token: string, pageNumber = 0, pageSize = 10) =>
    request<UserPage>(
      `/administrator/users?pageNumber=${pageNumber}&pageSize=${pageSize}&name=username&direction=ASC`,
      {},
      token
    ),

  disableUser: (userId: number, token: string) =>
    request<string>(`/administrator/users/${userId}`, { method: "DELETE" }, token),

  changePassword: (oldPassword: string, newPassword: string, token: string) =>
    request<string>(
      `/shared/users/change/password?oldPassword=${encodeURIComponent(oldPassword)}&newPassword=${encodeURIComponent(newPassword)}`,
      { method: "PATCH" },
      token
    ),

  changeUsername: (requestNewUsername: string, token: string) =>
    request<string>(
      `/shared/users/change/username?requestNewUsername=${encodeURIComponent(requestNewUsername)}`,
      { method: "PATCH" },
      token
    )
};
