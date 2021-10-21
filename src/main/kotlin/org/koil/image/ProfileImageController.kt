package org.koil.image

import org.koil.auth.EnrichedUserDetails
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import java.net.URI


@Controller
@RequestMapping("/dashboard/user-settings/image")
class ProfileImageController(
    private val profileImageService: ProfileImageService
) {
    @GetMapping
    fun getImageUrl(@AuthenticationPrincipal user: EnrichedUserDetails): ResponseEntity<Unit> {
        val image = profileImageService.getProfileImageUrl(user.accountId)

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI(image))
            .build()
    }

    @PostMapping
    fun handleFileUpload(
        @AuthenticationPrincipal user: EnrichedUserDetails,
        @RequestParam("file") file: MultipartFile
    ): ModelAndView {
        profileImageService.saveProfileImage(
            user.accountId,
            file
        )

        return ModelAndView("redirect:/dashboard/user-settings?imageUploaded=true")
    }
}
