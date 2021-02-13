import { Application } from "@stimulus/core";
import MobileMenuController from "./controllers/mobile_menu_controller";
import PasswordController from "./controllers/password_controller";
import ChoicesController from "./controllers/choices_controller";
import NavController from "./controllers/nav_controller";
import ModalController from "./controllers/modal_controller";
import TippyController from "./controllers/tippy_controller";

const controllers = Application.start();
controllers.register("choices", ChoicesController);
controllers.register("menu", MobileMenuController);
controllers.register("modal", ModalController);
controllers.register("nav-link", NavController);
controllers.register("password", PasswordController);
controllers.register("tippy", TippyController);
