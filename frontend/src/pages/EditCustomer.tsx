import { useState, useEffect } from "react";
import axios, { AxiosError } from "axios";
import { toast } from "react-toastify";
import { useNavigate, useParams } from "react-router-dom";
import { CustomerForm } from "../components/CustomerForm.tsx";
import {Customer, CustomerDTO} from "../types.ts";

export const EditCustomer = () => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [initialData, setInitialData] = useState<Customer | null>(null);
    const params = useParams();
    const id = params.id;
    const navigate = useNavigate();

    const getCustomer = async () => {
        try {
            const response = await axios.get(`/api/customers/${id}`);
            setInitialData(response.data);
        } catch (error) {
            toast.error("Failed to load customer data");
            console.error("Error fetching customer:", error);
            navigate("/");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (id) {
            getCustomer();
        } else {
            setIsLoading(false);
            navigate("/");
        }
    }, [id, navigate]);

    const handleSubmit = async (customer: CustomerDTO) => {
        setIsSubmitting(true);
        try {
            await axios.put(`/api/customers/${id}`, customer);
            toast.success("Customer updated successfully!");
            navigate("/");
        } catch (error) {
            const axiosError = error as AxiosError;
            if (axiosError.response?.status === 404) {
                toast.error("Customer not found");
                navigate("/");
            } else {
                toast.error("Failed to update customer. Please try again.");
                console.error("Error updating customer:", error);
            }
            throw error;
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleCancel = () => {
        if (initialData) {
            setInitialData({ ...initialData }); // Force re-render with initial data
        }
    };

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (!initialData) {
        return null; // or some error state
    }

    return (
        <CustomerForm
            initialData={initialData}
            onSubmit={handleSubmit}
            onCancel={handleCancel}
            isSubmitting={isSubmitting}
            submitButtonText="Update Customer"
            resetButtonText="Reset Changes"
            mode="edit"
        />
    );
};