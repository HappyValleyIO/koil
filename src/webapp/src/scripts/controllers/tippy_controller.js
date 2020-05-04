import { Controller } from "@stimulus/core";
import tippy from "tippy.js";

export default class TippyController extends Controller {
  connect() {
    tippy(this.element);
  }
}
