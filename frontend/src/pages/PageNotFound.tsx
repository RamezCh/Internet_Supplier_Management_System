import { FaFrown, FaHome } from "react-icons/fa";
import { useNavigate } from "react-router-dom";

export const PageNotFound = () => {
    const navigate = useNavigate();

    return (
        <div className="w-full h-screen flex flex-col items-center justify-center p-4">
            <div className="max-w-2xl w-full text-center space-y-8">
                <div className="text-8xl text-blue-600 flex justify-center">
                    <FaFrown />
                </div>

                <div className="space-y-4">
                    <h1 className="text-5xl font-bold text-gray-800 leading-tight">
                        404 - Page Not Found
                    </h1>
                    <p className="text-2xl text-gray-600">
                        How did you get here? Perhaps a typo?
                    </p>
                </div>

                <p className="text-xl text-gray-500 max-w-lg mx-auto">
                    The page you're looking for doesn't exist in our Internet Supplier Management System.
                </p>

                <button
                    onClick={() => navigate("/")}
                    className="flex items-center justify-center gap-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-lg py-3 px-6 rounded-lg transition-colors duration-200 mx-auto mt-6"
                >
                    <FaHome className="text-xl" />
                    Return to Home
                </button>
            </div>
        </div>
    );
}