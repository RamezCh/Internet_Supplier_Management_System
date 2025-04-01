import { KeyboardEvent, useEffect, useState } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { FaChevronLeft, FaChevronRight } from "react-icons/fa";

import { Customer } from "../types.ts";
import { CustomerCard } from "../components/CustomerCard.tsx";
import { Input } from "../shared/Input.tsx";
import { Button } from "../shared/Button.tsx";
import { RadioButton } from "../shared/RadioButton.tsx";

interface ApiResponse {
    content: Customer[];
    pageable: {
        pageNumber: number;
        pageSize: number;
    };
    totalPages: number;
    totalElements: number;
    first: boolean;
    last: boolean;
}

export const Customers = () => {
    const [customers, setCustomers] = useState<Customer[]>([]);
    const [searchQuery, setSearchQuery] = useState<string>("");
    const [status, setStatus] = useState<string>("");
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [pageSize, setPageSize] = useState<number>(5);
    const [totalPages, setTotalPages] = useState<number>(1);

    const statusOptions = [
        { value: "", label: "All" },
        { value: "ACTIVE", label: "Active" },
        { value: "EXPIRING", label: "Expiring" },
        { value: "SUSPENDED", label: "Suspended" },
        { value: "EXPIRED", label: "Expired" },
        { value: "PENDING_ACTIVATION", label: "Pending Activation" },
    ];

    const pageSizeOptions = [5, 10, 15, 20];

    const getCustomers = async (page: number = currentPage, size: number = pageSize) => {
        setIsLoading(true);
        try {
            const response = await axios.get<ApiResponse>("/api/customers", {
                params: {
                    page,
                    size,
                },
            });
            setCustomers(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error("Error fetching customers:", error);
            toast.error("Failed to load customers");
        } finally {
            setIsLoading(false);
        }
    };

    const searchCustomers = async (page: number = 0, size: number = pageSize) => {
        setIsLoading(true);
        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
            });

            if (searchQuery) {
                params.append("searchTerm", searchQuery.trim());
            }

            if (status) {
                params.append("status", status);
            }

            const response = await axios.get<ApiResponse>(
                `/api/customers/search?${params.toString()}`
            );
            setCustomers(response.data.content);
            setCurrentPage(page);
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
        searchCustomers(0);
    };

    const handleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Enter") {
            event.preventDefault();
            searchCustomers(0);
        }
    };

    const handleDelete = async (id: string) => {
        try {
            await axios.delete(`/api/customers/${id}`);
            toast.success("Customer deleted successfully");
            searchCustomers(currentPage);
        } catch (error) {
            console.error("Error deleting customer:", error);
            toast.error("Failed to delete customer");
        }
    };

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
        if (searchQuery || status) {
            searchCustomers(page);
        } else {
            getCustomers(page);
        }
    };

    const handlePageSizeChange = (size: number) => {
        setPageSize(size);
        if (searchQuery || status) {
            searchCustomers(0, size);
        } else {
            getCustomers(0, size);
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
                    onClick={() => searchCustomers(0)}
                    className="h-[42px]"
                    disabled={isLoading}
                >
                    {isLoading ? "Searching..." : "Search"}
                </Button>
            </div>

            <div className="flex flex-col lg:flex-row items-center mb-5 gap-4 w-full">
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
                    className="h-[42px] bg-gray-200 hover:bg-gray-300 text-gray-800"
                    disabled={isLoading || (!searchQuery && !status)}
                >
                    Reset
                </Button>

                <div className="ml-auto flex items-center gap-2">
                    <span className="text-sm text-gray-600">Items per page:</span>
                    <select
                        value={pageSize}
                        onChange={(e) => handlePageSizeChange(Number(e.target.value))}
                        className="border rounded-md p-1 text-sm"
                    >
                        {pageSizeOptions.map((size) => (
                            <option key={size} value={size}>
                                {size}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {isLoading ? (
                <div className="flex justify-center py-8">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                </div>
            ) : (
                <>
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

                    {customers.length > 0 && (
                        <div className="flex justify-center mt-6 gap-4">
                            <button
                                onClick={() => handlePageChange(currentPage - 1)}
                                disabled={currentPage === 0}
                                className="p-2 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                            >
                                <FaChevronLeft className="w-4 h-4" />
                            </button>

                            <span className="flex items-center">
                Page {currentPage + 1}
              </span>

                            <button
                                onClick={() => handlePageChange(currentPage + 1)}
                                disabled={totalPages - 1 <= currentPage}
                                className="p-2 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                            >
                                <FaChevronRight className="w-4 h-4" />
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};