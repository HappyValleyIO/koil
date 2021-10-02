package org.koil.dashboard

import org.koil.view.ViewRenderer

sealed class DashboardViews<MODEL>(override val template: String) : ViewRenderer<MODEL> {
    object Index : DashboardViews<Unit>("pages/dashboard/index")
    object ContactUs : DashboardViews<Unit>("pages/dashboard/contact-us")
}
