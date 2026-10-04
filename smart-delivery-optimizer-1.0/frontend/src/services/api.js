const API_BASE_URL = "http://localhost:8080/api";

async function get(endpoint) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`);

  if (!response.ok) {
    throw new Error(
      `API request failed: ${response.status} ${response.statusText}`
    );
  }

  return response.json();
}

export const api = {
  getOrders: () => get("/orders"),

  getVehicles: () => get("/vehicles"),

  getDistances: () => get("/distances"),

  getRoutes: () => get("/optimization/routes"),

  getOptimizationSummary: () =>
    get("/optimization/summary"),

  getUnallocatedOrders: () =>
    get("/optimization/unallocated"),

  getUnallocatedCount: () =>
    get("/optimization/unallocated/count"),

  getUnallocatedReasons: () =>
    get("/optimization/unallocated/reasons"),
};