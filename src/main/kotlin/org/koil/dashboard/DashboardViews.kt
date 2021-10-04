package org.koil.dashboard

import org.koil.view.ViewRenderer

data class IndexViewModel(
    val accountVerified: Boolean,
    val accountAlreadyVerified: Boolean,
    val incorrectVerificationCode: Boolean,
)

sealed class DashboardViews<MODEL>(override val template: String) : ViewRenderer<MODEL> {
    object Index : DashboardViews<IndexViewModel>("pages/dashboard/index")
    object ContactUs : DashboardViews<Unit>("pages/dashboard/contact-us")
}
