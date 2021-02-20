import { Controller } from "stimulus";

export default class MobileMenuController extends Controller {
  static get targets() {
    return ["toggleable"];
  }

  toggle() {
    this.toggleableTargets.forEach(el => el.classList.toggle("is-active"));
  }
}
