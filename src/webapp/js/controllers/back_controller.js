import {Controller} from "stimulus";

export default class BackController extends Controller {
    goBack() {
        window.history.back()
    }
}
