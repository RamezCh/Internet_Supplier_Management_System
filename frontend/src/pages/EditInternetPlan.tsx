import { useState, useEffect } from "react";
import axios, { AxiosError } from "axios";
import { toast } from "react-toastify";
import { useNavigate, useParams } from "react-router-dom";
import { InternetPlanForm } from "../components/InternetPlanForm";
import { InternetPlanDTO } from "../types";

export const EditInternetPlan = () => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [initialData, setInitialData] = useState<InternetPlanDTO | null>(null);
    const params = useParams();
    const id = params.id;
    const navigate = useNavigate();

    const getInternetPlan = async () => {
        try {
            const response = await axios.get(`/api/internet_plans/${id}`);
            setInitialData(response.data);
        } catch (error) {
            toast.error("Failed to load internet plan data");
            console.error("Error fetching internet plan:", error);
            navigate("/internet-plans");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (id) {
            getInternetPlan();
        } else {
            setIsLoading(false);
            navigate("/internet-plans");
        }
    }, [id, navigate]);

    const handleSubmit = async (plan: InternetPlanDTO) => {
        setIsSubmitting(true);
        try {
            await axios.put(`/api/internet_plans/${id}`, plan);
            toast.success("Internet plan updated successfully!");
            navigate("/internet-plans");
        } catch (error) {
            const axiosError = error as AxiosError;
            if (axiosError.response?.status === 404) {
                toast.error("Internet plan not found");
                navigate("/internet-plans");
            } else if (axiosError.response?.status === 409) {
                toast.error("A plan with this name already exists");
            } else {
                toast.error("Failed to update internet plan. Please try again.");
                console.error("Error updating internet plan:", error);
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
        return null;
    }

    return (
        <InternetPlanForm
            initialData={initialData}
            onCancel={handleCancel}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitButtonText="Update Plan"
            resetButtonText="Reset Changes"
            mode="edit"
        />
    );
};