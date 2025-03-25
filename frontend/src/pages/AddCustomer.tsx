import { useState } from "react";
import axios, { AxiosError } from "axios";
import { toast } from "react-toastify";
import { CustomerForm } from "../components/CustomerForm.tsx";
import { Customer } from "../types.ts";
import { useNavigate } from "react-router-dom";

export const AddCustomer = () => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (customer: Customer) => {
        setIsSubmitting(true);
        try {
            await axios.post("/api/customers", customer);
            toast.success("Customer added successfully!");
            navigate("/"); // Move navigation here
        } catch (error) {
            const axiosError = error as AxiosError;
            if (axiosError.response?.status === 409) {
                throw { username: "Username already exists" };
            }
            toast.error("Failed to add customer. Please try again.");
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
        />
    );
};