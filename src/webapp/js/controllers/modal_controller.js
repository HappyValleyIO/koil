import { Controller } from "stimulus";

export default class ModalController extends Controller {
  static get targets() {
    return ["upgrade"];
  }

  toggleUpgradeModal() {
    this.upgradeTarget.classList.toggle("is-active");
  }
}
