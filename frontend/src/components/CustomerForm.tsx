import { Input } from "../shared/Input.tsx";
import { Textarea } from "../shared/Textarea.tsx";
import { Customer } from "../types.ts";
import { ChangeEvent, useState, useEffect } from "react";
import { Button } from "../shared/Button.tsx";

interface CustomerFormProps {
    initialData?: Customer;
    onSubmit: (customer: Customer) => Promise<void>;
    onCancel?: () => void;
    isSubmitting: boolean;
    submitButtonText: string;
    resetButtonText: string;
    mode: 'add' | 'edit';
    loading?: boolean;
    submissionError?: Partial<Customer>; // New prop for submission errors
}

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
    const [customer, setCustomer] = useState<Customer>(initialData || {
        username: "",
        fullName: "",
        notes: ""
    });
    const [errors, setErrors] = useState<Partial<Customer>>({});

    // Sync submission errors with form errors
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
        // Clear error when user types
        if (errors[name as keyof Customer]) {
            setErrors(prev => ({ ...prev, [name]: undefined }));
        }
    };

    const handleClearForm = () => {
        if (onCancel) {
            onCancel();
        } else {
            setCustomer(initialData || { username: "", fullName: "", notes: "" });
            setErrors({});
        }
    };

    const validateForm = (): boolean => {
        const newErrors: Partial<Customer> = {};
        if (!customer.username.trim()) newErrors.username = "Username is required";
        if (!customer.fullName.trim()) newErrors.fullName = "Full name is required";

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleFormSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) return;

        try {
            await onSubmit(customer);
        } catch (error) {
            console.error("Error submitting form:", error);
        }
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <form className="space-y-4" onSubmit={handleFormSubmit}>
            <Input
                name="username"
                label="Customer's Username"
                placeholder="Please enter customer username, make sure it is unique"
                value={customer.username}
                handleOnChange={handleOnChange}
                required
                error={errors.username}
                disabled={mode === 'edit'}
            />
            <Input
                name="fullName"
                label="Customer's Full Name"
                placeholder="Please enter customer full name"
                value={customer.fullName}
                handleOnChange={handleOnChange}
                required
                error={errors.fullName}
            />
            <Textarea
                name="notes"
                label="Note"
                placeholder="If you have any note about the customer add it here"
                value={customer.notes || ""}
                handleOnChange={handleOnChange}
            />
            <div className="flex flex-row justify-between pt-2">
                <Button
                    variant="secondary"
                    onClick={handleClearForm}
                    type="button"
                    disabled={isSubmitting}
                >
                    {resetButtonText}
                </Button>
                <Button
                    variant="primary"
                    type="submit"
                    isLoading={isSubmitting}
                >
                    {submitButtonText}
                </Button>
            </div>
        </form>
    );
};