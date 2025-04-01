import { KeyboardEvent, useEffect, useState } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import { Customer } from "../types.ts";
import { CustomerCard } from "../components/CustomerCard.tsx";
import { Input } from "../shared/Input.tsx";
import { Button } from "../shared/Button.tsx";
import { RadioButton } from "../shared/RadioButton.tsx";

export const Customers = () => {
    const [customers, setCustomers] = useState<Customer[]>([]);
    const [searchQuery, setSearchQuery] = useState<string>("");
    const [status, setStatus] = useState<string>("");
    const [isLoading, setIsLoading] = useState<boolean>(false);

    const statusOptions = [
        { value: "", label: "All" },
        { value: "ACTIVE", label: "Active" },
        { value: "EXPIRING", label: "Expiring" },
        { value: "SUSPENDED", label: "Suspended" },
        { value: "EXPIRED", label: "Expired" },
        { value: "PENDING_ACTIVATION", label: "Pending Activation" },
    ];

    const getCustomers = async () => {
        setIsLoading(true);
        try {
            const response = await axios.get<{ content: Customer[] }>("/api/customers");
            setCustomers(response.data.content);
        } catch (error) {
            console.error("Error fetching customers:", error);
            toast.error("Failed to load customers");
        } finally {
            setIsLoading(false);
        }
    };

    const searchCustomers = async () => {
        setIsLoading(true);
        try {
            const params = new URLSearchParams();

            if (searchQuery) {
                params.append("searchTerm", searchQuery.trim());
            }

            if (status) {
                params.append("status", status);
            }

            const response = await axios.get<{ content: Customer[] }>(
                `/api/customers/search?${params.toString()}`
            );
            setCustomers(response.data.content);
        } catch (error) {
            console.error("Error searching customers:", error);
            toast.error("Failed to load customers");
        } finally {
            setIsLoading(false);
        }
    };

    const resetFilters = () => {
        setSearchQuery("");
        setStatus("");
        getCustomers();
    };

    const handleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Enter") {
            event.preventDefault();
            searchCustomers();
        }
    };

    const handleDelete = async (id: string) => {
        try {
            await axios.delete(`/api/customers/${id}`);
            toast.success("Customer deleted successfully");
            await searchCustomers(); // Maintain current filters after deletion
        } catch (error) {
            console.error("Error deleting customer:", error);
            toast.error("Failed to delete customer");
        }
    };

    // Initial load
    useEffect(() => {
        getCustomers();
    }, []);

    return (
        <div className="flex flex-col gap-4 p-4 max-w-full">
            <div className="flex flex-row items-center mb-5 gap-4 w-full">
                <Input
                    label="Search Bar"
                    placeholder="Search by username, name, city, phone number..."
                    value={searchQuery}
                    onChange={(event) => setSearchQuery(event.target.value)}
                    onKeyDown={handleKeyDown}
                    containerClassName="flex-grow mb-0"
                />
                <Button
                    onClick={searchCustomers}
                    className="h-[42px]"
                    disabled={isLoading}
                >
                    {isLoading ? "Searching..." : "Search"}
                </Button>
            </div>

            <div className="flex flex-row items-center mb-5 gap-4 w-full">
                <RadioButton
                    name="customerStatus"
                    options={statusOptions}
                    selectedValue={status}
                    onChange={setStatus}
                    orientation="horizontal"
                    className="p-4 border rounded-lg"
                />
                <Button
                    onClick={resetFilters}
                    variant="red"
                    disabled={isLoading || (!searchQuery && !status)}
                >
                    Reset
                </Button>
            </div>

            {isLoading ? (
                <div className="flex justify-center py-8">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                </div>
            ) : (
                <div className="grid gap-4">
                    {customers.length > 0 ? (
                        customers.map((customer) => (
                            <CustomerCard
                                key={customer.username}
                                customer={customer}
                                onDelete={handleDelete}
                            />
                        ))
                    ) : (
                        <div className="text-center py-8 text-gray-500">
                            No customers found matching your criteria
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};