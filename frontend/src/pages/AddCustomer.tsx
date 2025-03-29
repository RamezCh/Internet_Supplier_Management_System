import { useState } from "react";
import axios, { AxiosError } from "axios";
import { toast } from "react-toastify";
import { CustomerForm } from "../components/CustomerForm.tsx";
import {Customer, CustomerDTO} from "../types.ts";
import { useNavigate } from "react-router-dom";

export const AddCustomer = () => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submissionError, setSubmissionError] = useState<
        Partial<Record<keyof Omit<Customer, 'address'>, string>> & {
        address?: Partial<Record<keyof Customer['address'], string>>;
    }
    >({});
    const navigate = useNavigate();

    const handleSubmit = async (customer: CustomerDTO) => {
        setIsSubmitting(true);
        setSubmissionError({});
        try {
            await axios.post("/api/customers", customer);
            toast.success("Customer added successfully!");
            navigate("/");
        } catch (error) {
            const axiosError = error as AxiosError;
            if (axiosError.response?.status === 409) {
                setSubmissionError({ username: "Username already exists" });
            } else {
                toast.error("Failed to add customer. Please try again.");
                console.error("Error adding customer:", error);
            }
            throw error;
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <CustomerForm
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitButtonText="Add Customer"
            resetButtonText="Clear Inputs"
            mode="add"
            submissionError={submissionError}
        />
    );
};