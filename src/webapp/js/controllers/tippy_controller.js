import { Controller } from "stimulus";
import tippy from "tippy.js";

export default class TippyController extends Controller {
  connect() {
    tippy(this.element);
  }
}
