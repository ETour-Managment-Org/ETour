import { HomeOutlined, InfoCircleOutlined } from "@ant-design/icons";
import HomePage from "../pages/HomePage";
import AboutPage from "../pages/AboutPage";
import NotFoundPage from "../pages/NotFoundPage";
import { ROUTES } from "../utils/constants";
import LoginPage from "../pages/LoginPage";

export const routeConfig = [
  {
    path: ROUTES.HOME,
    label: "Home",
    icon: <HomeOutlined />,
    element: <HomePage />,
    showInMenu: true,
    layout: {
      pageTitle: "Home",
      breadcrumbs: [{ title: "Home" }],
    },
  },
  {
    path: ROUTES.ABOUT,
    label: "About",
    icon: <InfoCircleOutlined />,
    element: <AboutPage />,
    showInMenu: true,
    layout: {
      pageTitle: "About",
      breadcrumbs: [{ title: "Home", href: ROUTES.HOME }, { title: "About" }],
    },
  },
  {
    path: ROUTES.NOT_FOUND,
    label: "Not Found",
    element: <NotFoundPage />,
    showInMenu: false,
    layout: {
      pageTitle: "Page Not Found",
      breadcrumbs: [{ title: "Not Found" }],
    },
  },
];
