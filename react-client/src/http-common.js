import axios from "axios";

// Use a relative base URL so the bundled React app can be served from the backend
// under /app/react/ without hard-coding localhost (works behind reverse proxy / containers too).
export default axios.create({
    baseURL: "",
    headers: {
        "Content-type": "application/json"
    }
});