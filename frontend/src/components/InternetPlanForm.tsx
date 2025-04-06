import { FormEvent, useState } from "react";
import { Input } from "../shared/Input";
import { RadioButton } from "../shared/RadioButton";
import { InternetPlanDTO } from "../types.ts";

interface InternetPlanFormProps {
    onSubmit: (plan: InternetPlanDTO) => Promise<void>;
    isSubmitting: boolean;
    submitButtonText: string;
    resetButtonText: string;
    mode: "add" | "edit";
    submissionError?: Partial<Record<keyof InternetPlanDTO, string>>;
    initialData?: InternetPlanDTO;
}

export const InternetPlanForm = ({
                                     onSubmit,
                                     isSubmitting,
                                     submitButtonText,
                                     resetButtonText,
                                     mode,
                                     submissionError,
                                     initialData
                                 }: InternetPlanFormProps) => {
    const [formData, setFormData] = useState<InternetPlanDTO>(initialData || {
        name: "",
        speed: "",
        price: 0,
        bandwidth: "",
        isActive: true,
    });

    const [clientErrors, setClientErrors] = useState<
        Partial<Record<keyof InternetPlanDTO, string>>
    >({});

    const validateForm = (): boolean => {
        const errors: Partial<Record<keyof InternetPlanDTO, string>> = {};

        if (!formData.name.trim()) {
            errors.name = "Plan name is required";
        }

        if (!formData.speed.trim()) {
            errors.speed = "Speed is required";
        } else if (formData.speed.length > 10) {
            errors.speed = "Speed must be at most 10 characters";
        }

        if (formData.price < 0) {
            errors.price = "Price must be non-negative";
        }

        if (!formData.bandwidth.trim()) {
            errors.bandwidth = "Bandwidth is required";
        }

        setClientErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, type } = e.target;
        setFormData({
            ...formData,
            [name]: type === "number" ? (value === "" ? "" : Number(value)) : value,
        });

        // Clear error when user starts typing
        if (clientErrors[name as keyof InternetPlanDTO]) {
            setClientErrors(prev => ({
                ...prev,
                [name]: undefined
            }));
        }
    };

    const handleStatusChange = (value: string) => {
        setFormData({
            ...formData,
            isActive: value === "active",
        });
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            await onSubmit({
                ...formData,
                price: Number(formData.price) // Ensure price is number
            });
        } catch (error) {
            // Server-side errors are handled by parent component via submissionError
            console.error("Form submission error:", error);
        }
    };

    const handleReset = () => {
        setFormData(initialData || {
            name: "",
            speed: "",
            price: 0,
            bandwidth: "",
            isActive: true,
        });
        setClientErrors({});
    };

    // Combine server and client errors
    const getError = (field: keyof InternetPlanDTO): string | undefined => {
        return submissionError?.[field] || clientErrors[field];
    };

    return (
        <div className="mx-auto p-6 bg-white">
            <h2 className="text-2xl font-bold text-gray-800 mb-6">
                {mode === "add" ? "Add New Internet Plan" : "Edit Internet Plan"}
            </h2>

            <form onSubmit={handleSubmit} noValidate>
                <div className="space-y-4">
                    <Input
                        label="Plan Name"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        placeholder="e.g., Premium Fiber"
                        required
                        error={getError('name')}
                    />

                    <Input
                        label="Speed"
                        name="speed"
                        value={formData.speed}
                        onChange={handleChange}
                        placeholder="e.g., 100 Mbps"
                        required
                        maxLength={10}
                        error={getError('speed')}
                    />

                    <Input
                        label="Price (USD)"
                        name="price"
                        type="number"
                        value={formData.price}
                        onChange={handleChange}
                        placeholder="e.g., 49.99"
                        min="0"
                        step="0.01"
                        required
                        error={getError('price')}
                    />

                    <Input
                        label="Bandwidth"
                        name="bandwidth"
                        value={formData.bandwidth}
                        onChange={handleChange}
                        placeholder="e.g., Unlimited"
                        required
                        error={getError('bandwidth')}
                    />

                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Status
                        </label>
                        <RadioButton
                            options={[
                                { value: "active", label: "Active" },
                                { value: "inactive", label: "Inactive" },
                            ]}
                            name="status"
                            selectedValue={formData.isActive ? "active" : "inactive"}
                            onChange={handleStatusChange}
                        />
                        {getError('isActive') && (
                            <p className="mt-1 text-sm text-red-600">
                                {getError('isActive')}
                            </p>
                        )}
                    </div>
                </div>

                <div className="mt-6 flex justify-between space-x-3">
                    <button
                        type="button"
                        onClick={handleReset}
                        className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                        {resetButtonText}
                    </button>
                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isSubmitting ? "Processing..." : submitButtonText}
                    </button>
                </div>
            </form>
        </div>
    );
};