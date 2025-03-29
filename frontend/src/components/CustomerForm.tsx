import { Input } from "../shared/Input";
import { Textarea } from "../shared/Textarea";
import { Select } from "../shared/Select";
import {CustomerDTO, CustomerStatus} from "../types";
import {ChangeEvent, useState, useEffect, FormEvent} from "react";
import { Button } from "../shared/Button";

interface CustomerFormProps {
    initialData?: Partial<CustomerDTO>;
    onSubmit: (customer: CustomerDTO) => Promise<void>;
    onCancel?: () => void;
    isSubmitting: boolean;
    submitButtonText: string;
    resetButtonText: string;
    mode: 'add' | 'edit';
    loading?: boolean;
    submissionError?: Partial<Record<keyof Omit<CustomerDTO, 'address'>, string>> & {
        address?: Partial<Record<keyof CustomerDTO['address'], string>>;
    };
}

const defaultCustomer: CustomerDTO = {
    username: "",
    fullName: "",
    phone: "",
    address: {
        country: "",
        city: "",
        street: "",
        postalCode: "",
    },
    status: "PENDING_ACTIVATION",
    notes: ""
};

export const CustomerForm = ({
                                 initialData,
                                 onSubmit,
                                 onCancel,
                                 isSubmitting,
                                 submitButtonText,
                                 resetButtonText,
                                 mode,
                                 loading = false,
                                 submissionError,
                             }: CustomerFormProps) => {

    const [customer, setCustomer] = useState<CustomerDTO>({
        ...defaultCustomer,
        ...initialData,
        address: {
            ...defaultCustomer.address,
            ...initialData?.address
        }
    });

    const [errors, setErrors] = useState<
        NonNullable<CustomerFormProps['submissionError']>
    >({});

    useEffect(() => {
        if (initialData) {
            setCustomer(() => ({
                ...defaultCustomer,
                ...initialData,
                address: {
                    ...defaultCustomer.address,
                    ...initialData.address
                }
            }));
        }
    }, [initialData]);

    useEffect(() => {
        if (submissionError) {
            setErrors(prev => ({ ...prev, ...submissionError }));
        }
    }, [submissionError]);

    const handleOnChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setCustomer(prev => ({
            ...prev,
            [name]: value
        }));
        if (errors?.[name as keyof CustomerDTO]) {
            setErrors(prev => ({ ...prev, [name]: undefined }));
        }
    };

    const handleAddressChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setCustomer(prev => ({
            ...prev,
            address: {
                ...prev.address,
                [name]: value
            }
        }));
        if (errors?.address?.[name as keyof CustomerDTO['address']]) {
            setErrors(prev => ({
                ...prev,
                address: {
                    ...prev.address,
                    [name]: undefined
                }
            }));
        }
    };

    const handleStatusChange = (value: string) => {
        setCustomer(prev => ({
            ...prev,
            status: value as CustomerStatus
        }));
        if (errors?.status) {
            setErrors(prev => ({ ...prev, status: undefined }));
        }
    };

    const validateForm = (): boolean => {
        const newErrors: CustomerFormProps['submissionError'] = {};

        if (!customer.username.trim()) newErrors.username = "Username is required";
        if (!customer.fullName.trim()) newErrors.fullName = "Full name is required";

        // Address validation
        const addressErrors: Partial<Record<keyof CustomerDTO['address'], string>> = {};
        if (!customer.address.street.trim()) addressErrors.street = "Street is required";
        if (!customer.address.city.trim()) addressErrors.city = "City is required";

        if (Object.keys(addressErrors).length > 0) {
            newErrors.address = addressErrors;
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;
        await onSubmit(customer);
    };

    const handleReset = () => {
        if (onCancel) {
            onCancel();
        } else {
            setCustomer(defaultCustomer);
        }
    };

    if (loading) return <div className="text-center py-4">Loading...</div>;

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                    name="username"
                    label="Username"
                    value={customer.username}
                    onChange={handleOnChange}
                    error={errors?.username}
                    required
                    disabled={mode === 'edit'}
                    placeholder="Enter unique username (e.g., john_doe123)"
                />
                <Input
                    name="fullName"
                    label="Full Name"
                    value={customer.fullName}
                    onChange={handleOnChange}
                    error={errors?.fullName}
                    required
                    placeholder="Enter customer's full name (e.g., John Doe)"
                />
                <Input
                    name="phone"
                    label="Phone"
                    value={customer.phone || ""}
                    onChange={handleOnChange}
                    error={errors?.phone}
                    placeholder="Enter phone number (e.g., +1 555-123-4567)"
                />
                <Select
                    name="status"
                    label="Status"
                    value={customer.status}
                    onChange={(e) => handleStatusChange(e.target.value)}
                    options={[
                        { value: "ACTIVE", label: "Active" },
                        { value: "PENDING_ACTIVATION", label: "Pending Activation" },
                        { value: "SUSPENDED", label: "Suspended" },
                        { value: "EXPIRING", label: "Expiring" },
                        { value: "EXPIRED", label: "Expired" },
                    ]}
                    error={errors?.status}
                />
            </div>

            <fieldset className="border border-gray-200 rounded-md p-4">
                <legend className="px-2 font-medium">Address</legend>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <Input
                        name="street"
                        label="Street"
                        value={customer.address.street}
                        onChange={handleAddressChange}
                        error={errors?.address?.street}
                        required
                        placeholder="Enter street address (e.g., 123 Main St)"
                    />
                    <Input
                        name="city"
                        label="City"
                        value={customer.address.city}
                        onChange={handleAddressChange}
                        error={errors?.address?.city}
                        required
                        placeholder="Enter city (e.g., New York)"
                    />
                    <Input
                        name="postalCode"
                        label="Postal Code"
                        value={customer.address.postalCode}
                        onChange={handleAddressChange}
                        error={errors?.address?.postalCode}
                        placeholder="Enter postal/zip code (e.g., 10001)"
                    />
                    <Input
                        name="country"
                        label="Country"
                        value={customer.address.country}
                        onChange={handleAddressChange}
                        placeholder="Enter country (e.g., United States)"
                    />
                </div>
            </fieldset>

            <Textarea
                name="notes"
                label="Notes"
                value={customer.notes || ""}
                onChange={handleOnChange}
                error={errors?.notes}
                placeholder="Enter any additional notes about the customer..."
            />

            <div className="flex justify-end space-x-3 pt-4">
                <Button
                    type="button"
                    variant="secondary"
                    onClick={handleReset}
                    disabled={isSubmitting}
                >
                    {resetButtonText}
                </Button>
                <Button
                    type="submit"
                    variant="primary"
                    disabled={isSubmitting}
                    isLoading={isSubmitting}
                >
                    {submitButtonText}
                </Button>
            </div>
        </form>
    );
};