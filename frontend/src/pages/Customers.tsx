import { KeyboardEvent, useEffect, useState } from "react";
import axios from "axios";
import { Customer } from "../types.ts";
import { CustomerCard } from "../components/CustomerCard.tsx";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { Input } from "../shared/Input.tsx";
import { Button } from "../shared/Button.tsx";

export const Customers = () => {
    const [customers, setCustomers] = useState<Customer[] | undefined>();
    const [searchQuery, setSearchQuery] = useState<string>("");

    const getCustomers = async () => {
        try {
            const response = await axios.get<{ content: Customer[] }>("/api/customers");
            setCustomers(response.data.content);
        } catch (error) {
            console.error("Error fetching customers:", error);
            toast.error("Failed to load customers");
        }
    };

    const searchCustomers = async () => {
        try {
            const searchTermPart = searchQuery ? "searchTerm=" + encodeURIComponent(searchQuery.trim()) : "";
            const backendLink = `/api/customers/search?${searchTermPart}`;
            const response = await axios.get<{ content: Customer[] }>(backendLink);
            setCustomers(response.data.content);
        } catch (error) {
            console.error("Error searching customers:", error);
            toast.error("Failed to load customers");
        }
    };

    const handleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
        if (event.key === 'Enter') {
            event.preventDefault();
            searchCustomers();
        }
    };

    const handleDelete = async (id: string) => {
        try {
            await axios.delete(`/api/customers/${id}`);
            toast.success("Customer deleted successfully");
            await getCustomers();
        } catch (error) {
            console.error("Error deleting customer:", error);
            toast.error("Failed to delete customer");
        }
    };

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
                <Button onClick={searchCustomers} className="h-[42px]">
                    Search
                </Button>
            </div>
            <div className="grid gap-4">
                {customers?.map((customer) => (
                    <CustomerCard
                        key={customer.username}
                        customer={customer}
                        onDelete={handleDelete}
                    />
                ))}
            </div>
        </div>
    );
};