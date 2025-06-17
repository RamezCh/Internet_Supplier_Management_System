import { useEffect, useState } from "react";
import { toast } from "react-toastify";
import axios from "axios";
import { FaBolt, FaTachometerAlt, FaDollarSign, FaEdit, FaTrash } from "react-icons/fa";
import { InternetPlan } from "../types.ts";
import { useNavigate } from "react-router-dom";
import {Button} from "../shared/Button.tsx";

export const InternetPlans = () => {
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [internetPlans, setInternetPlans] = useState<InternetPlan[]>([]);
    const navigate = useNavigate();

    const fetchInternetPlans = async () => {
        try {
            const { data } = await axios.get<InternetPlan[]>("/api/internet_plans");
            setInternetPlans(data);
        } catch (err) {
            console.error("Error fetching internet plans:", err);
            toast.error("Failed to load internet plans");
        } finally {
            setIsLoading(false);
        }
    };

    const handleDelete = async (id: string) => {
        if (!window.confirm("Are you sure you want to delete this plan?")) return;

        try {
            await axios.delete(`/api/internet_plans/${id}`);
            toast.success("Plan deleted successfully");
            fetchInternetPlans(); // Refresh the list
        } catch (err) {
            console.error("Error deleting plan:", err);
            toast.error("Failed to delete plan");
        }
    };

    const handleEdit = (id: string) => {
        navigate(`/internet-plan/${id}/edit`);
    };

    useEffect(() => {
        fetchInternetPlans();
    }, []);

    if (isLoading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            </div>
        );
    }

    if (internetPlans.length === 0) {
        return (
            <div className="space-y-6 p-4">
                <div className="flex justify-between items-center mb-6">
                    <h1 className="text-2xl font-bold text-gray-800">Internet Plans</h1>
                    <Button 
                        onClick={() => navigate('/internet-plan/add')}
                        className="flex items-center gap-2"
                    >
                        Add New Plan
                    </Button>
                </div>
                <div className="p-6 bg-blue-50 rounded-lg border border-blue-200 text-blue-600 text-center">
                    No internet plans available. Click "Add New Plan" to create one.
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6 p-4">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Internet Plans</h1>
                <Button 
                    onClick={() => navigate('/internet-plan/add')}
                    className="flex items-center gap-2"
                >
                    Add New Plan
                </Button>
            </div>
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                {internetPlans.map((plan) => (
                    <div
                        key={plan.id}
                        className={`rounded-lg shadow-md overflow-hidden border ${
                            plan.isActive
                                ? "border-blue-500 hover:shadow-lg"
                                : "border-gray-200 opacity-80"
                        } transition-all`}
                    >
                        <div className="p-6">
                            <div className="flex justify-between items-start">
                                <h3 className="text-xl font-semibold text-gray-800">
                                    {plan.name}
                                </h3>
                                {!plan.isActive && (
                                    <span className="px-2 py-1 text-xs bg-gray-100 text-gray-600 rounded">
                                        Inactive
                                    </span>
                                )}
                            </div>

                            <div className="mt-4 space-y-3">
                                <div className="flex items-center">
                                    <FaBolt className="text-blue-500 mr-2" />
                                    <span className="text-gray-600">{plan.speed}</span>
                                </div>

                                <div className="flex items-center">
                                    <FaTachometerAlt className="text-blue-500 mr-2" />
                                    <span className="text-gray-600">{plan.bandwidth}</span>
                                </div>

                                <div className="flex items-center">
                                    <FaDollarSign className="text-blue-500 mr-2" />
                                    <span className="text-gray-600">${plan.price.toFixed(2)}/mo</span>
                                </div>
                            </div>

                            <div className="mt-6 flex justify-between space-x-2">
                                <Button className="py-2 px-3 flex items-center justify-center gap-2" onClick={() => handleEdit(plan.id)}><FaEdit /> Edit</Button>
                                <Button className="py-2 px-3 flex items-center justify-center gap-2" onClick={() => handleDelete(plan.id)} variant="red"><FaTrash /> Delete</Button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};
