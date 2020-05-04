import { Application } from "@stimulus/core";
import MobileMenuController from "./controllers/mobile_menu_controller";
import PasswordController from "./controllers/password_controller";
import ChoicesController from "./controllers/choices_controller";
import NavController from "./controllers/nav_controller";
import ModalController from "./controllers/modal_controller";
import TippyController from "./controllers/tippy_controller";

const application = Application.start();
application.register("choices", ChoicesController);
application.register("menu", MobileMenuController);
application.register("modal", ModalController);
application.register("nav-link", NavController);
application.register("password", PasswordController);
application.register("tippy", TippyController);
