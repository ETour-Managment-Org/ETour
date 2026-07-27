export const APP_NAME = "CDAC Project";

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "https://jsonplaceholder.typicode.com";

export const ROUTES = {
  LOGIN: "/login",
  HOME: "/",
  ABOUT: "/about",
  NOT_FOUND: "*",
};
