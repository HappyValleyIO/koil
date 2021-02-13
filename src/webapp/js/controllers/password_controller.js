import { Controller } from "@stimulus/core";

export default class PasswordController extends Controller {
  static get targets() {
    return ["input"];
  }

  toggleVisibility() {
    if (this.inputTarget.getAttribute("type") === "password") {
      this.inputTarget.setAttribute("type", "text");
    } else {
      this.inputTarget.setAttribute("type", "password");
    }
  }
}
