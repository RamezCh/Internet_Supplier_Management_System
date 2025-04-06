import { useState } from "react";
import axios, { AxiosError } from "axios";
import { toast } from "react-toastify";
import { InternetPlanForm } from "../components/InternetPlanForm";
import { InternetPlanDTO } from "../types";
import { useNavigate } from "react-router-dom";

export const AddInternetPlan = () => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submissionError, setSubmissionError] = useState<
        Partial<Record<keyof InternetPlanDTO, string>>
    >({});
    const navigate = useNavigate();

    const handleSubmit = async (plan: InternetPlanDTO) => {
        setIsSubmitting(true);
        setSubmissionError({});
        try {
            await axios.post("/api/internet_plans", plan, {
                validateStatus: (status) => status < 500 // Don't throw for 4xx errors
            });
            toast.success("Internet plan added successfully!");
            navigate("/internet-plans");
        } catch (error) {
            const axiosError = error as AxiosError<{
                errors?: Partial<Record<keyof InternetPlanDTO, string>>;
                message?: string;
            }>;

            if (axiosError.response) {
                switch (axiosError.response.status) {
                    case 400:
                        // Handle validation errors from backend
                        setSubmissionError(axiosError.response.data?.errors || {});
                        if (axiosError.response.data?.message) {
                            toast.error(axiosError.response.data.message);
                        }
                        break;
                    case 409:
                        setSubmissionError({
                            name: "A plan with this name already exists"
                        });
                        toast.error("A plan with this name already exists");
                        break;
                    case 401:
                        toast.error("You need to be logged in to perform this action");
                        navigate("/login");
                        break;
                    case 403:
                        toast.error("You don't have permission to add plans");
                        break;
                    default:
                        toast.error("Failed to add internet plan. Please try again.");
                        console.error("Server error:", axiosError.response.data);
                }
            } else {
                toast.error("Network error. Please check your connection and try again.");
                console.error("Network error:", error);
            }
            throw error; // Re-throw to allow form to handle submission state
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="container mx-auto px-4 py-8">
            <InternetPlanForm
                onSubmit={handleSubmit}
                isSubmitting={isSubmitting}
                submitButtonText="Add Plan"
                resetButtonText="Clear Form"
                mode="add"
                submissionError={submissionError}
            />
        </div>
    );
};