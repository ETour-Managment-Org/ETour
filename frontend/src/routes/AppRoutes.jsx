import { useEffect } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import AppLayout from "../layout/AppLayout";
import { useLayout } from "../hooks/useLayout";
import { ROUTES } from "../utils/constants";
import { routeConfig } from "./routeConfig";
import LoginPage from "../pages/LoginPage";

function RouteLayoutSync() {
  const location = useLocation();
  const { setPageTitle, setBreadcrumbs, setShowFooter, resetLayout } =
    useLayout();

  useEffect(() => {
    const currentRoute = routeConfig.find(
      (route) => route.path === location.pathname,
    );

    if (currentRoute?.layout) {
      setPageTitle(currentRoute.layout.pageTitle);
      setBreadcrumbs(currentRoute.layout.breadcrumbs);
      setShowFooter(currentRoute.layout.showFooter ?? true);
      return;
    }

    resetLayout();
  }, [
    location.pathname,
    resetLayout,
    setBreadcrumbs,
    setPageTitle,
    setShowFooter,
  ]);

  return null;
}

export default function AppRoutes() {
  const menuRoutes = routeConfig.filter(
    (route) => route.path !== ROUTES.NOT_FOUND,
  );

  return (
    <>
      <RouteLayoutSync />

      <Routes>
        <Route element={<LoginPage />} path="/login" />
        <Route element={<AppLayout />}>
          {menuRoutes.map((route) => (
            <Route key={route.path} path={route.path} element={route.element} />
          ))}
        </Route>

        <Route
          path={ROUTES.NOT_FOUND}
          element={routeConfig.find((r) => r.path === ROUTES.NOT_FOUND).element}
        />
        <Route path="*" element={<Navigate to={ROUTES.NOT_FOUND} replace />} />
      </Routes>
    </>
  );
}
