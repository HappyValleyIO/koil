import Choices from "choices.js";
import { Controller } from "stimulus";

export default class ChoicesController extends Controller {
  static get targets() {
    return [];
  }

  connect() {
    new Choices(this.element, {
      classNames: {
        input: "input",
        list: "tags",
        button: "delete is-small",
        item: "tag is-medium"
      }
    });
  }
}
