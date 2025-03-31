export type AppUser = {
    id: string,
    username: string,
    avatarUrl: string,
    todoIds: string[],
    role: "ADMIN" | "USER"
};

export type Address = {
    country?: string | null,  // Nullable, max 100 chars
    city: string,  // Not blank, max 100 chars
    street: string,  // Not blank, max 200 chars
    postalCode?: string | null,  // Nullable, 3-10 alphanumeric chars with hyphens/spaces
};

export type CustomerStatus = "ACTIVE" |
    "EXPIRING" | "SUSPENDED" |
    "EXPIRED" | "PENDING_ACTIVATION";

export type CustomerDTO = {
    username: string,
    fullName: string,
    phone: string,
    address: Address,
    status: CustomerStatus,
    notes: string
};

export type Customer = {
    id: string,
    username: string,
    fullName: string,
    phone: string,
    address: Address,
    registrationDate: string,
    status: CustomerStatus,
    notes: string
};