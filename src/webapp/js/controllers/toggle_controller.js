import {Controller} from "stimulus";

export default class ToggleController extends Controller {
  static get targets() {
    return ["toggleable"];
  }

  toggle() {
      this.toggleableTargets.forEach(el => el.toggleAttribute('data-active'));
  }
}
