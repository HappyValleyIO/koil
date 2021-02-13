import { Controller } from "@stimulus/core";

export default class ModalController extends Controller {
  static get targets() {
    return ["upgrade"];
  }

  toggleUpgradeModal() {
    this.upgradeTarget.classList.toggle("is-active");
  }
}
