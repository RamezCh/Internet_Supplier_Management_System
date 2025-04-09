import { Input } from "../shared/Input";
import { Textarea } from "../shared/Textarea";
import { Select } from "../shared/Select";
import { CustomerDTO, CustomerStatus, InternetPlanSmallDTO } from "../types";
import { ChangeEvent, useState, useEffect, FormEvent, useCallback } from "react";
import { Button } from "../shared/Button";

interface CustomerFormProps {
    initialData?: Partial<CustomerDTO>;
    onSubmit: (customer: CustomerDTO, internetPlanId:string) => Promise<void>;
    onCancel?: () => void;
    isSubmitting: boolean;
    submitButtonText: string;
    resetButtonText: string;
    mode: "add" | "edit";
    loading?: boolean;
    submissionError?: Partial<Record<keyof Omit<CustomerDTO, "address">, string>> & {
        address?: Partial<Record<keyof CustomerDTO["address"], string>>;
    };
    initialPlans?: InternetPlanSmallDTO[];
    internetPlanId?: string;
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
    notes: "",
    internetPlan: undefined,
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
                                 initialPlans = [],
                                 internetPlanId,
                             }: CustomerFormProps) => {
    const [customer, setCustomer] = useState<CustomerDTO>({
        ...defaultCustomer,
        ...initialData,
        address: {
            ...defaultCustomer.address,
            ...initialData?.address,
        },
        internetPlan: initialData?.internetPlan || (internetPlanId ? { id: internetPlanId, name: "" } : undefined),
    });

    const [errors, setErrors] = useState<
        NonNullable<CustomerFormProps["submissionError"]>
    >({});
    const [internetPlans, setInternetPlans] = useState<InternetPlanSmallDTO[]>(initialPlans);
    const [loadingPlans, setLoadingPlans] = useState(!initialPlans.length);
    const [selectedPlanId, setSelectedPlanId] = useState<string>(internetPlanId ?? "");
    const [isFormValid, setIsFormValid] = useState(false);

    useEffect(() => {
        if (initialData) {
            setCustomer({
                ...defaultCustomer,
                ...initialData,
                address: {
                    ...defaultCustomer.address,
                    ...initialData.address,
                },
                internetPlan: initialData.internetPlan || (internetPlanId ? { id: internetPlanId, name: "" } : undefined),
            });
            setSelectedPlanId(initialData.internetPlan?.id || internetPlanId || "");
        }
    }, [initialData, internetPlanId]);

    useEffect(() => {
        if (submissionError) {
            setErrors((prev) => ({ ...prev, ...submissionError }));
        }
    }, [submissionError]);

    const fetchInternetPlans = async (): Promise<InternetPlanSmallDTO[]> => {
        const response = await fetch("/api/internet_plans/small");
        if (!response.ok) throw new Error("Failed to fetch internet plans");
        return response.json();
    };

    useEffect(() => {
        if (!initialPlans.length) {
            fetchInternetPlans()
                .then(setInternetPlans)
                .catch(console.error)
                .finally(() => setLoadingPlans(false));
        }
    }, []);

    const validateForm = useCallback((): boolean => {
        return Boolean(
            customer.username.trim() &&
            customer.fullName.trim() &&
            customer.phone.trim() &&
            selectedPlanId &&
            customer.address.street.trim() &&
            customer.address.city.trim()
        );
    }, [customer, selectedPlanId]);

    useEffect(() => {
        setIsFormValid(validateForm());
    }, [validateForm]);

    const handleOnChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setCustomer((prev) => ({
            ...prev,
            [name]: value,
        }));
        if (errors?.[name as keyof CustomerDTO]) {
            setErrors((prev) => ({ ...prev, [name]: undefined }));
        }
    };

    const handleAddressChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setCustomer((prev) => ({
            ...prev,
            address: {
                ...prev.address,
                [name]: value,
            },
        }));
        if (errors?.address?.[name as keyof CustomerDTO["address"]]) {
            setErrors((prev) => ({
                ...prev,
                address: {
                    ...prev.address,
                    [name]: undefined,
                },
            }));
        }
    };

    const handleStatusChange = (e: ChangeEvent<HTMLSelectElement>) => {
        const value = e.target.value;
        setCustomer((prev) => ({
            ...prev,
            status: value as CustomerStatus,
        }));
        if (errors?.status) {
            setErrors((prev) => ({ ...prev, status: undefined }));
        }
    };

    const handleInternetPlanChange = (e: ChangeEvent<HTMLSelectElement>) => {
        const selectedPlanId = e.target.value;
        const selectedPlan = internetPlans.find(plan => plan.id === selectedPlanId);
        setSelectedPlanId(selectedPlanId);
        setCustomer((prev) => ({
            ...prev,
            internetPlan: selectedPlan ? { id: selectedPlan.id, name: selectedPlan.name } : undefined,
        }));
        if (errors?.internetPlan) {
            setErrors((prev) => ({ ...prev, internetPlan: undefined }));
        }
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();

        const isValid = (() => {
            const newErrors: CustomerFormProps["submissionError"] = {};

            if (!customer.username.trim()) newErrors.username = "Username is required";
            if (!customer.fullName.trim()) newErrors.fullName = "Full name is required";
            if (!customer.phone.trim()) newErrors.phone = "Phone Number is required";
            if (!selectedPlanId) newErrors.internetPlan = "Internet plan is required";

            const addressErrors: Partial<Record<keyof CustomerDTO["address"], string>> = {};
            if (!customer.address.street.trim()) addressErrors.street = "Street is required";
            if (!customer.address.city.trim()) addressErrors.city = "City is required";

            if (Object.keys(addressErrors).length > 0) {
                newErrors.address = addressErrors;
            }

            setErrors(newErrors);
            return Object.keys(newErrors).length === 0;
        })();

        if (!isValid) return;

        const cleanedCustomer = { ...customer, internetPlan: undefined };
        await onSubmit(cleanedCustomer, selectedPlanId);
    };

    const handleReset = () => {
        if (onCancel) onCancel();
        else setCustomer(defaultCustomer);
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
                    disabled={mode === "edit"}
                    placeholder="Enter unique username"
                />
                <Input
                    name="fullName"
                    label="Full Name"
                    value={customer.fullName}
                    onChange={handleOnChange}
                    error={errors?.fullName}
                    required
                    placeholder="Enter customer's full name"
                />
                <Input
                    name="phone"
                    label="Phone"
                    value={customer.phone || ""}
                    onChange={handleOnChange}
                    error={errors?.phone}
                    required
                    placeholder="Enter phone number"
                />
                <Select
                    name="status"
                    label="Status"
                    value={customer.status}
                    onChange={handleStatusChange}
                    options={[
                        { value: "ACTIVE", label: "Active" },
                        { value: "PENDING_ACTIVATION", label: "Pending Activation" },
                        { value: "SUSPENDED", label: "Suspended" },
                        { value: "EXPIRING", label: "Expiring" },
                        { value: "EXPIRED", label: "Expired" },
                    ]}
                    error={errors?.status}
                />
                <Select
                    name="internetPlan"
                    label="Internet Plan"
                    value={selectedPlanId}
                    onChange={handleInternetPlanChange}
                    options={[
                        { value: "", label: "Select internet plan"},
                        ...internetPlans.map((plan) => ({
                            value: plan.id,
                            label: plan.name,
                        })),
                    ]}
                    error={errors?.internetPlan}
                    disabled={loadingPlans}
                    required
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
                        placeholder="Enter street address"
                    />
                    <Input
                        name="city"
                        label="City"
                        value={customer.address.city}
                        onChange={handleAddressChange}
                        error={errors?.address?.city}
                        required
                        placeholder="Enter city"
                    />
                    <Input
                        name="postalCode"
                        label="Postal Code"
                        value={customer.address.postalCode}
                        onChange={handleAddressChange}
                        error={errors?.address?.postalCode}
                        placeholder="Enter postal code"
                    />
                    <Input
                        name="country"
                        label="Country"
                        value={customer.address.country}
                        onChange={handleAddressChange}
                        placeholder="Enter country"
                    />
                </div>
            </fieldset>

            <Textarea
                name="notes"
                label="Notes"
                value={customer.notes ?? ""}
                onChange={handleOnChange}
                error={errors?.notes}
                placeholder="Enter any additional notes"
            />

            <div className="flex justify-between space-x-3 pt-4">
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
                    disabled={isSubmitting || !isFormValid}
                    isLoading={isSubmitting}
                >
                    {submitButtonText}
                </Button>
            </div>
        </form>
    );
};