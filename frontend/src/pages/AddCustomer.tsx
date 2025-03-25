import { Input } from "../shared/Input.tsx";
import { Textarea } from "../shared/Textarea.tsx";
import { Customer } from "../types.ts";
import { ChangeEvent, useState } from "react";
import { Button } from "../shared/Button.tsx";
import axios, { AxiosError } from "axios";
import { toast } from "react-toastify";
import {useNavigate} from "react-router-dom";

const InitialState: Customer = {
    username: "",
    fullName: "",
    notes: ""
};

export const AddCustomer = () => {
    const [customer, setCustomer] = useState<Customer>(InitialState);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errors, setErrors] = useState<Partial<Customer>>({});
    const navigate = useNavigate();

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
        setCustomer(InitialState);
        setErrors({});
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

        setIsSubmitting(true);

        try {
            await axios.post("/api/customers", customer);
            toast.success("Customer added successfully!");
            handleClearForm();
            navigate("/");
        } catch (error) {
            const axiosError = error as AxiosError;
            if (axiosError.response?.status === 409) {
                setErrors(prev => ({ ...prev, username: "Username already exists" }));
            }
            toast.error("Failed to add customer. Please try again.");
            console.error("Error adding customer:", error);
        } finally {
            setIsSubmitting(false);
        }
    };

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
            />
            <Input
                name="fullName"
                label="Customer's Full Name"
                placeholder="Please enter customer full name."
                value={customer.fullName}
                handleOnChange={handleOnChange}
                required
                error={errors.fullName}
            />
            <Textarea
                name="notes"
                label="Note"
                placeholder="If you have any note about the customer add it here"
                value={customer.notes}
                handleOnChange={handleOnChange}
            />
            <div className="flex flex-row justify-between pt-2">
                <Button
                    variant="secondary"
                    onClick={handleClearForm}
                    type="button"
                    disabled={isSubmitting}
                >
                    Clear Inputs
                </Button>
                <Button
                    variant="primary"
                    type="submit"
                    isLoading={isSubmitting}
                >
                    Add Customer
                </Button>
            </div>
        </form>
    );
};